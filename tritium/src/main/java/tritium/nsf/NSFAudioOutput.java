package tritium.nsf;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class NSFAudioOutput {

    private SourceDataLine line;
    private byte[] bytes;
    private boolean started = false;

    public NSFAudioOutput(int sampleRate) {
        AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, false);
        int bufferBytes = (int) (sampleRate * 2 * 0.12) & ~1;
        try {
            line = AudioSystem.getSourceDataLine(af);
            line.open(af, bufferBytes);
        } catch (LineUnavailableException e) {
            line = null;
        }
    }

    public boolean isAvailable() {
        return line != null;
    }

    public void writeSamples(short[] src, int off, int len) {
        if (line == null || len <= 0) {
            return;
        }

        int need = len * 2;
        if (bytes == null || bytes.length < need) {
            bytes = new byte[need];
        }

        int dptr = 0;
        for (int i = 0; i < len; i++) {
            short sample = src[off + i];
            bytes[dptr++] = (byte) sample;
            bytes[dptr++] = (byte) ((sample >> 8) & 0xFF);
        }

        if (!started) {
            line.start();
            started = true;
        }

        line.write(bytes, 0, need);
    }

    public void pause() {
        if (line != null) {
            line.stop();
        }
    }

    public void resume() {
        if (line != null && started) {
            line.start();
        }
    }

    public void flush() {
        if (line != null) {
            line.flush();
        }
    }

    public void close() {
        if (line != null) {
            line.stop();
            line.flush();
            line.close();
            line = null;
        }
    }
}
