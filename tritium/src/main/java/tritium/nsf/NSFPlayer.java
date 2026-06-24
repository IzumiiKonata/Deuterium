package tritium.nsf;

import lombok.Getter;
import tritium.zdream.nsfplayer.mixer.IMixerChannel;
import tritium.zdream.nsfplayer.mixer.xgm.XgmMultiSoundMixer;
import tritium.zdream.nsfplayer.nsf.audio.NsfAudio;
import tritium.zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import tritium.zdream.nsfplayer.nsf.renderer.NsfRenderer;
import tritium.zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import tritium.zdream.nsfplayer.sound.AbstractNsfSound;
import tritium.zdream.nsfplayer.sound.SoundTriangle;
import tritium.zdream.nsfplayer.sound.SoundVRC6Sawtooth;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static tritium.zdream.nsfplayer.core.INsfChannelCode.*;

public class NSFPlayer {

    private static final int RING_SIZE = 16384;

    private static final int TRIANGLE_SMOOTH_FACTOR = 8;

    private static final byte[] DISPLAY_ORDER = {
            CHANNEL_2A03_PULSE1, CHANNEL_2A03_PULSE2, CHANNEL_2A03_TRIANGLE, CHANNEL_2A03_NOISE, CHANNEL_2A03_DPCM,
            CHANNEL_VRC6_PULSE1, CHANNEL_VRC6_PULSE2, CHANNEL_VRC6_SAWTOOTH,
            CHANNEL_MMC5_PULSE1, CHANNEL_MMC5_PULSE2,
            CHANNEL_FDS,
            CHANNEL_N163_1, CHANNEL_N163_2, CHANNEL_N163_3, CHANNEL_N163_4,
            CHANNEL_N163_5, CHANNEL_N163_6, CHANNEL_N163_7, CHANNEL_N163_8,
            CHANNEL_VRC7_FM1, CHANNEL_VRC7_FM2, CHANNEL_VRC7_FM3,
            CHANNEL_VRC7_FM4, CHANNEL_VRC7_FM5, CHANNEL_VRC7_FM6,
            CHANNEL_S5B_SQUARE1, CHANNEL_S5B_SQUARE2, CHANNEL_S5B_SQUARE3
    };

    private final Object lock = new Object();

    private NsfRenderer renderer;
    private NsfAudio audio;

    private volatile ScopeTap[] taps = new ScopeTap[0];

    private final short[] renderBuffer = new short[8192];

    private int sampleRate = 48000;
    private int frameRate = 60;

    private NSFAudioOutput output;

    private Thread thread;
    private volatile boolean running = false;
    private volatile boolean paused = true;
    private volatile boolean loaded = false;

    @Getter
    private volatile int currentTrack = 0;
    private volatile long framesPlayed = 0;

    private volatile boolean linearMixing = false;
    private volatile boolean triangleMoreSteps = false;
    private volatile boolean echoEnabled = false;

    @Getter
    private String title = "";
    @Getter
    private String artist = "";
    @Getter
    private String copyright = "";
    @Getter
    private String fileName = "";

