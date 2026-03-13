package tritium.ncm.music;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import repackage.processing.sound.*;
import tritium.management.WidgetsManager;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.other.MemoryTracker;
import tritium.widget.impl.ExtendedSpectrumVisualizer;
import tritium.widget.impl.MusicSpectrumWidget;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 播放视频里面的音频
 */
public class AudioPlayer {
    public SoundFile player;
    public Runnable afterPlayed;

    @Getter
    public float volume = 0.25f;

    public AudioPlayer(File file) {
        finished = false;

        this.player = new SoundFile(file.getAbsolutePath());
        this.setListeners();
    }

    public void setAudio(File file) {
        this.close();

        this.player = new SoundFile(file.getAbsolutePath());
        this.setListeners();
        finished = false;
    }

    @Getter
    FFT fft = new FFT(128, callback);

    public static float[] bandValues = new float[1];
    public static ExtendedSpectrumVisualizer visualizer;

    static int skipCount = 0;

    public static final JSynFFT.FFTCalcCallback callback = fft -> {

        if (!WidgetsManager.musicSpectrum.isEnabled() && !(Minecraft.getMinecraft().currentScreen instanceof NCMScreen))
            return;

        if (visualizer == null || visualizer.getSampleRate() != 44100 || visualizer.getFftSize() != JSynFFT.FFT_SIZE) {
            visualizer = new ExtendedSpectrumVisualizer(44100, JSynFFT.FFT_SIZE, 1024, ExtendedSpectrumVisualizer.FrequencyDistribution.BARK_ENHANCED);
        }

        int skipAmount = 4;

        if (skipCount < skipAmount) {
            skipCount++;
        } else {
            skipCount = 0;
            bandValues = visualizer.processFFT(fft);
        }
    };

    @Getter
    Waveform waveform = new Waveform(WidgetsManager.musicSpectrum.windowTime.getValue() * 0.001f);
    public final Object lock = new Object();
    public float[] wave, waveRight;

    public float[] waveVertexes, waveRightVertexes;
    public float[] osc, oscRight;
    public ByteBuffer waveVertexesBufferBackend, waveRightVertexesBufferBackend;
    public FloatBuffer waveVertexesBuffer, waveRightVertexesBuffer;

    public final ReentrantLock lockL = new ReentrantLock(), lockR = new ReentrantLock();
    public volatile boolean spectrumDataLFilled = false, spectrumDataRFilled = false;

    private OscilloscopeState oscStateL;
    private OscilloscopeState oscStateR;

    public static class OscilloscopeState {
        private final float[] corrected;
        private final float[] previousFrame;
        private int lastPeriod = 64;
        private float smoothedShift = 0;

        OscilloscopeState(int n) {
            corrected = new float[n];
            previousFrame = new float[n];
        }
    }

    public void setListeners() {
        fft.removeInput();

        float windowTime = WidgetsManager.musicSpectrum.windowTime.getValue() * 0.001f;
        int numSamples = (int) (Engine.getEngine().getSampleRate() * windowTime);
        waveform.removeInput();

        waveform.resize(windowTime);

        oscStateL = new OscilloscopeState(numSamples);
        oscStateR = new OscilloscopeState(numSamples);

        lockL.lock();
        wave = new float[numSamples];
        waveVertexes = new float[numSamples * 2];
        osc = new float[numSamples];
        lockL.unlock();

        lockR.lock();
        waveRight = new float[numSamples];
        waveRightVertexes = new float[numSamples * 2];
        oscRight = new float[numSamples];
        lockR.unlock();

        waveform.input(this.player);
        fft.input(this.player);

        player.setOnFinished(() -> finished = true);
    }

    @SneakyThrows
    public void doDetections() {
        boolean pausing = this.isPausing();
        if (!WidgetsManager.musicSpectrum.isEnabled() || pausing) {
            return;
        }

        MusicSpectrumWidget.Style style = WidgetsManager.musicSpectrum.style.getValue();

        boolean rect = style == MusicSpectrumWidget.Style.Rect;
        boolean line = style == MusicSpectrumWidget.Style.Line;
        if (rect || line) {
            return;
        }

        if (style == MusicSpectrumWidget.Style.Waveform || style == MusicSpectrumWidget.Style.Oscilloscope) {

            wave = waveform.analyze();
            waveRight = waveform.analyzeRight();

            if (style == MusicSpectrumWidget.Style.Waveform) {
                this.computeVertexes(wave, waveVertexes);
                this.computeVertexes(waveRight, waveRightVertexes);
            } else {
                computeOscilloscopeVertexes(wave, osc, waveVertexes, oscStateL);
                computeOscilloscopeVertexes(waveRight, oscRight, waveRightVertexes, oscStateR);
            }

            if (waveVertexesBuffer == null || waveVertexesBuffer.capacity() != this.waveVertexes.length) {
                lockL.lock();
                if (waveVertexesBuffer != null) {
                    MemoryTracker.memFree(waveVertexesBufferBackend);
                }

                waveVertexesBufferBackend = MemoryTracker.memAlloc(this.waveVertexes.length << 2);
                waveVertexesBuffer = waveVertexesBufferBackend.asFloatBuffer();
                waveVertexesBuffer.put(this.waveVertexes);
                lockL.unlock();
            } else {
                waveVertexesBuffer.clear();
                waveVertexesBuffer.put(this.waveVertexes);
            }

            waveVertexesBuffer.flip();
            spectrumDataLFilled = true;

            if (waveRightVertexesBuffer == null || waveRightVertexesBuffer.capacity() != this.waveRightVertexes.length) {
                lockR.lock();
                if (waveRightVertexesBuffer != null) {
                    MemoryTracker.memFree(waveRightVertexesBufferBackend);
                }

                waveRightVertexesBufferBackend = MemoryTracker.memAlloc(this.waveRightVertexes.length << 2);
                waveRightVertexesBuffer = waveRightVertexesBufferBackend.asFloatBuffer();
                waveRightVertexesBuffer.put(this.waveRightVertexes);
                lockR.unlock();
            } else {
                waveRightVertexesBuffer.clear();
                waveRightVertexesBuffer.put(this.waveRightVertexes);
            }

            waveRightVertexesBuffer.flip();
            spectrumDataRFilled = true;
        }
    }

