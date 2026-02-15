package tritium.ncm.music;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import processing.sound.*;
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
    public static int spectrumChannels = 2048;
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
    public ByteBuffer waveVertexesBufferBackend, waveRightVertexesBufferBackend;
    public FloatBuffer waveVertexesBuffer, waveRightVertexesBuffer;

    public float[] oscilloscopeL, oscilloscopeR;
    public float[] oscilloscopeVertexesL, oscilloscopeVertexesR;
    public ByteBuffer oscilloscopeVertexesBufferBackendL, oscilloscopeVertexesBufferBackendR;
    public FloatBuffer oscilloscopeVertexesBufferL, oscilloscopeVertexesBufferR;
    public volatile boolean oscilloscopeDataLFilled = false, oscilloscopeDataRFilled = false;

    private int lastZeroCrossingIndexL = 0;
    private float smoothedZeroCrossingIndexL = 0;

    public final ReentrantLock lockL = new ReentrantLock(), lockR = new ReentrantLock();
    public volatile boolean spectrumDataLFilled = false, spectrumDataRFilled = false;


    public void setListeners() {
        fft.removeInput();

        float windowTime = WidgetsManager.musicSpectrum.windowTime.getValue() * 0.001f;
        int nsamples = (int) (Engine.getEngine().getSampleRate() * windowTime);
        waveform.removeInput();

        waveform.resize(windowTime);

        lockL.lock();
        wave = new float[nsamples];
        waveVertexes = new float[nsamples * 2];
        oscilloscopeL = new float[nsamples];
        oscilloscopeVertexesL = new float[nsamples * 2];
        lockL.unlock();

        lockR.lock();
        waveRight = new float[nsamples];
        waveRightVertexes = new float[nsamples * 2];
        oscilloscopeR = new float[nsamples];
        oscilloscopeVertexesR = new float[nsamples * 2];
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

        if (style == MusicSpectrumWidget.Style.Waveform && waveRightVertexes.length == waveRight.length * 2) {

            wave = waveform.analyze();
            waveRight = waveform.analyzeRight();

            this.computeVertexes(wave, waveVertexes);
            this.computeVertexes(waveRight, waveRightVertexes);

            // ⚠⚠⚠ race conditions 警告 ⚠⚠⚠
//            if (lockL.tryLock()) {

                if (waveVertexesBuffer == null || waveVertexesBuffer.capacity() != CloudMusic.player.waveVertexes.length) {

                    if (waveVertexesBuffer != null) {
                        MemoryTracker.memFree(waveVertexesBufferBackend);
                    }

                    waveVertexesBufferBackend = MemoryTracker.memAlloc(CloudMusic.player.waveVertexes.length << 2);
                    waveVertexesBuffer = waveVertexesBufferBackend.asFloatBuffer();
                    waveVertexesBuffer.put(CloudMusic.player.waveVertexes);
                } else {
                    waveVertexesBuffer.clear();
                    waveVertexesBuffer.put(CloudMusic.player.waveVertexes);
                }

                waveVertexesBuffer.flip();
                spectrumDataLFilled = true;
//                lockL.unlock();
//            }

//            if (lockR.tryLock()) {
                if (waveRightVertexesBuffer == null || waveRightVertexesBuffer.capacity() != CloudMusic.player.waveRightVertexes.length) {

                    if (waveRightVertexesBuffer != null) {
                        MemoryTracker.memFree(waveRightVertexesBufferBackend);
                    }

                    waveRightVertexesBufferBackend = MemoryTracker.memAlloc(CloudMusic.player.waveRightVertexes.length << 2);
                    waveRightVertexesBuffer = waveRightVertexesBufferBackend.asFloatBuffer();
                } else {
                    waveRightVertexesBuffer.clear();
                    waveRightVertexesBuffer.put(CloudMusic.player.waveRightVertexes);
                }

                waveRightVertexesBuffer.flip();
                spectrumDataRFilled = true;
//                lockR.unlock();
//            }

        }

        if (style == MusicSpectrumWidget.Style.Oscilloscope) {
            wave = waveform.analyze();
            waveRight = waveform.analyzeRight();

            this.computeOscilloscopeVertexes(wave, oscilloscopeL, oscilloscopeVertexesL);
            this.computeOscilloscopeVertexes(waveRight, oscilloscopeR, oscilloscopeVertexesR);

            if (oscilloscopeVertexesBufferL == null || oscilloscopeVertexesBufferL.capacity() != oscilloscopeVertexesL.length) {
                if (oscilloscopeVertexesBufferL != null) {
                    MemoryTracker.memFree(oscilloscopeVertexesBufferBackendL);
                }

                oscilloscopeVertexesBufferBackendL = MemoryTracker.memAlloc(oscilloscopeVertexesL.length << 2);
                oscilloscopeVertexesBufferL = oscilloscopeVertexesBufferBackendL.asFloatBuffer();
            } else {
                oscilloscopeVertexesBufferL.clear();
            }
            oscilloscopeVertexesBufferL.put(oscilloscopeVertexesL);
            oscilloscopeVertexesBufferL.flip();
            oscilloscopeDataLFilled = true;

            if (oscilloscopeVertexesBufferR == null || oscilloscopeVertexesBufferR.capacity() != oscilloscopeVertexesR.length) {
                if (oscilloscopeVertexesBufferR != null) {
                    MemoryTracker.memFree(oscilloscopeVertexesBufferBackendR);
                }

                oscilloscopeVertexesBufferBackendR = MemoryTracker.memAlloc(oscilloscopeVertexesR.length << 2);
                oscilloscopeVertexesBufferR = oscilloscopeVertexesBufferBackendR.asFloatBuffer();
            } else {
                oscilloscopeVertexesBufferR.clear();
            }
            oscilloscopeVertexesBufferR.put(oscilloscopeVertexesR);
            oscilloscopeVertexesBufferR.flip();
            oscilloscopeDataRFilled = true;
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

    public void computeOscilloscopeVertexes(float[] input, float[] output, float[] vertexes) {
        MusicSpectrumWidget ms = WidgetsManager.musicSpectrum;

        int zeroCrossingIndex = findZeroCrossing(input);
        
        if (lastZeroCrossingIndexL == 0) {
            lastZeroCrossingIndexL = zeroCrossingIndex;
            smoothedZeroCrossingIndexL = zeroCrossingIndex;
        } else {
            int diff = zeroCrossingIndex - lastZeroCrossingIndexL;
            
            if (Math.abs(diff) > 10) {
                smoothedZeroCrossingIndexL = smoothedZeroCrossingIndexL * 0.9f + lastZeroCrossingIndexL * 0.1f;
            } else {
                smoothedZeroCrossingIndexL = smoothedZeroCrossingIndexL * 0.7f + zeroCrossingIndex * 0.3f;
            }
            
            lastZeroCrossingIndexL = zeroCrossingIndex;
        }
        
        for (int i = 0; i < input.length; i++) {
            float sourceIndex = (smoothedZeroCrossingIndexL + i) % input.length;
            int idx1 = (int) sourceIndex;
            int idx2 = (idx1 + 1) % input.length;
            float fraction = sourceIndex - idx1;
            output[i] = input[idx1] * (1 - fraction) + input[idx2] * fraction;
        }

        double spacing = (ms.getWidth() - 8) / input.length;
        double height = (ms.stereo.getValue() ? (ms.getHeight() - 17) * 0.5 : (ms.getHeight() - 17)) - 4;

        for (int i = 0; i < output.length; i++) {
            float v = output[i];
            int vertexIdx = i * 2;
            vertexes[vertexIdx] = (float) (spacing * i);
            vertexes[vertexIdx + 1] = (float) (height * v / (WidgetsManager.musicSpectrum.absVol.getValue() ? (WidgetsManager.musicInfo.volume.getValue() * 2) : .5 + (WidgetsManager.musicInfo.volume.getValue() * 1.75)));
        }

    }

    private int findZeroCrossing(float[] data) {
        int searchStart = data.length / 4;
        int searchEnd = data.length / 2;
        
        for (int i = searchStart + 1; i < searchEnd; i++) {
            if (data[i - 1] < 0 && data[i] >= 0) {
                float fraction = -data[i - 1] / (data[i] - data[i - 1]);
                return (int) (i - 1 + fraction);
            }
        }
        
        for (int i = 1; i < data.length; i++) {
            if (data[i - 1] < 0 && data[i] >= 0) {
                float fraction = -data[i - 1] / (data[i] - data[i - 1]);
                return (int) (i - 1 + fraction);
            }
        }
        
        return 0;
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
    private boolean finished = false;

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