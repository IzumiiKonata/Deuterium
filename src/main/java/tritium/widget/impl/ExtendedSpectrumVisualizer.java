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

    // 低频增强参数
    private final float lowFreqBoost = 2.0f; // 低频段频带数量倍增因子
    private final float lowFreqCutoff = 500.0f; // 低频段分界点 (Hz)

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
        float maxFreq = 20000;
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

    /**
     * 增强的Bark分布 - 在低频区域提供更多细分
     */
    private void generateEnhancedBarkBands(float minFreq, float maxFreq) {
        // 计算低频段应该占用的频带数量
        float lowFreqFactor = .3f;
        int lowFreqBands = (int) (numBands * lowFreqFactor); // 40%的频带用于低频
        int highFreqBands = numBands - lowFreqBands;

        // 低频段使用更密集的分布
        float lowFreqMax = Math.min(lowFreqCutoff, maxFreq);
        generateDenseBarkBands(minFreq, lowFreqMax, lowFreqBands);

        // 高频段使用标准Bark分布
        if (lowFreqMax < maxFreq) {
            float barkMin = freqToBark(lowFreqMax * .5f);
            float barkMax = freqToBark(maxFreq);
//            System.out.println("maxFreq * lowFreqFactor: " + (maxFreq * lowFreqFactor) + ", lowFreqMax: " + lowFreqMax);
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

    /**
     * 自适应Bark分布 - 根据频率范围动态调整频带宽度
     */
    private void generateAdaptiveBarkBands(float minFreq, float maxFreq) {
        float barkMin = freqToBark(minFreq);
        float barkMax = freqToBark(maxFreq);

        // 创建非均匀的Bark步长分布
        float[] barkSteps = new float[numBands];
        float totalWeight = 0;

        for (int i = 0; i < numBands; i++) {
            float progress = (float) i / (numBands - 1);
            // 在低频区域使用更大的步长，增强低频表现
            float weight = (float) Math.exp(-progress * 1.2f) + 0.8f;
            barkSteps[i] = weight;
            totalWeight += weight;
        }

        // 标准化步长
        float barkRange = barkMax - barkMin;
        float currentBark = barkMin;
        float prevHighFreq = minFreq; // 记录前一个频带的高频值

        for (int i = 0; i < numBands; i++) {
            float stepSize = (barkSteps[i] / totalWeight) * barkRange;
            float lowFreq = Math.max(prevHighFreq, barkToFreq(currentBark)); // 确保不重叠
            float highFreq = barkToFreq(currentBark + stepSize);
            float centerFreq = (lowFreq + highFreq) / 2;

            // 确保频率不小于最小频率
            lowFreq = Math.max(lowFreq, minFreq);
            highFreq = Math.max(highFreq, lowFreq + 1.0f); // 确保高低频不相等

            int lowBin = freqToBin(lowFreq);
            int highBin = freqToBin(highFreq);
            lowBin = Math.max(0, Math.min(maxBin, lowBin));
            highBin = Math.max(lowBin, Math.min(maxBin, highBin));

            // 确保bin不重叠
            if (i > 0 && lowBin <= bands.get(i-1).highBin) {
                lowBin = bands.get(i-1).highBin + 1;
            }
            highBin = Math.max(lowBin + 1, highBin);

            bands.add(new FrequencyBand(centerFreq, lowFreq, highFreq, lowBin, highBin));
            currentBark += stepSize;
            prevHighFreq = highFreq; // 更新前一个频带的高频值
        }
    }

    /**
     * 在指定频率范围内生成密集的Bark频带
     */
    private void generateDenseBarkBands(float minFreq, float maxFreq, int bandCount) {
        // 在低频区域使用混合分布：部分线性 + 部分Bark
        int linearBands = bandCount / 3; // 1/3使用线性分布
        int barkBands = bandCount - linearBands; // 2/3使用Bark分布

        float midFreq = minFreq + (maxFreq - minFreq) * .35f;

        // 线性分布部分（超低频）
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

        // Bark分布部分（低频）
        float barkMin = freqToBark(midFreq);
        float barkMax = freqToBark(maxFreq);
        float barkStep = (barkMax - barkMin) / barkBands;

        for (int i = 0; i < barkBands; i++) {
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

    private float weightingdB(float freq) {
        float f2 = freq * freq;
        double h = ( Math.pow(1037918.48 - f2, 2) + 1080768.16 * f2 ) / ( Math.pow(9837328 - f2, 2) + 11723776 * f2 );
        double rD = ( freq / 6.8966888496476e-5 ) * Math.sqrt( h / ( ( f2 + 79919.29 ) * ( f2 + 1345600 ) ) );
        return (float) (20 * Math.log10(rD));
    }

    private float binToFreq(int magIn) {
        if (magIn == 0)
            return 1;
        return (float) (magIn * this.sampleRate) / this.fftSize;
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

            // 对低频段应用额外的敏感度调整
            float sensitivity = 1.0f;

            float normalized = (float) Math.pow((value - min) / (max - min), 1 / (1.8 * sensitivity));
            bandMagnitudes[i] = normalized;
        }

        return bandMagnitudes;
    }

    private float clamp(float val, float min, float max) {
        if (val < min) {
            return min;
        }
        return Math.min(val, max);
    }

    private float dBToLinear(float input) {
        return (float) Math.pow(10, input / 20);
    }

    public void printBandInfo() {
        System.out.println("频谱分析器配置：");
        System.out.println("采样率: " + sampleRate + " Hz");
        System.out.println("FFT大小: " + fftSize);
        System.out.println("频带数量: " + bands.size());
        System.out.println("频率分辨率: " + (sampleRate / (float) fftSize) + " Hz/bin");

        System.out.println("\n前10个频带：");
        for (int i = 0; i < Math.min(10, bands.size()); i++) {
            FrequencyBand band = bands.get(i);
            System.out.printf("频带 %d: %.1fHz (%.1f-%.1fHz) bins[%d-%d] 带宽:%.1fHz\n",
                    i, band.centerFreq, band.lowFreq, band.highFreq, band.lowBin, band.highBin,
                    band.highFreq - band.lowFreq);
        }

        System.out.println("\n后5个频带：");
        for (int i = Math.max(0, bands.size() - 5); i < bands.size(); i++) {
            FrequencyBand band = bands.get(i);
            System.out.printf("频带 %d: %.1fHz (%.1f-%.1fHz) bins[%d-%d] 带宽:%.1fHz\n",
                    i, band.centerFreq, band.lowFreq, band.highFreq, band.lowBin, band.highBin,
                    band.highFreq - band.lowFreq);
        }

        // 统计低频段信息
        int lowFreqBandCount = 0;
        for (FrequencyBand band : bands) {
            if (band.centerFreq < lowFreqCutoff) {
                lowFreqBandCount++;
            }
        }
        System.out.println("\n低频段统计：");
        System.out.println("低频段频带数量: " + lowFreqBandCount + " / " + bands.size());
        System.out.println("低频段占比: " + String.format("%.1f", (float)lowFreqBandCount / bands.size() * 100) + "%");
    }

    public enum FrequencyDistribution {
        LINEAR,
        LOGARITHMIC,
        MEL_SCALE,
        BARK_SCALE,
        BARK_ENHANCED,    // 增强的Bark分布（推荐）
        ADAPTIVE_BARK     // 自适应Bark分布
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