    public void computeVertexes(float[] input, float[] output) {
        MusicSpectrumWidget ms = WidgetsManager.musicSpectrum;

        double spacing = (ms.getWidth() - 8) / input.length;
        double height = (ms.stereo.getValue() ? (ms.getHeight() - 17) * 0.5 : (ms.getHeight() - 17)) - 4;

        for (int i = 0; i < input.length; i++) {
            float v = input[i];
            int outputIdx = i * 2;
            output[outputIdx] = (float) (spacing * i);
            output[outputIdx + 1] = (float) (height * v / (WidgetsManager.musicSpectrum.absVol.getValue() ? (WidgetsManager.musicInfo.volume.getValue() * 2) : .5 + (WidgetsManager.musicInfo.volume.getValue() * 1.75)));
        }
    }

    public void computeOscilloscopeVertexes(float[] input, float[] output, float[] vertexes, OscilloscopeState state) {
        MusicSpectrumWidget ms = WidgetsManager.musicSpectrum;

        int n = input.length;
        float mean = 0f;

        for (float v : input) mean += v;

        mean /= n;

        for (int i = 0; i < n; i++) {
            state.corrected[i] = input[i] - mean;
        }

        int minLag = 16;
        int maxLag = Math.min(n / 2, 512);

        float bestScore = -Float.MAX_VALUE;
        int bestLag = state.lastPeriod;

        for (int lag = minLag; lag < maxLag; lag++) {

            float score = 0f;

            int limit = n - lag;

            for (int i = 0; i < limit; i++) {
                score += state.corrected[i] * state.corrected[i + lag];
            }

            if (score > bestScore) {
                bestScore = score;
                bestLag = lag;
            }
        }

        state.lastPeriod = bestLag;

        int search = Math.min(bestLag, 64);

        int bestShift = (int) state.smoothedShift;
        bestScore = -Float.MAX_VALUE;

        int corrSize = Math.min(256, n / 4);

        int startShift = bestShift - search;
        int endShift = bestShift + search;

        for (int shift = startShift; shift <= endShift; shift++) {

            int s = shift;

            if (s < 0) s += n;
            if (s >= n) s -= n;

            float score = 0f;

            int idx = s;

            for (int i = 0; i < corrSize; i++) {

                score += state.corrected[idx] * state.previousFrame[i];

                idx++;

                if (idx >= n) idx = 0;
            }

            if (score > bestScore) {
                bestScore = score;
                bestShift = s;
            }
        }

        float alpha = 0.25f;

        state.smoothedShift = state.smoothedShift * (1 - alpha) + bestShift * alpha;

        int idx = (int) state.smoothedShift;

        for (int i = 0; i < n; i++) {

            output[i] = state.corrected[idx];

            idx++;

            if (idx >= n) idx = 0;
        }

        System.arraycopy(output, 0, state.previousFrame, 0, n);

        double spacing = (ms.getWidth() - 8) / (double) n;

        double height =
                (ms.stereo.getValue()
                        ? (ms.getHeight() - 17) * 0.5
                        : (ms.getHeight() - 17)) - 4;

        float volumeScale =
                (float) (WidgetsManager.musicSpectrum.absVol.getValue()
                        ? (WidgetsManager.musicInfo.volume.getValue() * 2)
                        : (.5f + (WidgetsManager.musicInfo.volume.getValue() * 1.75f)));

        for (int i = 0; i < n; i++) {

            int vi = i * 2;

            vertexes[vi] = (float) (spacing * i);

            vertexes[vi + 1] =
                    (float) (height * output[i] / volumeScale);
        }
    }

    public void play() {
        finished = false;
        this.player.play();
        this.player.amp(volume);
    }

    @SneakyThrows
    public void setPlaybackTime(float millis) {
        this.player.jump(millis / 1000F);
        this.player.amp(volume);
    }

    @SneakyThrows
    public void close() {
        this.player.jump(0);
        player.stop();
        player.cleanUp();
    }

    @Getter
    private boolean finished;

    public void setAfterPlayed(Runnable runnable) {
        this.afterPlayed = runnable;
        this.player.setOnFinished(() -> {
            finished = true;
            runnable.run();
        });
    }

    public float getTotalTimeSeconds() {
        return (int) this.player.duration();
    }

    public float getCurrentTimeSeconds() {
        return (int) (getCurrentTimeMillis() / 1000);
    }

    public float getTotalTimeMillis() {
        return getTotalTimeSeconds() * 1000;
    }

    public float getCurrentTimeMillis() {
        return this.player.position() * 1000;
    }

    public boolean isPausing() {
        return !this.player.isPlaying();
    }

    public void setVolume(float volume) {
        this.volume = volume;
        this.player.amp(this.getVolume());
    }

    public void pause() {
        this.player.pause();
    }

    public void unpause() {
        this.play();
    }
}