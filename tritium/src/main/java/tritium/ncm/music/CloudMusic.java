package tritium.ncm.music;

import com.google.gson.*;
import com.jsyn.exceptions.ChannelMismatchException;
import javazoom.jl.converter.Converter;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import org.apache.commons.io.IOUtils;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import org.kc7bfi.jflac.util.WavWriter;
import tritium.management.CommandManager;
import tritium.ncm.OptionsUtil;
import tritium.ncm.api.CloudMusicApi;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.ncm.music.dto.User;
import tritium.management.ConfigManager;
import tritium.management.WidgetsManager;
import tritium.rendering.GaussianKernel;
import tritium.rendering.MusicToast;
import tritium.rendering.texture.Textures;
import tritium.screens.ConsoleScreen;
import tritium.screens.ncm.LyricLine;
import tritium.screens.ncm.LyricParser;
import tritium.screens.ncm.MusicLyricsPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.json.JsonUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.StringUtils;
import tritium.utils.other.WrappedInputStream;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 9:34 AM
 */
public class CloudMusic {

    @Getter
    private static final Map<String, String> headers = new HashMap<>();
    public static AudioPlayer player;
    // 当前播放列表
    public static List<Music> playList = new ArrayList<>();
    public static int curIdx = 0;
    public static Music currentlyPlaying;
    public static Thread playThread;

    public static User profile;
    public static List<PlayList> playLists;
    public static List<Long> likeList;

    public static PlayMode playMode = PlayMode.Sequential;

    public static Quality quality = Quality.STANDARD;

    public static final List<LyricLine> lyrics = new CopyOnWriteArrayList<>();
    public static LyricLine currentLyric = null;
    public static boolean hasTransLyrics = false;
    public static boolean hasRomanization = false;

    public static final File COOKIE_FILE = new File(ConfigManager.configDir, "NCMCookie.txt");

    public static void initLyrics(JsonObject rawLyricData, Music music, List<LyricLine> parsedLyrics) {
        resetLyricFlags();
        detectTranslations(rawLyricData);
        
        synchronized (lyrics) {
            updateLyricsList(parsedLyrics);
            currentLyric = lyrics.getFirst();
            addLongBreaks();
        }

        MusicLyricsPanel.updateLyricPositionsImmediate(NCMScreen.getInstance().getPanelWidth() * MusicLyricsPanel.getLyricWidthFactor());
    }
    
    private static void resetLyricFlags() {
        hasTransLyrics = false;
        hasRomanization = false;
    }
    
    private static void updateLyricsList(List<LyricLine> parsedLyrics) {
        lyrics.clear();
        lyrics.addAll(parsedLyrics);
        
        if (lyrics.isEmpty()) {
            lyrics.add(new LyricLine(0L, "暂无歌词"));
        }
    }

    private static void detectTranslations(JsonObject lyric) {
        if (hasLyricsType(lyric, "tlyric") || hasLyricsType(lyric, "ytlrc")) hasTransLyrics = true;
        if (hasLyricsType(lyric, "romalrc") || hasLyricsType(lyric, "yromalrc")) hasRomanization = true;
    }

    private static boolean hasLyricsType(JsonObject lyric, String type) {
        if (lyric.has(type) && lyric.get(type).isJsonObject()) {
            JsonObject lyricTypeObj = lyric.get(type).getAsJsonObject();
            return lyricTypeObj.has("lyric") && !lyricTypeObj.get("lyric").getAsString().isEmpty();
        }
        return false;
    }

    /**
     * 为歌词添加长间隔时的 "● ● ●"
     */
    private static void addLongBreaks() {
        final long longBreaksDuration = 3000L;
        
        if (lyricsHaveNoWords()) {
            // 如果不为逐字歌词的话只在开头添加长间隔
            addInitialBreakIfNeeded(longBreaksDuration);
            return;
        }
        
        addBreaksBetweenLyrics(longBreaksDuration);
    }

    /**
     * 歌词是否不为逐字歌词
     * @return true 表示不为逐字歌词
     */
    private static boolean lyricsHaveNoWords() {
        return lyrics.stream().allMatch(l -> l.words.isEmpty());
    }

    private static void addInitialBreakIfNeeded(long duration) {
        long firstTimestamp = lyrics.getFirst().getTimestamp();
        if (firstTimestamp >= duration) {
            addBreakLine(0L, firstTimestamp);
        }
    }
    
