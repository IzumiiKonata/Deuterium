package tritium.rendering;

import net.minecraft.client.shader.Framebuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author IzumiiKonata
 * @since 2024/9/21 21:53
 */
public class FramebufferCaching {

    public static Framebuffer render2DNormalBuffer;

    private static final List<Framebuffer> LIST = new ArrayList<>();

    public static void setOverridingFramebuffer(Framebuffer buffer) {
        LIST.addFirst(buffer);
    }

    public static Framebuffer getOverridingFramebuffer() {

        if (LIST.isEmpty())
            return null;

        return LIST.getFirst();
    }

    public static void removeCurrentlyBinding() {
        LIST.removeFirst();
    }

}