    public boolean load(File file) {
        try {
            NsfAudio a = new NsfAudioFactory().createFromFile(file.getAbsolutePath());

            NsfRendererConfig cfg = new NsfRendererConfig();
            cfg.sampleRate = 48000;
            cfg.region = NsfRendererConfig.REGION_FOLLOW_AUDIO;
            NsfRenderer r = new NsfRenderer(cfg);

            synchronized (lock) {
                this.audio = a;
                this.renderer = r;
                this.frameRate = r.getFrameRate();
                this.sampleRate = r.getSampleRate();

                int track = a.start;
                if (track < 0 || track >= a.total_songs) {
                    track = 0;
                }

                r.ready(a, track);
                this.currentTrack = track;
                installTaps();
                this.framesPlayed = 0;

                this.title = a.title;
                this.artist = a.artist;
                this.copyright = a.copyright;
                this.fileName = file.getName();
                this.loaded = true;
            }

            ensureOutput();
            ensureThread();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private void installTaps() {
        Set<Byte> set = renderer.allChannelSet();

        List<Byte> ordered = new ArrayList<>();
        for (byte code : DISPLAY_ORDER) {
            if (set.contains(code)) {
                ordered.add(code);
            }
        }
        for (byte code : new LinkedHashSet<>(set)) {
            if (!ordered.contains(code)) {
                ordered.add(code);
            }
        }

        int clockRate = renderer.getClockRate();
        int cyclesPerFrame = Math.max(1, clockRate / frameRate);

        List<ScopeTap> list = new ArrayList<>();
        for (byte code : ordered) {
            AbstractNsfSound sound = renderer.getSound(code);
            if (sound == null) {
                continue;
            }

            IMixerChannel original = sound.getOut();
            while (original instanceof ScopeTap) {
                original = ((ScopeTap) original).getDelegate();
            }

            ScopeTap tap = new ScopeTap(code, channelName(code), original, cyclesPerFrame, RING_SIZE);
            sound.setOut(tap);
            list.add(tap);
        }

        this.taps = list.toArray(new ScopeTap[0]);

        applyPatches();
    }

    private void applyPatches() {
        if (renderer == null) {
            return;
        }

        if (renderer.mixer instanceof XgmMultiSoundMixer) {
            XgmMultiSoundMixer mix = (XgmMultiSoundMixer) renderer.mixer;
            mix.setLinearMixing(linearMixing);
            mix.setTriangleSmoothing(triangleMoreSteps ? TRIANGLE_SMOOTH_FACTOR : 1);
            mix.setEchoEnabled(echoEnabled);
        }

        if (renderer.getSound(CHANNEL_2A03_TRIANGLE) instanceof SoundTriangle tri) {
            tri.setSmoothSteps(triangleMoreSteps ? TRIANGLE_SMOOTH_FACTOR : 1);
        }

        if (renderer.getSound(CHANNEL_VRC6_SAWTOOTH) instanceof SoundVRC6Sawtooth saw) {
            saw.setSmoothSteps(triangleMoreSteps);
        }
    }

    public boolean isLinearMixing() {
        return linearMixing;
    }

    public boolean isTriangleMoreSteps() {
        return triangleMoreSteps;
    }

    public void setLinearMixing(boolean value) {
        this.linearMixing = value;
        synchronized (lock) {
            applyPatches();
        }
    }

    public void setTriangleMoreSteps(boolean value) {
        this.triangleMoreSteps = value;
        synchronized (lock) {
            applyPatches();
        }
    }

    public boolean isEchoEnabled() {
        return echoEnabled;
    }

    public void setEchoEnabled(boolean value) {
        this.echoEnabled = value;
        synchronized (lock) {
            applyPatches();
        }
    }

    private void ensureOutput() {
        if (output == null) {
            output = new NSFAudioOutput(sampleRate);
        }
    }

    private void ensureThread() {
        if (thread == null) {
            running = true;
            thread = new Thread(this::runLoop, "NSF-Player");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void runLoop() {
        while (running) {
            if (paused || !loaded) {
                try {
                    Thread.sleep(8);
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            int n;
            synchronized (lock) {
                if (!loaded || paused || renderer == null) {
                    continue;
                }

                n = renderer.renderOneFrame(renderBuffer, 0, renderBuffer.length);

                ScopeTap[] local = this.taps;
                for (ScopeTap t : local) {
                    t.endFrame(n);
                }

                framesPlayed++;
            }

            if (n > 0 && output != null) {
                output.writeSamples(renderBuffer, 0, n);
            }
        }
    }

    public void play() {
        if (!loaded) {
            return;
        }
        paused = false;
        if (output != null) {
            output.resume();
        }
    }

    public void pause() {
        paused = true;
        if (output != null) {
            output.pause();
        }
    }

    public void togglePause() {
        if (paused) {
            play();
        } else {
            pause();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void selectTrack(int track) {
        if (!loaded) {
            return;
        }

        int count = getTrackCount();
        if (track < 0) {
            track = 0;
        } else if (track >= count) {
            track = count - 1;
        }

        synchronized (lock) {
            renderer.ready(track);
            currentTrack = track;
            installTaps();
            framesPlayed = 0;
        }

        if (output != null) {
            output.flush();
        }
    }

    public void nextTrack() {
        selectTrack((currentTrack + 1) % Math.max(1, getTrackCount()));
    }

    public void prevTrack() {
        int count = Math.max(1, getTrackCount());
        selectTrack((currentTrack - 1 + count) % count);
    }

    public void seek(double seconds) {
        if (!loaded) {
            return;
        }

        long target = Math.max(0, Math.round(seconds * frameRate));

        synchronized (lock) {
            if (target < framesPlayed) {
                renderer.ready(currentTrack);
                installTaps();
                framesPlayed = 0;
            }

            long diff = target - framesPlayed;
            if (diff > 0) {
                renderer.skip((int) diff);
                framesPlayed = target;
            }
        }

        if (output != null) {
            output.flush();
        }
    }

    public double getPositionSeconds() {
        return framesPlayed / (double) frameRate;
    }

    public int getTrackCount() {
        return audio == null ? 0 : audio.total_songs;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public ScopeTap[] getTaps() {
        return taps;
    }

    public void close() {
        running = false;
        loaded = false;
        paused = true;

        Thread t = this.thread;
        if (t != null) {
            t.interrupt();
        }
        this.thread = null;

        if (output != null) {
            output.close();
            output = null;
        }
    }

    public static String channelName(byte code) {
        switch (code) {
            case CHANNEL_2A03_PULSE1: return "Pulse 1";
            case CHANNEL_2A03_PULSE2: return "Pulse 2";
            case CHANNEL_2A03_TRIANGLE: return "Triangle";
            case CHANNEL_2A03_NOISE: return "Noise";
            case CHANNEL_2A03_DPCM: return "DPCM";
            case CHANNEL_VRC6_PULSE1: return "VRC6 Pulse 1";
            case CHANNEL_VRC6_PULSE2: return "VRC6 Pulse 2";
            case CHANNEL_VRC6_SAWTOOTH: return "VRC6 Saw";
            case CHANNEL_MMC5_PULSE1: return "MMC5 Pulse 1";
            case CHANNEL_MMC5_PULSE2: return "MMC5 Pulse 2";
            case CHANNEL_FDS: return "FDS";
            case CHANNEL_N163_1: return "N163 1";
            case CHANNEL_N163_2: return "N163 2";
            case CHANNEL_N163_3: return "N163 3";
            case CHANNEL_N163_4: return "N163 4";
            case CHANNEL_N163_5: return "N163 5";
            case CHANNEL_N163_6: return "N163 6";
            case CHANNEL_N163_7: return "N163 7";
            case CHANNEL_N163_8: return "N163 8";
            case CHANNEL_VRC7_FM1: return "VRC7 FM1";
            case CHANNEL_VRC7_FM2: return "VRC7 FM2";
            case CHANNEL_VRC7_FM3: return "VRC7 FM3";
            case CHANNEL_VRC7_FM4: return "VRC7 FM4";
            case CHANNEL_VRC7_FM5: return "VRC7 FM5";
            case CHANNEL_VRC7_FM6: return "VRC7 FM6";
            case CHANNEL_S5B_SQUARE1: return "5B Square 1";
            case CHANNEL_S5B_SQUARE2: return "5B Square 2";
            case CHANNEL_S5B_SQUARE3: return "5B Square 3";
            default: return "CH " + (code & 0xFF);
        }
    }
}
