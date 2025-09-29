package tech.konata.phosphate.utils.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.jsyn.exceptions.ChannelMismatchException;
import javazoom.jl.converter.Converter;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import org.kc7bfi.jflac.util.WavWriter;
import tech.konata.ncm.OptionsUtil;
import tech.konata.ncm.api.CloudMusicApi;
import tech.konata.phosphate.management.ConfigManager;
import tech.konata.phosphate.rendering.GaussianKernel;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.utils.music.dto.PlayList;
import tech.konata.phosphate.utils.music.dto.User;
import tech.konata.phosphate.rendering.MMCQ;
import tech.konata.phosphate.rendering.notification.Notification;
import tech.konata.phosphate.rendering.notification.NotificationCurrentPlaying;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.texture.Textures;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.panels.MusicPanel;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.utils.audio.AudioPlayer;
import tech.konata.phosphate.utils.json.JsonUtils;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.network.HttpClient;
import tech.konata.phosphate.utils.network.HttpUtils;
import tech.konata.phosphate.utils.other.WrappedInputStream;
import tech.konata.phosphate.utils.res.InputStreams;
import tech.konata.phosphate.utils.timing.Timer;
import tech.konata.phosphate.widget.impl.MusicLyrics;
import tech.konata.phosphate.widget.impl.MusicWidget;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 9:34 AM
 */
public class CloudMusic {

    @Getter
    private static final Map<String, String> headers = new HashMap<>();
    public static AudioPlayer player;
    public static List<Music> playList = new ArrayList<>();
    public static int curIdx = 0;
    public static Music currentlyPlaying;
    public static Thread playThread;

    public static PlayMode playMode = PlayMode.Sequential;

    public static final Map<Long, ColorPlatte> avgColor = new HashMap<>();

    public static class ColorPlatte {

        public final Integer[] colors;
        final Timer timer = new Timer();

        public ColorPlatte(Integer[] colors) {
            this.colors = colors;
        }

        public int get(int index) {

            if (index > this.colors.length - 1) {
                return this.colors[0];
            }

            return this.colors[index];

        }

        int idx = 0;

        public int get() {

            if (timer.isDelayed(5000)) {
                timer.reset();
                idx ++;

                if (idx > this.colors.length - 1) {
                    idx = 0;
                }
            }

            return this.colors[idx];

        }

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

        MusicLyrics.allLyrics.clear();
        MusicLyrics.timings.clear();
        playList = songs;

        playThread = new PlayThread(songs, startIdx);
        doBreak = false;
        MusicLyrics.allLyrics.clear();
        MusicLyrics.currentDisplaying = null;
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

                Tuple<String, String> playUrl = song.getPlayUrl();

                File musicFile = getMusicFile(playUrl, song);

