package net.optifine.util;

import io.netty.util.internal.PlatformDependent;
import net.minecraft.src.Config;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NativeMemory {

    final static List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);

    public static long getBufferAllocated() {
        long sum = 0L;

        for (BufferPoolMXBean pool : pools) {
            sum += pool.getMemoryUsed();
        }

        return sum;
    }

    public static long getBufferMaximum() {
        long sum = 0L;

        for (BufferPoolMXBean pool : pools) {
            sum += pool.getTotalCapacity();
        }

        return sum;
    }

}