    private static void addBreaksBetweenLyrics(long duration) {
        long lastTimestamp = 0L;
        List<LyricLine> breaksToAdd = new ArrayList<>();

        for (LyricLine lyric : lyrics) {
            long lyricDuration = getLyricDuration(lyric);
            long gap = lyric.getTimestamp() - lastTimestamp;
            
            if (gap >= duration) {
                breaksToAdd.add(createBreakLine(lastTimestamp, gap));
            }
            
            lastTimestamp = lyric.getTimestamp() + lyricDuration;
        }

        addAndSortBreaks(breaksToAdd);
    }
    
    private static LyricLine createBreakLine(long timestamp, long duration) {
        LyricLine line = new LyricLine(timestamp, "● ● ●");
        line.words.add(new LyricLine.Word("● ● ●", duration));
        return line;
    }
    
    private static void addBreakLine(long timestamp, long duration) {
        lyrics.add(createBreakLine(timestamp, duration));
        lyrics.sort(Comparator.comparingLong(LyricLine::getTimestamp));
    }
    
    private static void addAndSortBreaks(List<LyricLine> breaks) {
        lyrics.addAll(breaks);
        lyrics.sort(Comparator.comparingLong(LyricLine::getTimestamp));
    }

    private static long getLyricDuration(LyricLine line) {
        return line.words.isEmpty() ? 0 : line.words.getLast().timestamp;
    }

    /**
     * 更新当前歌词行
     * @param songProgress 歌曲进度 (ms)
     */
    public static void updateCurrentLyric(float songProgress) {
        LyricLine previousLyric = currentLyric;
        currentLyric = findCurrentLyric(songProgress);
        
        if (previousLyric != currentLyric) {
            resetLyricStatus();
        }
    }
    
    private static LyricLine findCurrentLyric(float songProgress) {
        for (int i = 0; i < lyrics.size(); i++) {
            LyricLine lyric = lyrics.get(i);
            
            if (lyric.getTimestamp() > songProgress) {
                return i > 0 ? lyrics.get(i - 1) : currentLyric;
            } else if (i == lyrics.size() - 1) {
                return lyrics.get(i);
            }
        }
        return currentLyric;
    }

    public static void resetLyricStatus() {
        lyrics.forEach(l -> {
            l.shouldUpdatePosition = false;
            l.delayTimer.reset();
            
            for (LyricLine.Word word : l.words) {
                Arrays.fill(word.emphasizes, 0);
            }
            
            l.markDirty();
        });
    }