                try {
                    player = initializePlayer(musicFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

                currentlyPlaying = song;

                if (GlobalSettings.PLAY_NOTIFY.getValue()) {
                    new NotificationCurrentPlaying("Now Playing", song.getName(), Notification.Type.INFO, 4000, song).show();
                }
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

                while (playing.get()) {

                    if (this.isInterrupted() || doBreak)
                        break mainCycle;

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
//                    try {
//                        player.doDetections();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }

                if (!dontAdd) {
                    if (playedFrom != null) {
                        playList.get(curIdx).updPlayCount(playedFrom, player.getCurrentTimeSeconds());
                    }
                }

                player.close();

                MusicLyrics.allLyrics.clear();
                MusicLyrics.timings.clear();

                updateCurIdx();
                loadMusicCover(playList.get(curIdx));
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

            String extension = "_" + GlobalSettings.MUSIC_QUALITY.getValue().getQuality() + "." + type;

            boolean isFlac = type.equals("flac");
            boolean isMp3 = type.equals("mp3");

            File music = new File(musicCacheDir, song.getId() + extension);
            File converted = new File(convertedDir, song.getId() + ".wav");

            if (!music.exists()) {
                downloadMusic(playUrl, music);

                // delete all other qualities

                MultiThreadingUtil.runAsync(() -> {

                    for (File file : musicCacheDir.listFiles()) {

                        if (file.getName().startsWith(String.valueOf(song.getId())) && !file.getName().startsWith(song.getId() + "_" + GlobalSettings.MUSIC_QUALITY.getValue().getQuality())) {
                            file.delete();
                        }

                    }

                });
            } else {
                if (converted.exists()) {
                    return converted;
                }
            }

            System.out.println("Music Path: " + music.getAbsolutePath());

            if (isFlac) {
                return convertFlacToWav(music, converted);
            }

            if (isMp3) {
                return convertMp3ToWav(music, converted);
            }

            return music;
        }

        @SneakyThrows
        private static File convertFlacToWav(File flacIn, File destFile) {

            @Cleanup
            FileOutputStream os = new FileOutputStream(destFile);

            WavWriter ww = new WavWriter(os);

            FLACDecoder fd = new FLACDecoder(new FileInputStream(flacIn));
            fd.addPCMProcessor(new PCMProcessor() {
                @Override
                public void processStreamInfo(StreamInfo info) {
                    try {
                        ww.writeHeader(info);
                    } catch (IOException e) {
                        e.printStackTrace();
                        destFile.delete();
                    }
                }

                @Override
                public void processPCM(ByteData pcm) {
                    try {
                        ww.writePCM(pcm);
                    } catch (IOException e) {
                        e.printStackTrace();
                        destFile.delete();
                    }
                }
            });

            try {
                fd.decode();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

            return destFile;
        }

        @SneakyThrows
        private static File convertMp3ToWav(File mp3In, File destFile) {

            Converter converter = new Converter();
            converter.convert(Files.newInputStream(mp3In.toPath()), destFile.getAbsolutePath(), null, null);

            return destFile;
        }

        private AudioPlayer initializePlayer(File musicFile) {
            AudioPlayer player = CloudMusic.player;
            if (player == null) {
                player = new AudioPlayer(musicFile);
                player.volume = GlobalSettings.volume.getValue() / 100.0f;
//                player.setVolume(GlobalSettings.volume.getValue() / 100.0f);
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
            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(MusicWidget.getMusicCover(song));
            if (texture == null || texture == TextureUtil.missingTexture) {
                CloudMusic.loadMusicCover(song);
            }
        }

//        private void waitForNextSong() {
//            try {
//                synchronized (waitLock) {
//                    waitLock.wait();
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }

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
        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                Location musicCover = MusicWidget.getMusicCover(music);
                Location musicCoverBlur = MusicWidget.getMusicCoverBlurred(music);
                ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(musicCover);

                if (texture != null && texture != TextureUtil.missingTexture && !forceReload)
                    return;

                @Cleanup
                InputStream inputStream = HttpClient.downloadStream(music.getPicUrl(160), 5);

                BufferedImage img = ImageIO.read(inputStream);

                BufferedImage blured = gaussianBlur(img, 41);

                int pixelCount = img.getWidth() * img.getHeight();
                int rBucket = 0, gBucket = 0, bBucket = 0;

                for (int x = 0; x < img.getWidth(); x++) {
                    for (int y = 0; y < img.getHeight(); y++) {

                        int color = img.getRGB(x, y);

                        rBucket += (color >> 16) & 0xFF;
                        gBucket += (color >> 8) & 0xFF;
                        bBucket += (color & 0xFF);

                    }
                }

                try {
                    List<int[]> dominantColors = MMCQ.getDominantColors(img);
//                                System.out.println("Found " + dominantColors.size() + " dominant colors.");

                    List<Integer> toHex = new ArrayList<>();
                    for (int[] dominantColor : dominantColors) {
                        toHex.add(RenderSystem.hexColor(dominantColor[0], dominantColor[1], dominantColor[2]));
                    }

                    CloudMusic.ColorPlatte colorPlatte = new CloudMusic.ColorPlatte(toHex.toArray(new Integer[0]));
                    CloudMusic.avgColor.put(music.getId(), colorPlatte);
                } catch (ArrayIndexOutOfBoundsException aioo) {
                    CloudMusic.ColorPlatte colorPlatte = new CloudMusic.ColorPlatte(new Integer[]{ RenderSystem.hexColor(rBucket / pixelCount, gBucket / pixelCount, bBucket / pixelCount) });
                    CloudMusic.avgColor.put(music.getId(), colorPlatte);

                }

                Textures.loadTextureAsyncly(musicCover, img);
                Textures.loadTextureAsyncly(musicCoverBlur, blured);

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
    private static void downloadMusic(String playUrl, File music) {
        Notification not = new Notification("Cloud Music", "Downloading...", Notification.Type.INFO);
        not.show();
        try {

            InputStream stream = new WrappedInputStream(HttpUtils.get(playUrl, null), new WrappedInputStream.ProgressListener() {

                Timer timer = new Timer();

                @Override
                public void onProgress(double prog) {

                    if (prog >= 1) {
                        not.message = "Downloaded!";
                        not.type = Notification.Type.SUCCESS;
                        not.stayTime = 2000;
                        not.forever = false;
                        not.timer.reset();
                    } else {
                        not.progress = prog;
                    }

                }

                final long kilo = 1024;
                final long mega = kilo * kilo;
                final long giga = mega * kilo;
                final long tera = giga * kilo;

                String getSize(long size) {
                    String s = "";
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

                        not.message = "(" + this.getSize(diff) + "/s) Downloading...";

                        lastBytesRead = bytesRead;
                    }

                }
            });

            OutputStream os = Files.newOutputStream(music.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            InputStreams.writeTo(stream, os);

            os.close();

        } catch (Throwable t) {
            t.printStackTrace();
            not.message = "Failed to download!";
            not.stayTime = 10000L;
            not.forever = false;
            not.timer.reset();
            not.type = Notification.Type.ERROR;

            music.delete();
        }

//        NotificationManager.show("Cloud Music", "Decoded flac => wav", Notification.Type.INFO, 2000);
    }

    public static void loadLyric(Music song) {
        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {

                String string = CloudMusicApi.lyricNew(song.getId()).toString();

                string = string.replaceAll("[ - ]", " ");

                JsonObject json = JsonUtils.jsonObjectFromString(string);

                MusicLyrics.initLyric(json, song);

            }
        });
    }

    public static void initialize(String cookie) {

        if (cookie == null || cookie.isEmpty())
            return;

        OptionsUtil.setCookie(cookie);
        MusicPanel.profile = getUserProfile();

        if (MusicPanel.profile == null)
            return;

        List<PlayList> playLists = new ArrayList<>();

        int page = 0;

        while (true) {

            List<PlayList> pl;

            try {
                pl = MusicPanel.profile.playLists(page, 30);

                if (pl.isEmpty())
                    break;

                playLists.addAll(pl);
            } catch (Exception e) {

            }

            page += 1;
        }

        MusicPanel.playLists = playLists;
        MusicPanel.likeList = likeList();
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
                    ClickGui.getInstance().musicPanel.loginRenderer.tempUsername = json.get("nickname").getAsString();
                }

                if (json.has("avatarUrl")) {
                    String url = json.get("avatarUrl").getAsString();
                    Location avatar = ClickGui.getInstance().musicPanel.loginRenderer.tempAvatar;

                    if (!ClickGui.getInstance().musicPanel.loginRenderer.avatarLoaded) {
                        ClickGui.getInstance().musicPanel.loginRenderer.avatarLoaded = true;
                        MultiThreadingUtil.runAsync(new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {
                                try (InputStream is = HttpUtils.get(url, null)) {
                                    BufferedImage img = NativeBackedImage.make(is);

                                    Textures.loadTextureAsyncly(avatar, img);

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

    public static void main(String[] args) {
        getUserProfile();
        List<Music> search = search("Neverland");

        for (Music iMusic : search) {
            System.out.println(iMusic.getName() + ": " + iMusic.getId());
            System.out.println(iMusic.getPlayUrl());
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

        JsonObject json = CloudMusicApi.likeList(MusicPanel.profile.id).toJsonObject();

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
