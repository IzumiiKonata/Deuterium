package tritium.utils.other;

import lombok.experimental.UtilityClass;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/11/8 20:18
 */
@UtilityClass
public class MemoryTracker {

    public /*synchronized */ByteBuffer memAlloc(int size) {
        return MemoryUtil.memAlloc(size);
    }

    public /*synchronized */ByteBuffer memRealloc(ByteBuffer ptr, int size) {
        return MemoryUtil.memRealloc(ptr, size);
    }

    public /*synchronized */IntBuffer memAllocInt(int size) {
        return MemoryUtil.memAllocInt(size);
    }
    
    public /*synchronized */void memFree(Buffer buffer) {
        if (buffer == null)
            return;

        MemoryUtil.memFree(buffer);
    }
     
}
