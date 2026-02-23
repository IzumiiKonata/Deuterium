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
    public float[] osc, oscRight;
    public ByteBuffer waveVertexesBufferBackend, waveRightVertexesBufferBackend;
    public FloatBuffer waveVertexesBuffer, waveRightVertexesBuffer;

    private float[] previousWaveform = null;
    private int phaseLockCounter = 0;

    public final ReentrantLock lockL = new ReentrantLock(), lockR = new ReentrantLock();
    public volatile boolean spectrumDataLFilled = false, spectrumDataRFilled = false;

    public void setListeners() {
        fft.removeInput();

        float windowTime = WidgetsManager.musicSpectrum.windowTime.getValue() * 0.001f;
        int nsamples = (int) (Engine.getEngine().getSampleRate() * windowTime);
        waveform.removeInput();

        waveform.resize(windowTime);

        previousWaveform = null;
        phaseLockCounter = 0;

        lockL.lock();
        wave = new float[nsamples];
        waveVertexes = new float[nsamples * 2];
        osc = new float[nsamples];
        lockL.unlock();

        lockR.lock();
        waveRight = new float[nsamples];
        waveRightVertexes = new float[nsamples * 2];
        oscRight = new float[nsamples];
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
                this.computeOscilloscopeVertexes(wave, osc, waveVertexes);
                this.computeOscilloscopeVertexes(waveRight, oscRight, waveRightVertexes);
            }

            // ⚠⚠⚠ race conditions 警告 ⚠⚠⚠
//            if (lockL.tryLock()) {

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
//            }

