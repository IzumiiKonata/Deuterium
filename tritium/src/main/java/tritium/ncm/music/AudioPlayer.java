package tritium.ncm.music;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import repackage.processing.sound.*;
import tritium.management.WidgetsManager;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.other.MemoryTracker;
import tritium.widget.impl.SpectrumVisualizer;
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
    public static final SpectrumVisualizer visualizer = new SpectrumVisualizer(JSynFFT.FFT_SIZE, 128);


    static int skipCount = 0;

    public static final JSynFFT.FFTCalcCallback callback = fft -> {

        if (!WidgetsManager.musicSpectrum.isEnabled() && !(Minecraft.getMinecraft().currentScreen instanceof NCMScreen))
            return;

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

    private static final int OSC_TARGET_TAPS = 384;
    private static final float OSC_EDGE_STRENGTH = 0.8f;
    private static final float OSC_BUFFER_STRENGTH = 1.0f;
    private static final float OSC_RESPONSIVENESS = 0.4f;

    public static class OscilloscopeState {
        final int stride;
        final int wd;
        final int nd;
        final float[] corrected;
        final float[] ds;
        final float[] corrBuffer;
        final float[] kernel;
        final float[] slopeFinder;
        final float[] bufferWindow;

        OscilloscopeState(int captureSamples, int displaySamples) {
            int s = Math.max(1, Math.round(displaySamples / (float) OSC_TARGET_TAPS));
            this.stride = s;
            this.wd = Math.max(8, displaySamples / s);
            this.nd = captureSamples / s;

            this.corrected = new float[captureSamples];
            this.ds = new float[nd];
            this.corrBuffer = new float[wd];
            this.kernel = new float[wd];
            this.slopeFinder = new float[wd];
            this.bufferWindow = new float[wd];

            float center = (wd - 1) * 0.5f;
            float slopeStd = Math.max(1f, wd * 0.10f);
            float winStd = Math.max(1f, wd * 0.35f);
            int half = wd / 2;

            for (int k = 0; k < wd; k++) {
                float ds = (k - center) / slopeStd;
                float g = (float) Math.exp(-0.5f * ds * ds);
                slopeFinder[k] = (k < half ? -OSC_EDGE_STRENGTH : OSC_EDGE_STRENGTH) * g;

                float dw = (k - center) / winStd;
                bufferWindow[k] = (float) Math.exp(-0.5f * dw * dw);
            }
        }
    }

    public void setListeners() {
        fft.removeInput();

        int sampleRate = Engine.getEngine().getSampleRate();
        float windowTime = WidgetsManager.musicSpectrum.windowTime.getValue() * 0.001f;
        int displaySamples = Math.max(2, (int) (sampleRate * windowTime));
        int triggerSearch = displaySamples;
        int captureSamples = displaySamples + triggerSearch;

        waveform.removeInput();

        waveform.resize((float) captureSamples / sampleRate);

        oscStateL = new OscilloscopeState(captureSamples, displaySamples);
        oscStateR = new OscilloscopeState(captureSamples, displaySamples);

        lockL.lock();
        wave = new float[captureSamples];
        waveVertexes = new float[displaySamples * 2];
        osc = new float[displaySamples];
        lockL.unlock();

        lockR.lock();
        waveRight = new float[captureSamples];
        waveRightVertexes = new float[displaySamples * 2];
        oscRight = new float[displaySamples];
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

        int display = output.length / 2;
        int offset = Math.max(0, input.length - display);

        double spacing = (ms.getWidth() - 8) / (double) display;
        double height = (ms.stereo.getValue() ? (ms.getHeight() - 17) * 0.5 : (ms.getHeight() - 17)) - 4;
        double volumeScale = ms.absVol.getValue() ? (WidgetsManager.musicInfo.volume.getValue() * 2) : .5 + (WidgetsManager.musicInfo.volume.getValue() * 1.75);

        for (int i = 0; i < display; i++) {
            float v = input[offset + i];
            int outputIdx = i * 2;
            output[outputIdx] = (float) (spacing * i);
            output[outputIdx + 1] = (float) (height * v / volumeScale);
        }
    }

    public void computeOscilloscopeVertexes(float[] input, float[] output, float[] vertexes, OscilloscopeState state) {
        MusicSpectrumWidget ms = WidgetsManager.musicSpectrum;

        int n = input.length;
        int display = output.length;

        int stride = state.stride;
        int wd = state.wd;
        int nd = Math.min(state.nd, n / stride);
        int searchD = Math.max(1, nd - wd);

        float[] corrected = state.corrected;
        float[] ds = state.ds;
        float[] buf = state.corrBuffer;
        float[] kernel = state.kernel;
        float[] slope = state.slopeFinder;
        float[] window = state.bufferWindow;

        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += input[i];
        }
        float mean = (float) (sum / n);
        for (int i = 0; i < n; i++) {
            corrected[i] = input[i] - mean;
        }

        double energy = 0;
        for (int j = 0; j < nd; j++) {
            int base = j * stride;
            float s = 0;
            int cnt = 0;
            for (int k = 0; k < stride; k++) {
                int idx = base + k;
                if (idx < n) {
                    s += corrected[idx];
                    cnt++;
                }
            }
            float v = cnt > 0 ? s / cnt : 0f;
            ds[j] = v;
            energy += (double) v * v;
        }
        float dsRms = (float) Math.sqrt(energy / nd);

        int triggerFull;

        if (dsRms < 1.0e-6f) {
            triggerFull = 0;
        } else {
            for (int k = 0; k < wd; k++) {
                kernel[k] = slope[k] + OSC_BUFFER_STRENGTH * buf[k];
            }

            int bestOff = 0;
            double bestScore = -Double.MAX_VALUE;
            for (int off = 0; off <= searchD; off++) {
                double score = 0;
                for (int k = 0; k < wd; k++) {
                    score += ds[off + k] * kernel[k];
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestOff = off;
                }
            }

            triggerFull = bestOff * stride;

            double a2 = 0;
            for (int k = 0; k < wd; k++) {
                float v = ds[bestOff + k];
                a2 += (double) v * v;
            }
            float aRms = (float) Math.sqrt(a2 / wd);
            if (aRms > 1.0e-6f) {
                float ainv = 1f / aRms;
                for (int k = 0; k < wd; k++) {
                    float val = ds[bestOff + k] * ainv * window[k];
                    buf[k] += OSC_RESPONSIVENESS * (val - buf[k]);
                }

                double b2 = 0;
                for (int k = 0; k < wd; k++) {
                    b2 += (double) buf[k] * buf[k];
                }
                if (b2 > 1.0e-9) {
                    float bn = (float) (1.0 / Math.sqrt(b2 / wd));
                    for (int k = 0; k < wd; k++) {
                        buf[k] *= bn;
                    }
                }
            }
        }

        if (triggerFull > n - display) {
            triggerFull = n - display;
        }
        if (triggerFull < 0) {
            triggerFull = 0;
        }

        for (int j = 0; j < display; j++) {
            output[j] = corrected[triggerFull + j];
        }

        double spacing = (ms.getWidth() - 8) / (double) display;

        double height =
                (ms.stereo.getValue()
                        ? (ms.getHeight() - 17) * 0.5
                        : (ms.getHeight() - 17)) - 4;

        float volumeScale =
                (float) (ms.absVol.getValue()
                        ? (WidgetsManager.musicInfo.volume.getValue() * 2)
                        : (.5f + (WidgetsManager.musicInfo.volume.getValue() * 1.75f)));

        for (int j = 0; j < display; j++) {
            int vi = j * 2;
            vertexes[vi] = (float) (spacing * j);
            vertexes[vi + 1] = (float) (height * output[j] / volumeScale);
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