package processing.sound;

import lombok.Getter;

public class WaveformVisualizer {
    @Getter
    private float[] displayBuffer = new float[1024];
    private int sampleRate;
    private float timeWindow = 0.05f; // 50ms的时间窗口
    private int samplesPerWindow;
    
    public WaveformVisualizer(int sampleRate) {
        this.sampleRate = sampleRate;
        this.samplesPerWindow = (int)(sampleRate * timeWindow);
    }
    
    public void updateDisplay(float[] audioBuffer, int writePosition) {
        // 计算固定时间窗口的起始位置
        int startPos = writePosition - samplesPerWindow;
        if (startPos < 0) {
            startPos += audioBuffer.length; // 处理循环buffer
        }
        
        // 从固定时间窗口提取数据到显示buffer
        for (int i = 0; i < 1024; i++) {
            int sourceIndex = startPos + (i * samplesPerWindow / 1024);
            sourceIndex %= audioBuffer.length;
            displayBuffer[i] = audioBuffer[sourceIndex];
        }
    }
}