//            if (lockR.tryLock()) {
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
//                lockR.unlock();
//            }

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

        // Apply DC offset correction first
        float dcOffset = calculateDCOffset(input);
        float[] correctedInput = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            correctedInput[i] = input[i] - dcOffset;
        }

        // Find stable phase-aligned start and ensure waveform continuity
        int phaseAlignedStart = findStablePhaseAlignedStart(correctedInput);

        // Ensure we start at a complete cycle boundary to avoid discontinuity
        int cycleAdjustedStart = adjustToCycleBoundary(correctedInput, phaseAlignedStart);

        for (int i = 0; i < input.length; i++) {
            int sourceIndex = (cycleAdjustedStart + i) % input.length;
            output[i] = correctedInput[sourceIndex];
        }

        double spacing = (ms.getWidth() - 8) / input.length;
        double height = (ms.stereo.getValue() ? (ms.getHeight() - 17) * 0.5 : (ms.getHeight() - 17)) - 4;

        for (int i = 0; i < output.length; i++) {
            float v = output[i];
            int vertexIdx = i * 2;
            vertexes[vertexIdx] = (float) (spacing * i);
            vertexes[vertexIdx + 1] = (float) (height * v / (WidgetsManager.musicSpectrum.absVol.getValue() ? (WidgetsManager.musicInfo.volume.getValue() * 2) : .5 + (WidgetsManager.musicInfo.volume.getValue() * 1.75)));
        }

        previousWaveform = output.clone();
    }

    private float calculateDCOffset(float[] data) {
        // Simple DC offset calculation using mean value
        float sum = 0;
        for (float sample : data) {
            sum += sample;
        }
        return sum / data.length;
    }

    private int findStablePhaseAlignedStart(float[] data) {
        // Use amplitude-based zero crossing detection for stability
        int bestCrossing = findAmplitudeBasedZeroCrossing(data);

        // If we have a previous waveform, try to maintain phase continuity
        if (previousWaveform != null && phaseLockCounter >= 3) {
            int stableCrossing = findPhaseContinuousCrossing(data, bestCrossing);
            if (stableCrossing != -1) {
                bestCrossing = stableCrossing;
            }
        }

        phaseLockCounter++;
        return bestCrossing;
    }

    private int findAmplitudeBasedZeroCrossing(float[] data) {
        // Find the strongest zero crossing in the central region
        int searchStart = data.length / 4;
        int searchEnd = data.length * 3 / 4;

        float maxAmplitude = 0;
        int bestCrossing = data.length / 2;

        for (int i = searchStart + 1; i < searchEnd; i++) {
            // Look for positive-going zero crossing
            if (data[i - 1] <= 0 && data[i] > 0) {
                // Calculate exact crossing point using linear interpolation
                float fraction = -data[i - 1] / (data[i] - data[i - 1]);
                float crossingPoint = i - 1 + fraction;

                // Calculate amplitude around the crossing
                float amplitude = Math.abs(data[i]) + Math.abs(data[i - 1]);

                // Prefer crossings with higher amplitude for stability
                if (amplitude > maxAmplitude) {
                    maxAmplitude = amplitude;
                    bestCrossing = (int) Math.round(crossingPoint);
                }
            }
        }

        // If no good crossing found, use the center
        if (maxAmplitude < 0.01f) {
            return data.length / 2;
        }

        return bestCrossing;
    }

    private int findPhaseContinuousCrossing(float[] currentData, int suggestedCrossing) {
        // Try to maintain phase continuity with previous waveform
        int searchRange = Math.min(50, currentData.length / 8);
        float bestCorrelation = -1;
        int bestOffset = 0;

        for (int offset = -searchRange; offset <= searchRange; offset++) {
            int testCrossing = (suggestedCrossing + offset + currentData.length) % currentData.length;

            // Calculate correlation around the crossing point
            float correlation = calculateLocalCorrelation(currentData, testCrossing, 20);

            if (correlation > bestCorrelation) {
                bestCorrelation = correlation;
                bestOffset = offset;
            }
        }

        // Only apply correction if correlation is reasonably good
        if (bestCorrelation > 0.5f && Math.abs(bestOffset) < currentData.length / 16) {
            return (suggestedCrossing + bestOffset + currentData.length) % currentData.length;
        }

        return -1; // No good correction found
    }

    private float calculateLocalCorrelation(float[] currentData, int crossing, int windowSize) {
        float correlation = 0;
        int validSamples = 0;

        for (int i = -windowSize; i <= windowSize; i++) {
            int currentIdx = (crossing + i + currentData.length) % currentData.length;
            int prevIdx = (crossing + i + previousWaveform.length) % previousWaveform.length;

            if (currentIdx >= 0 && currentIdx < currentData.length &&
                    prevIdx >= 0 && prevIdx < previousWaveform.length) {
                correlation += currentData[currentIdx] * previousWaveform[prevIdx];
                validSamples++;
            }
        }

        return validSamples > 0 ? correlation / validSamples : -1;
    }

    private int adjustToCycleBoundary(float[] data, int suggestedStart) {
        // Find the next complete cycle boundary to avoid discontinuity
        int cycleLength = estimateCycleLength(data, suggestedStart);

        if (cycleLength > 0 && cycleLength < data.length / 2) {
            // Adjust to the nearest cycle boundary
            int cyclesFromStart = suggestedStart / cycleLength;
            int adjustedStart = cyclesFromStart * cycleLength;

            // Ensure the adjusted start is within bounds
            if (adjustedStart >= 0 && adjustedStart < data.length) {
                return adjustedStart;
            }
        }

        // Fallback: use the original start position
        return suggestedStart;
    }

    private int estimateCycleLength(float[] data, int startPos) {
        // Simple cycle length estimation using zero crossing detection
        int searchRange = Math.min(200, data.length / 4);

        // Find the next zero crossing after startPos
        int nextCrossing = -1;
        for (int i = startPos + 1; i < startPos + searchRange; i++) {
            int idx = i % data.length;
            int prevIdx = (i - 1 + data.length) % data.length;

            if (data[prevIdx] <= 0 && data[idx] > 0) {
                nextCrossing = idx;
                break;
            }
        }

        if (nextCrossing == -1) {
            return -1; // No cycle detected
        }

        // Find the crossing after that to estimate cycle length
        int secondCrossing = -1;
        for (int i = nextCrossing + 1; i < nextCrossing + searchRange; i++) {
            int idx = i % data.length;
            int prevIdx = (i - 1 + data.length) % data.length;

            if (data[prevIdx] <= 0 && data[idx] > 0) {
                secondCrossing = idx;
                break;
            }
        }

        if (secondCrossing == -1) {
            return -1; // Only one cycle detected
        }

        int cycleLength = secondCrossing - nextCrossing;
        if (cycleLength < 0) {
            cycleLength += data.length;
        }

        return cycleLength;
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