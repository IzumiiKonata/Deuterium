package tritium.nsf;

import tritium.zdream.nsfplayer.mixer.IMixerChannel;

import java.util.concurrent.locks.ReentrantLock;

public class ScopeTap implements IMixerChannel {

    private final byte channelCode;
    private final String name;
    private final IMixerChannel delegate;
    private final int cyclesPerFrame;

    private int[] stepTime = new int[4096];
    private int[] stepValue = new int[4096];
    private int stepCount = 0;
    private float heldValue = 0f;

    private final float[] ring;
    private final int ringSize;
    private int writePos = 0;

    private final ReentrantLock lock = new ReentrantLock();

    public ScopeTap(byte channelCode, String name, IMixerChannel delegate, int cyclesPerFrame, int ringSize) {
        this.channelCode = channelCode;
        this.name = name;
        this.delegate = delegate;
        this.cyclesPerFrame = Math.max(1, cyclesPerFrame);
        this.ringSize = ringSize;
        this.ring = new float[ringSize];
    }

    public byte getChannelCode() {
        return channelCode;
    }

    public String getName() {
        return name;
    }

    public IMixerChannel getDelegate() {
        return delegate;
    }

    @Override
    public void setLevel(float level) {
        if (delegate != null) {
            delegate.setLevel(level);
        }
    }

    @Override
    public float getLevel() {
        return delegate != null ? delegate.getLevel() : 0f;
    }

    @Override
    public void reset() {
        if (delegate != null) {
            delegate.reset();
        }
        stepCount = 0;
    }

    @Override
    public void mix(int value, int time) {
        if (delegate != null) {
            delegate.mix(value, time);
        }

        if (stepCount >= stepTime.length) {
            int[] nt = new int[stepTime.length << 1];
            int[] nv = new int[stepValue.length << 1];
            System.arraycopy(stepTime, 0, nt, 0, stepCount);
            System.arraycopy(stepValue, 0, nv, 0, stepCount);
            stepTime = nt;
            stepValue = nv;
        }

        stepTime[stepCount] = time;
        stepValue[stepCount] = value;
        stepCount++;
    }

    public void endFrame(int samples) {
        if (samples <= 0) {
            stepCount = 0;
            return;
        }

        lock.lock();
        try {
            int cursor = 0;
            float held = heldValue;
            int wp = writePos;

            for (int i = 0; i < stepCount; i++) {
                int idx = (int) Math.round((double) stepTime[i] * samples / cyclesPerFrame);
                if (idx < 0) {
                    idx = 0;
                } else if (idx > samples) {
                    idx = samples;
                }

                while (cursor < idx) {
                    ring[wp] = held;
                    if (++wp == ringSize) {
                        wp = 0;
                    }
                    cursor++;
                }

                held = stepValue[i];
            }

            while (cursor < samples) {
                ring[wp] = held;
                if (++wp == ringSize) {
                    wp = 0;
                }
                cursor++;
            }

            heldValue = held;
            writePos = wp;
        } finally {
            lock.unlock();
        }

        stepCount = 0;
    }

    public void snapshot(float[] dst, int count) {
        lock.lock();
        try {
            int start = writePos - count;
            for (int i = 0; i < count; i++) {
                int p = (start + i) % ringSize;
                if (p < 0) {
                    p += ringSize;
                }
                dst[i] = ring[p];
            }
        } finally {
            lock.unlock();
        }
    }
}