    public static void setLyricsProgress(float progress) {
        if (lyrics.isEmpty()) return;
        
        try {
            resetLyricDisplayStates();
            updateCurrentLyric(progress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void resetLyricDisplayStates() {
        resetAllLyricsState();
        resetWordStates();
    }

    private static void resetAllLyricsState() {
        for (LyricLine lyric : lyrics) {
            lyric.scrollWidth = 0;
            lyric.offsetX = 0;
            lyric.offsetY = Double.MIN_VALUE;
            lyric.targetOffsetX = 0;
        }
    }

    private static void resetWordStates() {
        for (LyricLine lyric : lyrics) {
            for (LyricLine.Word word : lyric.words) {
                word.alpha = 0.0f;
                word.progress = 0.0;
            }
        }
    }

    public static String getSecondaryLyrics(LyricLine lyricLine) {
        if (hasTransLyrics) {
            return getTranslationOrRomanizationText(lyricLine);
        }
        
        if (hasRomanization) {
            return getRomanizationTextIfEnabled(lyricLine);
        }
        
        return "";
    }
    
    private static String getTranslationOrRomanizationText(LyricLine lyricLine) {
        boolean showRoman = WidgetsManager.musicLyrics.showRoman.getValue();
        
        if (!showRoman) {
            return StringUtils.returnEmptyStringIfNull(lyricLine.getTranslationText());
        }
        
        if (hasRomanization) {
            return StringUtils.returnEmptyStringIfNull(lyricLine.getRomanizationText());
        }
        
        return StringUtils.returnEmptyStringIfNull(lyricLine.getTranslationText());
    }
    
    private static String getRomanizationTextIfEnabled(LyricLine lyricLine) {
        if (WidgetsManager.musicLyrics.showRoman.getValue()) {
            return StringUtils.returnEmptyStringIfNull(lyricLine.getRomanizationText());
        }
        return "";
    }

    public static boolean hasSecondaryLyrics() {
        boolean hasAvailableLyrics = hasTransLyrics || hasRomanization;
        boolean showTranslationEnabled = WidgetsManager.musicLyrics.showTranslation.getValue();
        return hasAvailableLyrics && showTranslationEnabled;
    }

    @SneakyThrows
    public static void initNCM() {
        String cookie = getCookieFromFileOrOptions();
        
        if (cookie.isEmpty()) {
            ConsoleScreen.log("[NCM] Not logged in.");
        } else {
            loadNCM(cookie);
        }
    }

    @SneakyThrows
    private static String loadCookie() {
        if (!COOKIE_FILE.exists()) {
            return "";
        }
        
        List<String> cookieLines = Files.readAllLines(COOKIE_FILE.toPath());
        return cookieLines.isEmpty() ? "" : cookieLines.getFirst();
    }
    
    private static String getCookieFromFileOrOptions() {
        String cookie = loadCookie();
        return cookie.isEmpty() ? OptionsUtil.getCookie() : cookie;
    }

    public static void loadNCM(String cookie) {
        OptionsUtil.setCookie(cookie);
        // 获取用户信息
        profile = getUserProfile();

        if (profile == null) {
            return;
        }

        ConsoleScreen.log("[NCM] Logged in as {}({})", profile.getName(), profile.getId());

        if (!OptionsUtil.getCookie().isEmpty()) {
            // 保存一次
            onStop();
        }

        // 加载用户歌单
        CloudMusic.playLists = loadUserPlaylists();
        ConsoleScreen.log("[NCM] Loaded {} playlists", playLists.size());

        // 加载喜欢列表
        likeList = likeList();
        NCMScreen.getInstance().markDirty();
    }
    
    private static List<PlayList> loadUserPlaylists() {
        List<PlayList> userPlaylists = new ArrayList<>();
        int page = 0;

        while (true) {
            List<PlayList> pagePlaylists = fetchPlaylistsPage(page);
            
            if (pagePlaylists.isEmpty()) {
                break;
            }
            
            userPlaylists.addAll(pagePlaylists);
            page++;
        }
        
        return userPlaylists;
    }
    
    private static List<PlayList> fetchPlaylistsPage(int page) {
        try {
            return profile.playLists(page, 30);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SneakyThrows
    public static void onStop() {

        Files.write(COOKIE_FILE.toPath(), OptionsUtil.getCookie().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

    }

    @Getter
    public enum PlayMode {
        Random("F"),
        LoopInList("I"),
        LoopSingle("L"),
        Sequential("G");

        private final String icon;

        PlayMode(String icon) {
            this.icon = icon;
        }
    }

    // 以下这些 上一首/下一首 以及 播放/暂停 的逻辑我写完我自己都看不懂
    // BUT IT WORKS
    public static volatile boolean dontAdd = false;

    public static void prev() {
        updatePlayCountIfNeeded();
        
        if (!canPlayPrevious()) {
            return;
        }
        
        if (player != null && !playList.isEmpty()) {
            prepareForTrackChange();
            curIdx--;
            stopCurrentPlayback();
        }
    }
    
    private static boolean canPlayPrevious() {
        if (curIdx - 1 >= 0) {
            return true;
        }
        
        if (playMode == PlayMode.LoopInList) {
            curIdx = playList.size();
            return true;
        } else if (playMode == PlayMode.LoopSingle) {
            curIdx++;
            return true;
        }
        
        return false;
    }

    public static void next() {
        if (!canPlayNext()) {
            return;
        }
        
        if (player != null && !playList.isEmpty()) {
            updatePlayCountIfNeeded();
            prepareForTrackChange();
            curIdx++;
            stopCurrentPlayback();
        }
    }
    
    private static boolean canPlayNext() {
        if (curIdx + 1 > playList.size() - 1 && playMode == PlayMode.Sequential) {
            return false;
        }
        return true;
    }

    /**
     * 给网易云发送当前歌曲的播放时长
     */
    private static void updatePlayCountIfNeeded() {
        if (playedFrom != null && player != null) {
            playList.get(curIdx).updPlayCount(playedFrom, player.getCurrentTimeSeconds());
        }
    }
    
    private static void prepareForTrackChange() {
        dontAdd = true;
    }
    
    private static void stopCurrentPlayback() {
        player.close();
        playing.set(false);
    }

    /**
     * 播放来源, 用于记录播放时长
     */
    public static PlayList playedFrom = null;

    /**
     * 播放给定的列表中的所有歌曲
     * @param songs 歌曲列表
     * @param startIdx 第一首播放的索引
     */
    @SneakyThrows
    public static void play(List<Music> songs, int startIdx) {
        // 深拷贝一份以避免打乱的时候影响传进来的列表的顺序
        List<Music> safeSongList = new ArrayList<>(songs);
        
        stopExistingPlayThread();
        
        if (playMode == PlayMode.Random) {
            // 打乱列表以及开始索引
            startIdx = handleRandomPlayMode(safeSongList, startIdx);
        }
        
        startIdx = normalizeStartIndex(startIdx);
        loadMusicCover(safeSongList.getFirst());
        
        playList = safeSongList;
        startNewPlayThread(safeSongList, startIdx);
    }
    
    private static void stopExistingPlayThread() throws InterruptedException {
        if (playThread != null) {
            doBreak = true;
            playing.set(false);
            playThread.interrupt();
            playThread.join();
        }
    }
    
    private static int handleRandomPlayMode(List<Music> songs, int startIdx) {
        if (startIdx == -1) {
            Collections.shuffle(songs);
        } else {
            Music selectedMusic = songs.get(startIdx);
            Collections.shuffle(songs);
            startIdx = songs.indexOf(selectedMusic);
        }
        return startIdx;
    }
    
    private static int normalizeStartIndex(int startIdx) {
        return startIdx == -1 ? 0 : startIdx;
    }
    
    private static void startNewPlayThread(List<Music> songs, int startIdx) {
        playThread = new PlayThread(songs, startIdx);
        doBreak = false;
        playing.set(false);
        playThread.start();
    }

    static volatile boolean doBreak = false;

    static AtomicBoolean playing = new AtomicBoolean(true);

    private static class PlayThread extends Thread {
        private final List<Music> songs;
        private final int startIdx;

        public PlayThread(List<Music> songs, int startIdx) {
            this.songs = songs;
            this.setName("Play Thread");
            this.startIdx = startIdx;
        }

        @Override
        public void run() {
            curIdx = startIdx;

            while (shouldContinuePlayback()) {
                if (playListChanged()) {
                    break;
                }

                Music currentSong = playList.get(curIdx);
                prepareForPlayback();
                
                if (!playSong(currentSong)) {
                    break;
                }
                
                preloadNextCover();
                waitForPlaybackCompletion();
                handlePlaybackCompletion();
                updateCurrentIndex();
            }
        }
        
        private boolean shouldContinuePlayback() {
            return curIdx < playList.size() && !doBreak && !this.isInterrupted();
        }
        
        private boolean playListChanged() {
            return playList != songs;
        }
        
        private void prepareForPlayback() {
            stopPreviousPlayer();
            loadMusicCover(playList.get(curIdx));
        }
        
        private boolean playSong(Music song) {
            loadLyric(song);
            currentlyPlaying = song;
            
            Tuple<String, String> playUrl = song.getPlayUrl();
            
            if (playUrl == null) {
                handleUnplayableSong(song);
                return false;
            }
            
            return initializeAndPlaySong(song, playUrl);
        }
        
        private boolean initializeAndPlaySong(Music song, Tuple<String, String> playUrl) {
            WidgetsManager.musicInfo.downloading = false;
            File musicFile = getMusicFile(playUrl, song);
            
            try {
                player = initializePlayer(musicFile);
            } catch (Exception e) {
                handlePlayerInitializationError(e);
                return false;
            }
            
            notifySongStart(song);
            startPlayback(song, playUrl, musicFile);
            return true;
        }
        
        private void waitForPlaybackCompletion() {
            while (playing.get()) {
                if (this.isInterrupted() || doBreak) {
                    break;
                }
                
                CloudMusic.updateCurrentLyric(player.getCurrentTimeMillis());
                
                try {
                    player.doDetections();
                    Thread.sleep(10L);
                } catch (Exception e) {
                    // Ignore interruption exceptions during playback
                }
            }
        }
        
        private void handlePlaybackCompletion() {
            if (!dontAdd && playedFrom != null) {
                playList.get(curIdx).updPlayCount(playedFrom, player.getCurrentTimeSeconds());
            }
            
            player.close();
        }
        
        private void stopPreviousPlayer() {
            if (player != null && !player.isFinished()) {
                player.close();
                sleep(250);
            }
        }
        
        private void handleUnplayableSong(Music song) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(EnumChatFormatting.RED + "无法播放: " + song.getName() + " - " + song.getArtistsName());
            }
            ConsoleScreen.log("{}无法播放: {} - {}, 可能因为该歌曲没有版权", EnumChatFormatting.RED, song.getName(), song.getArtistsName());
        }
        
        private void handlePlayerInitializationError(Exception e) {
            e.printStackTrace();
            ConsoleScreen.log(EnumChatFormatting.RED + "[NCM] Failed to initiate audio player! Error: {}", e.getMessage());
        }
        
        private void notifySongStart(Music song) {
            MusicToast.pushMusicToast(song.getArtistsName() + " - " + song.getName());
            ConsoleScreen.log("[NCM] Now playing: {}, id {}", song.getName(), song.getId());
        }
        
        private void startPlayback(Music song, Tuple<String, String> playUrl, File musicFile) {
            try {
                player.play();
            } catch (ChannelMismatchException e) {
                player.player.cleanUp();
                musicFile.delete();
                player = initializePlayer(getMusicFile(playUrl, song));
            }
            playing.set(true);
            
            player.setAfterPlayed(() -> {
                this.notifyWaitLock();
                playing.set(false);
            });
        }
        
        private void preloadNextCover() {
            if (curIdx + 1 < playList.size()) {
                loadMusicCover(playList.get(curIdx + 1));
            }
        }
        
        private void updateCurrentIndex() {
            updateCurIdx();
        }

        private File getMusicFile(Tuple<String, String> playUrl, Music song) {

            String url = playUrl.getA();
            String type = playUrl.getB().toLowerCase();

            if (type.equals("flac") || type.equals("wav") || type.equals("mp3")) {
                return getCachedOrTempFile(url, type, song);
            }
            throw new IllegalArgumentException("Unsupported music format, url: " + url + ", type: " + type);
        }

        private File getCachedOrTempFile(String playUrl, String type, Music song) {
            File musicCacheDir = new File(ConfigManager.configDir, "MusicCache");

            if (!musicCacheDir.exists()) {
                musicCacheDir.mkdir();
            }

            String extension = "_" + quality.getQuality() + "." + type;

            File music = new File(musicCacheDir, song.getId() + extension);

            if (!music.exists()) {
                downloadMusic(playUrl, music);

                // delete all other qualities

                MultiThreadingUtil.runAsync(() -> {

                    for (File file : musicCacheDir.listFiles()) {

                        if (file.getName().startsWith(String.valueOf(song.getId())) && !file.getName().startsWith(song.getId() + "_" + quality.getQuality())) {
                            file.delete();
                        }

                    }

                });
            }

            return music;
        }

        private AudioPlayer initializePlayer(File musicFile) {
            AudioPlayer player = CloudMusic.player;
            if (player == null) {
                player = new AudioPlayer(musicFile);
//                player.volume = 0.25f;
                player.setVolume(WidgetsManager.musicInfo.volume.getValue().floatValue());
                CloudMusic.player = player;
            } else {
                player.setAudio(musicFile);
            }
            return player;
        }

        private void notifyWaitLock() {
            playing.set(false);
        }

        private void loadMusicCover(Music song) {
            CloudMusic.loadMusicCover(song);
        }

        PlayMode lastMode = playMode;

        private void updateCurIdx() {

            if (lastMode != playMode) {

                if (playMode == PlayMode.Random) {
                    Collections.shuffle(songs);
                    playList = songs;
                }

                lastMode = playMode;
            }

            if (playMode == PlayMode.LoopSingle) {
                if (dontAdd) {
                    dontAdd = false;
                }

                if (curIdx < 0) {
                    curIdx = 0;
                }
            } else if (playMode == PlayMode.LoopInList || playMode == PlayMode.Random) {
                if (!dontAdd) {
                    curIdx++;
                } else {
                    dontAdd = false;
                }

                if (curIdx == playList.size()) {
                    curIdx = 0;
                }
            } else {
                if (!dontAdd) {
                    curIdx++;
                } else {
                    dontAdd = false;
                }
            }
        }

        private void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void loadMusicCover(Music music) {
        loadMusicCover(music, false);
    }

    public static void loadMusicCover(Music music, boolean forceReload) {
        Location musicCover = music.getCoverLocation();
        Location musicCoverSmall = music.getSmallCoverLocation();
        Location musicCoverBlur = music.getBlurredCoverLocation();
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        if (shouldLoadCover(textureManager, musicCover, forceReload)) {
            loadMainCoverAsync(music, musicCover, musicCoverBlur);
        }

        if (shouldLoadCover(textureManager, musicCoverSmall, forceReload)) {
            loadSmallCoverAsync(music, musicCoverSmall);
        }
    }
    
    private static boolean shouldLoadCover(TextureManager textureManager, Location coverLocation, boolean forceReload) {
        return textureManager.getTexture(coverLocation) == null || forceReload;
    }
    
    private static void loadMainCoverAsync(Music music, Location musicCover, Location musicCoverBlur) {
        MultiThreadingUtil.runAsync(() -> {
            try {
                @Cleanup
                InputStream coverStream = HttpUtils.downloadStream(music.getCoverUrl(320), 5);
                byte[] imageData = IOUtils.toByteArray(coverStream);
                
                BufferedImage coverImage = NativeBackedImage.make(new ByteArrayInputStream(imageData));
                
                if (coverImage != null) {
                    loadCoverTextures(coverImage, musicCover, musicCoverBlur);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    private static void loadCoverTextures(BufferedImage coverImage, Location musicCover, Location musicCoverBlur) {
        Textures.loadTexture(musicCover, coverImage);
        
        MultiThreadingUtil.runAsync(() -> {
            BufferedImage inputImage = new BufferedImage(coverImage.getWidth(), coverImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            inputImage.setRGB(0, 0, coverImage.getWidth(), coverImage.getHeight(), 
                             coverImage.getRGB(0, 0, coverImage.getWidth(), coverImage.getHeight(), null, 0, coverImage.getWidth()), 
                             0, coverImage.getWidth());

            // 创建高斯模糊之后的歌曲封面, 目前仅在播放器的歌词界面使用
            BufferedImage blurredImage = gaussianBlur(inputImage, 31);
            Textures.loadTexture(musicCoverBlur, blurredImage);
        });
    }
    
    private static void loadSmallCoverAsync(Music music, Location musicCoverSmall) {
        MultiThreadingUtil.runAsync(() -> {
            InputStream smallCoverStream = HttpUtils.downloadStream(music.getCoverUrl(128), 5);
            BufferedImage smallCoverImage = NativeBackedImage.make(smallCoverStream);
            Textures.loadTexture(musicCoverSmall, smallCoverImage);
        });
    }

    private static final Kernel GAUSSIAN_KERNEL = new Kernel(41, 41, GaussianKernel.generate(41));

    public static BufferedImage gaussianBlur(BufferedImage imgIn, int blur) {
        Map<RenderingHints.Key, Object> map = new HashMap<>();
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        RenderingHints hints = new RenderingHints(map);

        ConvolveOp op = new ConvolveOp(GAUSSIAN_KERNEL, ConvolveOp.EDGE_NO_OP, hints);

        BufferedImage filtered = op.filter(imgIn, null);

        BufferedImage output = new BufferedImage(filtered.getWidth(), filtered.getHeight(), filtered.getType());
        Graphics2D graphics = (Graphics2D) output.getGraphics();
        graphics.setRenderingHints(map);
        graphics.drawImage(filtered, -blur, -blur, filtered.getWidth() + blur * 2, filtered.getHeight() + blur * 2, null);

        return output;
    }

    @SneakyThrows
    private static File convertFlacToWav(File flacIn, File destFile) {

        @Cleanup
        FileOutputStream os = new FileOutputStream(destFile);

        WavWriter ww = new WavWriter(os);

        FLACDecoder fd = new FLACDecoder(Files.newInputStream(flacIn.toPath()));
        fd.addPCMProcessor(new PCMProcessor() {
            @Override
            public void processStreamInfo(StreamInfo info) {
                try {
                    ww.writeHeader(info);
                } catch (IOException e) {
                    e.printStackTrace();
                    WidgetsManager.musicInfo.downloading = false;
                    destFile.delete();
                }
            }

            @Override
            public void processPCM(ByteData pcm) {
                try {
                    ww.writePCM(pcm);
                } catch (IOException e) {
                    e.printStackTrace();
                    WidgetsManager.musicInfo.downloading = false;
                    destFile.delete();
                }
            }
        });
        fd.decode();

        return destFile;
    }

    @SneakyThrows
    private static File convertMp3ToWav(File mp3In, File destFile) {

        Converter converter = new Converter();
        converter.convert(Files.newInputStream(mp3In.toPath()), destFile.getAbsolutePath(), null, null);

        return destFile;
    }

    @SneakyThrows
    private static void downloadMusic(String playUrl, File music) {

        WidgetsManager.musicInfo.downloading = NCMScreen.getInstance().downloading = true;
        WidgetsManager.musicInfo.downloadProgress = NCMScreen.getInstance().downloadProgress = 0;
        WidgetsManager.musicInfo.downloadSpeed = NCMScreen.getInstance().downloadSpeed = "0 b/s";

        try {
            InputStream stream = new WrappedInputStream(HttpUtils.get(playUrl, null), new WrappedInputStream.ProgressListener() {

                tritium.utils.timing.Timer timer = new tritium.utils.timing.Timer();

                @Override
                public void onProgress(double progress) {
                    if (progress >= 1) {
                        WidgetsManager.musicInfo.downloading = NCMScreen.getInstance().downloading = false;
                    }

                    WidgetsManager.musicInfo.downloadProgress = NCMScreen.getInstance().downloadProgress = progress;
                }

                final long kilo = 1024;
                final long mega = kilo * kilo;
                final long giga = mega * kilo;
                final long tera = giga * kilo;

                String getSize(long size) {
                    String s;
                    double kb = (double)size / kilo;
                    double mb = kb / kilo;
                    double gb = mb / kilo;
                    double tb = gb / kilo;
                    if(size < kilo) {
                        s = size + " Bytes";
                    } else if(size < mega) {
                        s =  String.format("%.2f", kb) + " KB";
                    } else if(size < giga) {
                        s = String.format("%.2f", mb) + " MB";
                    } else if(size < tera) {
                        s = String.format("%.2f", gb) + " GB";
                    } else {
                        s = String.format("%.2f", tb) + " TB";
                    }
                    return s;
                }

                int lastBytesRead = 0;

                @Override
                public void bytesRead(int bytesRead) {

                    int checkDelay = 500;

                    if (timer.isDelayed(checkDelay)) {
                        timer.reset();

                        int diff = (bytesRead - lastBytesRead) * (1000 / checkDelay);

                        WidgetsManager.musicInfo.downloadSpeed = NCMScreen.getInstance().downloadSpeed = this.getSize(diff) + "/s";

                        lastBytesRead = bytesRead;
                    }

                }
            });

            OutputStream os = Files.newOutputStream(music.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            writeTo(stream, os);

            os.close();

        } catch (Throwable t) {
            t.printStackTrace();

            WidgetsManager.musicInfo.downloading = NCMScreen.getInstance().downloading = false;

            music.delete();
        }

//        NotificationManager.show("Cloud Music", "Decoded flac => wav", Notification.Type.INFO, 2000);
    }

    @SneakyThrows
    public static void writeTo(InputStream src, OutputStream dest) {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = src.read(buffer)) != -1) {
            dest.write(buffer, 0, len);
        }
        dest.flush();
    }

    public static void loadLyric(Music music) {
        MultiThreadingUtil.runAsync(() -> {

            String string = CloudMusicApi.lyricNew(music.getId()).toString();

            string = string.replaceAll("[ - ]", " ");

            JsonObject json = JsonUtils.toJsonObject(string);

            List<LyricLine> parsed = LyricParser.parse(json);

            InputStream stream = MusicLyricsPanel.class.getResourceAsStream("/assets/minecraft/tritium/yrc/" + music.getId() + ".yrc");
            if (stream != null) {
                try {
                    String s = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                    List<LyricLine> newLines = new ArrayList<>();
                    LyricParser.parseYrc(s, newLines);

                    for (int i = 0; i < newLines.size(); i++) {
                        LyricLine newLine = newLines.get(i);
                        LyricLine oldLine = parsed.get(i);
                        oldLine.words.clear();
                        oldLine.words.addAll(newLine.words);
                        oldLine.timestamp = newLine.timestamp;
                        oldLine.lyric = newLine.lyric;
                    }

                    stream.close();
                } catch (IOException ignored) {
                }
            }

            // 使用集中式歌词管理
            initLyrics(json, music, parsed);

        });
    }

    public static String qrCodeLogin() {
        String key = CloudMusic.qrKey();

        QRCodeGenerator.generateAndLoadTexture("https://music.163.com/login?codekey=" + key);

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                return "";
            }

//            HttpClient.HttpResult result = qrCheck(key);
            JsonObject json = CloudMusicApi.loginQrCheck(key).toJsonObject();

            int code = json.get("code").getAsInt();
            if (code == 800) {
                key = CloudMusic.qrKey();

                QRCodeGenerator.generateAndLoadTexture("https://music.163.com/login?codekey=" + key);
            }

            if (code == 802) {
                if (json.has("nickname")) {
                    NCMScreen.getInstance().loginRenderer.tempUsername = json.get("nickname").getAsString();
                }

                if (json.has("avatarUrl")) {
                    String url = json.get("avatarUrl").getAsString();

                    if (!NCMScreen.getInstance().loginRenderer.avatarLoaded) {
                        NCMScreen.getInstance().loginRenderer.avatarLoaded = true;
                        MultiThreadingUtil.runAsync(() -> {
                            try (InputStream is = HttpUtils.get(url, null)) {
                                BufferedImage img = NativeBackedImage.make(is);

                                Textures.loadTextureAsyncly(NCMScreen.getInstance().loginRenderer.tempAvatar, img);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            if (code == 803) {

                String cookie = json.get("cookie").getAsString();

                String[] split = cookie.split(";");
                StringBuilder sb = new StringBuilder();
                for (String s : split) {
                    if (s.contains("MUSIC_U") || s.contains("__csrf")) {
                        sb.append(s).append("; ");
                    }
                }

                return sb.substring(0, sb.length() - 2);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static User getUserProfile() {
        JsonObject jsonObject = CloudMusicApi.loginStatus().toJsonObject();

        JsonObject d = jsonObject.getAsJsonObject("data");

        if ((!d.has("account") || d.get("account") instanceof JsonNull) || (!d.has("profile") || d.get("profile") instanceof JsonNull)) {
            OptionsUtil.setCookie("");
            return null;
        }

        JsonObject profile = d.getAsJsonObject("profile");

        return JsonUtils.parse(profile, User.class);
    }


    public static List<Music> search(String keyWord) {
        List<Music> searchResults = new ArrayList<>();
        JsonObject searchResponse = CloudMusicApi.cloudSearch(keyWord, CloudMusicApi.SearchType.Single).toJsonObject();
        
        JsonArray songs = extractSongsFromResponse(searchResponse);
        
        if (songs != null) {
            for (JsonElement song : songs) {
                searchResults.add(JsonUtils.parse(song.getAsJsonObject(), Music.class));
            }
        }
        
        return searchResults;
    }
    
    private static JsonArray extractSongsFromResponse(JsonObject searchResponse) {
        try {
            JsonObject result = searchResponse.getAsJsonObject("result");
            return result != null ? result.getAsJsonArray("songs") : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse search response", e);
        }
    }

    public static List<Long> likeList() {
        List<Long> list = new ArrayList<>();

        JsonObject json = CloudMusicApi.likeList(profile.getId()).toJsonObject();

        JsonArray ids = json.getAsJsonArray("ids");
        for (JsonElement id : ids) {
            list.add(id.getAsLong());
        }

        return list;
    }

    public static String qrKey() {
        JsonObject json = CloudMusicApi.loginQrKey().toJsonObject();
        return json.getAsJsonObject("data").get("unikey").getAsString();
    }

    static {
        // 设置播放器播放速率, (0, +∞)
        CommandManager.registerCommand("set_player_rate", (Float rate) -> {
            if (player == null) {
                ConsoleScreen.log("{}Not playing!", EnumChatFormatting.RED);
                return;
            }

            if (rate <= 0) {
                ConsoleScreen.log("{}Invalid rate! Rate should be greater than 0", EnumChatFormatting.RED);
                return;
            }

            float prevRate = player.player.getRate();
            player.player.rate(rate);
            ConsoleScreen.log("set rate: {} => {}", prevRate, rate);
        }, Float.class, "rate").setDescription("Set audio player's playback rate");

        // 设置播放器音量, [0, 1]
        CommandManager.registerCommand("set_player_volume", (Float volume) -> {
            if (player == null) {
                ConsoleScreen.log("{}Not playing!", EnumChatFormatting.RED);
                return;
            }

            float prevVol = player.getVolume();
            player.setVolume(Math.max(0, Math.min(1, volume)));
            ConsoleScreen.log("set volume: {} => {}", prevVol, player.getVolume());
        }, Float.class, "volume").setDescription("Set audio player's volume");

        // 播放歌曲, 传入网易云音乐歌曲id
        CommandManager.registerCommand("play", (Long id) -> MultiThreadingUtil.runAsync(() -> {
            JsonArray songs = CloudMusicApi.songDetail(id).toJsonObject().getAsJsonArray("songs");
            if (songs.isEmpty()) {
                ConsoleScreen.log("{}No such song: {}", EnumChatFormatting.RED, id);
                return;
            }

            play(Collections.singletonList(JsonUtils.parse(songs.get(0), Music.class)), 0);
        }), Long.class, "music id").setDescription("Play the song with the given id");
    }

}
