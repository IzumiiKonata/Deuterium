package tritium.widget.impl;

import lombok.Getter;
import tritium.management.WidgetsManager;

import java.util.ArrayList;
import java.util.List;

public class ExtendedSpectrumVisualizer {

    @Getter
    private final int sampleRate;
    @Getter
    private final int fftSize;
    @Getter
    private final int numBands;
    private final float minDB = -90f;
    private final float maxDB = 0f;

    @Getter
    private final List<FrequencyBand> bands = new ArrayList<>();
    private final float[] bandMagnitudes;
    private final int maxBin;

    private final float lowFreqCutoff = 500.0f;

    public ExtendedSpectrumVisualizer(int sampleRate, int fftSize, int numBands) {
        this(sampleRate, fftSize, numBands, FrequencyDistribution.BARK_ENHANCED);
    }

    public ExtendedSpectrumVisualizer(int sampleRate, int fftSize, int numBands, FrequencyDistribution distribution) {
        this.sampleRate = sampleRate;
        this.fftSize = fftSize;
        this.numBands = numBands;
        this.maxBin = fftSize / 2 - 1;

        generateFrequencyBands(distribution);
        this.bandMagnitudes = new float[bands.size()];
    }

    private void generateFrequencyBands(FrequencyDistribution distribution) {
        float maxFreq = 14000;
        float minFreq = 20.0f;

        switch (distribution) {
            case LINEAR:
                generateLinearBands(minFreq, maxFreq);
                break;
            case LOGARITHMIC:
                generateLogarithmicBands(minFreq, maxFreq);
                break;
            case MEL_SCALE:
                generateMelBands(minFreq, maxFreq);
                break;
            case BARK_SCALE:
                generateBarkBands(minFreq, maxFreq);
                break;
            case BARK_ENHANCED:
                generateEnhancedBarkBands(minFreq, maxFreq);
                break;
            case ADAPTIVE_BARK:
                generateAdaptiveBarkBands(minFreq, maxFreq);
                break;
        }
    }

    private void generateLinearBands(float minFreq, float maxFreq) {
        float bandWidth = (maxFreq - minFreq) / numBands;

        for (int i = 0; i < numBands; i++) {
            float lowFreq = minFreq + i * bandWidth;
            float highFreq = minFreq + (i + 1) * bandWidth;
            float centerFreq = (lowFreq + highFreq) / 2;

            int lowBin = freqToBin(lowFreq);
            int highBin = freqToBin(highFreq);

            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));

            bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
        }
    }

    private void generateLogarithmicBands(float minFreq, float maxFreq) {
        float logMin = (float) Math.log10(minFreq);
        float logMax = (float) Math.log10(maxFreq);
        float logStep = (logMax - logMin) / numBands;
        for (int i = 0; i < numBands; i++) {
            float lowFreq = (float) Math.pow(10, logMin + i * logStep);
            float highFreq = (float) Math.pow(10, logMin + (i + 1) * logStep);
            float centerFreq = (float) Math.sqrt(lowFreq * highFreq);
            int lowBin = freqToBin(lowFreq);
            int highBin = freqToBin(highFreq);
            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));
            bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
        }
    }

    private void generateMelBands(float minFreq, float maxFreq) {
        double melMin = freqToMel(minFreq);
        double melMax = freqToMel(maxFreq);
        double melStep = (melMax - melMin) / numBands;
        for (int i = 0; i < numBands; i++) {
            double lowFreq = melToFreq((float) (melMin + i * melStep));
            double highFreq = melToFreq((float) (melMin + (i + 1) * melStep));
            double centerFreq = (lowFreq + highFreq) / 2;
            int lowBin = freqToBin((float) lowFreq);
            int highBin = freqToBin((float) highFreq);
            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));
            bands.add(new FrequencyBand((float) centerFreq, (float) lowFreq, (float) highFreq, lowBin, highBin));
        }
    }

    private void generateBarkBands(float minFreq, float maxFreq) {
        float barkMin = freqToBark(minFreq);
        float barkMax = freqToBark(maxFreq);
        float barkStep = (barkMax - barkMin) / numBands;
        for (int i = 0; i < numBands; i++) {
            float lowFreq = barkToFreq(barkMin + i * barkStep);
            float highFreq = barkToFreq(barkMin + (i + 1) * barkStep);
            float centerFreq = (lowFreq + highFreq) / 2;
            int lowBin = freqToBin(lowFreq);
            int highBin = freqToBin(highFreq);
            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));
            bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
        }
    }

    private void generateEnhancedBarkBands(float minFreq, float maxFreq) {
        float lowFreqFactor = .3f;
        int lowFreqBands = (int) (numBands * lowFreqFactor);
        int highFreqBands = numBands - lowFreqBands;

        float lowFreqMax = Math.min(lowFreqCutoff, maxFreq);
        generateDenseBarkBands(minFreq, lowFreqMax, lowFreqBands);

        if (lowFreqMax < maxFreq) {
            float barkMin = freqToBark(lowFreqMax * .21f);
            float barkMax = freqToBark(maxFreq);
            float barkStep = (barkMax - barkMin) / highFreqBands;

            for (int i = 0; i < highFreqBands; i++) {
                float lowFreq = barkToFreq(barkMin + i * barkStep);
                float highFreq = barkToFreq(barkMin + (i + 1) * barkStep);
                float centerFreq = (lowFreq + highFreq) / 2;
                int lowBin = freqToBin(lowFreq);
                int highBin = freqToBin(highFreq);
                lowBin = Math.max(0, Math.min(maxBin, lowBin));
                highBin = Math.max(lowBin, Math.min(maxBin, highBin));
                bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
            }
        }
    }

    private void generateAdaptiveBarkBands(float minFreq, float maxFreq) {
        float barkMin = freqToBark(minFreq);
        float barkMax = freqToBark(maxFreq);


        float[] barkSteps = new float[numBands];
        float totalWeight = 0;

        for (int i = 0; i < numBands; i++) {
            float progress = (float) i / (numBands - 1);

            float weight = (float) Math.exp(-progress * 1.2f) + 0.8f;
            barkSteps[i] = weight;
            totalWeight += weight;
        }


        float barkRange = barkMax - barkMin;
        float currentBark = barkMin;
        float prevHighFreq = minFreq;

        for (int i = 0; i < numBands; i++) {
            float stepSize = (barkSteps[i] / totalWeight) * barkRange;
            float lowFreq = Math.max(prevHighFreq, barkToFreq(currentBark));
            float highFreq = barkToFreq(currentBark + stepSize);
            float centerFreq = (lowFreq + highFreq) / 2;


            lowFreq = Math.max(lowFreq, minFreq);
            highFreq = Math.max(highFreq, lowFreq + 1.0f);

            int lowBin = freqToBin(lowFreq);
            int highBin = freqToBin(highFreq);
            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));


            if (i > 0 && lowBin <= bands.get(i-1).highBin) {
                lowBin = bands.get(i-1).highBin + 1;
            }
            highBin = Math.max(lowBin + 1, highBin);

            bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
            currentBark += stepSize;
            prevHighFreq = highFreq;
        }
    }

    private void generateDenseBarkBands(float minFreq, float maxFreq, int bandCount) {

        int linearBands = bandCount / 5;
        int barkBands = bandCount - linearBands;

        float midFreq = minFreq + (maxFreq - minFreq) * .3f;

        float linearStep = (midFreq - minFreq) / linearBands;
        for (int i = 0; i < linearBands; i++) {
            float lowFreq = minFreq + i * linearStep;
            float highFreq = minFreq + (i + 1) * linearStep;
            float centerFreq = (lowFreq + highFreq) / 2;

            int lowBin = freqToBin(lowFreq);
            int highBin = freqToBin(highFreq);
            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));

            bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
        }

    }

    private double freqToMel(double freq) {
        return 2595.0 * Math.log10(1 + freq / 700.0);
    }

    private double melToFreq(double mel) {
        return 700.0 * (Math.pow(10, mel / 2595.0) - 1);
    }

    private float freqToBark(float freq) {
        return 13.0f * (float) Math.atan(0.00076f * freq) + 3.5f * (float) Math.atan((freq / 7500.0f) * (freq / 7500.0f));
    }

    private float barkToFreq(float bark) {
        return 600.0f * (float) Math.sinh(bark / 4.0f);
    }

    private int freqToBin(float freq) {
        return Math.round(freq * fftSize / sampleRate);
    }

    public float[] processFFT(float[] magnitudes) {
        if (magnitudes.length < fftSize / 2) {
            return bandMagnitudes;
        }

        for (int i = 0; i < bands.size(); i++) {
            FrequencyBand band = bands.get(i);
            float energy = 0;
            int binCount = 0;

            for (int bin = band.lowBin; bin <= band.highBin && bin < magnitudes.length; bin++) {
                float magnitude = magnitudes[bin] * magnitudes[bin];

                if (WidgetsManager.musicSpectrum.absVol.getValue()) {
                    magnitude *= (0.125f * 0.125f) / (float) (WidgetsManager.musicInfo.volume.getValue() * WidgetsManager.musicInfo.volume.getValue());
                } else {
                    magnitude *= 0.25f * 0.25f;
                }

                energy += magnitude;
                binCount++;
            }

            if (binCount > 0) {
                energy = (float) Math.sqrt(energy / binCount);
            }

            float db = energy < 1e-10f ? minDB : 20 * (float) Math.log10(energy);

            if (band.centerFreq < lowFreqCutoff) {
                db -= (lowFreqCutoff - band.centerFreq) / lowFreqCutoff * 18f;
            }

            double max = dBToLinear(-30);
            double min = dBToLinear(-100);
            double value = dBToLinear(db) * 2;

            float sensitivity = 1.0f;

            float normalized = (float) Math.pow((value - min) / (max - min), 1 / (1.8 * sensitivity));
            bandMagnitudes[i] = normalized;
        }

        for (int i = 0; i < bandMagnitudes.length; i++) {
            if (!Float.isFinite(bandMagnitudes[i])) {
                bandMagnitudes[i] = 0;
            }
        }

        return bandMagnitudes;
    }

    private float dBToLinear(float input) {
        return (float) Math.pow(10, input / 20);
    }

    public enum FrequencyDistribution {
        LINEAR,
        LOGARITHMIC,
        MEL_SCALE,
        BARK_SCALE,
        BARK_ENHANCED,
        ADAPTIVE_BARK
    }

    public static class FrequencyBand {
        final float centerFreq;
        final float lowFreq;
        final float highFreq;
        final int lowBin;
        final int highBin;

        public FrequencyBand(float center, float low, float high, int lowBin, int highBin) {
            this.centerFreq = center;
            this.lowFreq = low;
            this.highFreq = high;
            this.lowBin = lowBin;
            this.highBin = highBin;
        }
    }
}