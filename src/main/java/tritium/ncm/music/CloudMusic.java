package tritium.ncm.music;

import com.google.gson.*;
import com.jsyn.exceptions.ChannelMismatchException;
import javazoom.jl.converter.Converter;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import org.kc7bfi.jflac.util.WavWriter;
import tritium.ncm.OptionsUtil;
import tritium.ncm.api.CloudMusicApi;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.ncm.music.dto.User;
import tritium.management.ConfigManager;
import tritium.management.WidgetsManager;
import tritium.rendering.GaussianKernel;
import tritium.rendering.texture.Textures;
import tritium.screens.ncm.FuckPussyPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.json.JsonUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.WrappedInputStream;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.widget.impl.MusicInfoWidget;
import tritium.widget.impl.MusicLyricsWidget;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
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

    public static final File COOKIE_FILE = new File(ConfigManager.configDir, "NCMCookie.txt");

    @SneakyThrows
    public static void initNCM() {
        String s = loadCookie();

        if (s.isEmpty()) {
            s = OptionsUtil.getCookie();
        }

        if (!s.isEmpty()) {
            loadNCM(s);
        }

    }

    @SneakyThrows
    private static String loadCookie() {

        if (!COOKIE_FILE.exists()) {
            return "";
        }

        List<String> strings = Files.readAllLines(COOKIE_FILE.toPath());

        if (strings.isEmpty())
            return "";

        return strings.get(0);

    }

    public static void loadNCM(String cookie) {
        OptionsUtil.setCookie(cookie);
        profile = getUserProfile();

        if (profile == null)
            return;

        if (!OptionsUtil.getCookie().isEmpty()) {
            onStop();
        }

        List<PlayList> playLists = new ArrayList<>();

        int page = 0;

        while (true) {

            List<PlayList> pl;

            try {
                pl = profile.playLists(page, 30);

                if (pl.isEmpty())
                    break;

                playLists.addAll(pl);
            } catch (Exception e) {

            }

            page += 1;
        }

        CloudMusic.playLists = playLists;

        if (!playLists.isEmpty()) {
            // FIXME
//            MusicScreen.selectedList = playLists.get(0);
        }

        likeList = likeList();
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

    private static final DecimalFormat df = new DecimalFormat("##.##");

    public static volatile boolean dontAdd = false;

    public static void prev() {

        if (playedFrom != null) {
            playList.get(curIdx).updPlayCount(playedFrom, player.getCurrentTimeSeconds());
        }

        if (curIdx - 1 < 0) {

            if (playMode == PlayMode.LoopInList) {
                curIdx = playList.size();
            } else if (playMode == PlayMode.LoopSingle) {
                curIdx++;
            } else {
                return;
            }
        }

        if (player != null && !playList.isEmpty()) {

            curIdx--;

            player.close();

            dontAdd = true;
            playing.set(false);
        }
    }

    public static void next() {
        if (curIdx + 1 > playList.size() - 1 && playMode == PlayMode.Sequential)
            return;

        if (player != null && !playList.isEmpty()) {
            player.close();

            if (playedFrom != null) {
                playList.get(curIdx).updPlayCount(playedFrom, player.getCurrentTimeSeconds());
            }

            curIdx ++;

            dontAdd = true;
            playing.set(false);
        }
    }

    public static PlayList playedFrom = null;

    @SneakyThrows
    public static void play(List<Music> songs, int startIdx) {

        // avoid modifying the list
        songs = new ArrayList<>(songs);

        if (playThread != null) {
            doBreak = true;
            playing.set(false);
            playThread.interrupt();
            playThread.join();
        }

        if (playMode == PlayMode.Random) {
            if (startIdx == -1) {
                Collections.shuffle(songs);
            } else {
                Music music = songs.get(startIdx);
                Collections.shuffle(songs);
                startIdx = songs.indexOf(music);
            }
        }

        loadMusicCover(songs.get(0));

        if (startIdx == -1) {
            startIdx = 0;
        }

        MusicLyricsWidget.allLyrics.clear();
        MusicLyricsWidget.timings.clear();
        playList = songs;

        playThread = new PlayThread(songs, startIdx);
        doBreak = false;
        MusicLyricsWidget.allLyrics.clear();
        MusicLyricsWidget.currentDisplaying = null;
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

            mainCycle:
            while (curIdx < playList.size() && !doBreak && !this.isInterrupted()) {
                Music song = playList.get(curIdx);

                if (playList != songs) {
                    break;
                }

                if (player != null && !player.isFinished()) {
                    player.close();
                    sleep(250);
                }

                loadMusicCover(playList.get(curIdx));

                loadLyric(song);

                currentlyPlaying = song;

                Tuple<String, String> playUrl = song.getPlayUrl();

                WidgetsManager.musicInfo.downloading = false;
                File musicFile = getMusicFile(playUrl, song);

                try {
                    player = initializePlayer(musicFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

//                OpenApiInstance.api.popNotification(EnumNotificationType.INFO, "Now Playing", song.getName(), 1500);

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

                // load next music's cover
                if (curIdx + 1 < playList.size()) {
                    loadMusicCover(playList.get(curIdx + 1));
                }

                while (playing.get()) {

                    if (this.isInterrupted() || doBreak)
                        break mainCycle;

                    try {
                        player.doDetections();
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                }

                if (!dontAdd) {
                    if (playedFrom != null) {
                        playList.get(curIdx).updPlayCount(playedFrom, player.getCurrentTimeSeconds());
                    }
                }

                player.close();

                MusicLyricsWidget.allLyrics.clear();
                MusicLyricsWidget.timings.clear();

                updateCurIdx();

//                if (curIdx < playList.size()) {
//                    loadMusicCover(playList.get(curIdx));
//                }
            }
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
            File musicCacheDir = new File("MusicCache");

            if (!musicCacheDir.exists()) {
                musicCacheDir.mkdir();
            }

            File convertedDir = new File(musicCacheDir, "Converted");
            if (!convertedDir.exists()) {
                convertedDir.mkdir();
            }

            String extension = "_" + quality.getQuality() + "." + type;

            boolean isFlac = type.equals("flac");
            boolean isMp3 = type.equals("mp3");

            File music = new File(musicCacheDir, song.getId() + extension);

            File converted = new File(convertedDir, song.getId() + ".wav");

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
            } else {

                if (isFlac) {
                    return music;
//                return convertFlacToWav(music, converted);
                }

                if (converted.exists()) {
                    return converted;
                }
            }

            if (isMp3) {
                return convertMp3ToWav(music, converted);
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
            if (!song.textureLoaded) {
                song.textureLoaded = true;
                CloudMusic.loadMusicCover(song);
            }
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
        CloudMusic.loadMusicCover(music, false);
    }

    public static void loadMusicCover(Music music, boolean forceReload) {

        Location musicCover = MusicInfoWidget.getMusicCover(music);
        Location musicCoverSmall = MusicInfoWidget.getMusicCoverSmall(music);
        Location musicCoverBlur = MusicInfoWidget.getMusicCoverBlurred(music);
        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(musicCover);

        if (texture != null && !forceReload)
            return;

        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {

                @Cleanup
                InputStream is = HttpUtils.downloadStream(music.getPicUrl(320), 5);
                InputStream isSmall = HttpUtils.downloadStream(music.getPicUrl(64), 5);

                // 此处无法使用 NativeBackedImage, 底下那个 gaussianBlur 需要很多 ImageIO 狗屎才能工作
                BufferedImage read = ImageIO.read(is);
                Textures.loadTextureAsyncly(musicCover, read);

                BufferedImage readSmall = NativeBackedImage.make(isSmall);
                Textures.loadTextureAsyncly(musicCoverSmall, readSmall);

                MultiThreadingUtil.runAsync(() -> {
                    BufferedImage blured = gaussianBlur(read, 31);
                    Textures.loadTextureAsyncly(musicCoverBlur, blured);
                });
            }
        });
    }

    public static void loadMusicCover(Music music, boolean forceReload, int size, Location location) {
        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(location);

                if (texture != null && !forceReload)
                    return;

                @Cleanup
                InputStream inputStream = HttpUtils.downloadStream(music.getPicUrl(size), 5);

                Textures.loadTextureAsyncly(location, NativeBackedImage.make(inputStream));
            }
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

        boolean enabled = WidgetsManager.musicInfo.isEnabled();

        if (enabled) {
            WidgetsManager.musicInfo.downloading = true;
            WidgetsManager.musicInfo.downloadProgress = 0;
            WidgetsManager.musicInfo.downloadSpeed = "0 b/s";
        }

        try {

            InputStream stream = new WrappedInputStream(HttpUtils.get(playUrl, null), new WrappedInputStream.ProgressListener() {

                tritium.utils.timing.Timer timer = new tritium.utils.timing.Timer();

                @Override
                public void onProgress(double progress) {
                    if (enabled) {

                        if (progress >= 1) {
                            WidgetsManager.musicInfo.downloading = false;
                        }

                        WidgetsManager.musicInfo.downloadProgress = progress;
                    }
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

                        WidgetsManager.musicInfo.downloadSpeed = this.getSize(diff) + "/s";

                        lastBytesRead = bytesRead;
                    }

                }
            });

            OutputStream os = Files.newOutputStream(music.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            writeTo(stream, os);

            os.close();

        } catch (Throwable t) {
            t.printStackTrace();

            if (enabled) {
                WidgetsManager.musicInfo.downloading = false;
            }

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

    public static void loadLyric(Music song) {
        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {

                String string = CloudMusicApi.lyricNew(song.getId()).toString();

                string = string.replaceAll("[ - ]", " ");

                JsonObject json = JsonUtils.jsonObjectFromString(string);

                MusicLyricsWidget.initLyric(json, song);
                FuckPussyPanel.initLyric(json, song);

            }
        });
    }

    public static String qrCodeLogin() {
        String key = CloudMusic.qrKey();

        QRCodeGenerator.generateAndLoadTexture("https://music.163.com/login?codekey=" + key);

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                System.out.println("! Interrupted login thread");
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
                        MultiThreadingUtil.runAsync(new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {
                                try (InputStream is = HttpUtils.get(url, null)) {
                                    BufferedImage img = NativeBackedImage.make(is);

                                    Textures.loadTextureAsyncly(NCMScreen.getInstance().loginRenderer.tempAvatar, img);

                                }
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

    static Gson gson = new Gson();

    public static User getUserProfile() {

        JsonObject jsonObject = CloudMusicApi.loginStatus().toJsonObject();

        JsonObject d = jsonObject.getAsJsonObject("data");

        if ((!d.has("account") || d.get("account") instanceof JsonNull) || (!d.has("profile") || d.get("profile") instanceof JsonNull)) {
            OptionsUtil.setCookie("");
            return null;
        }

        JsonObject profile = d.getAsJsonObject("profile");

//        DebugUtils.debugThis(profile);

        return new User(profile);
    }


    public static List<Music> search(String keyWord) {
        List<Music> list = new ArrayList<>();

        JsonObject json = CloudMusicApi.cloudSearch(keyWord, CloudMusicApi.SearchType.Single).toJsonObject();

        JsonArray songs = null;
        try {
            JsonObject result = json.getAsJsonObject("result");
            if (result != null) {
                songs = result.getAsJsonArray("songs");

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (songs != null) {
            for (JsonElement song : songs) {
                Music m = new Music(song.getAsJsonObject(), null);
                list.add(m);
            }
        }
//        JsonObject data = post.toJson();


        return list;
    }

    public static List<Long> likeList() {
        List<Long> list = new ArrayList<>();

        JsonObject json = CloudMusicApi.likeList(profile.id).toJsonObject();

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

}
