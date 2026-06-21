package tritium.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public class StencilClipManager {
    public static class StencilState {
        int stencilValue;
        boolean colorMask;
        boolean depthMask;

        StencilState(int value, boolean color, boolean depth) {
            this.stencilValue = value;
            this.colorMask = color;
            this.depthMask = depth;
        }
    }

    private static final Deque<Framebuffer> clipBufferStack = new ArrayDeque<>();

    private static Framebuffer ensureBound() {
        if (Framebuffer.currentlyBinding == null) {
            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        }
        return Framebuffer.currentlyBinding;
    }

    public static boolean stencilClipping() {
        Framebuffer fb = Framebuffer.currentlyBinding;
        return fb != null && !fb.stencilStack.isEmpty();
    }

    public static void initialize() {
        Stencil.checkSetupFBO(Framebuffer.currentlyBinding);
        GL11.glClearStencil(0);
        GlStateManager.enableDepth();
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GlStateManager.enableStencilTest();
    }

    static boolean depthMask = false;

    public static void beginClip() {
        Framebuffer fb = ensureBound();
        clipBufferStack.push(fb);

        if (fb.currentStencilValue == 0) {
            initialize();
        }

        boolean colorMask = GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK);
        depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        fb.stencilStack.push(new StencilState(fb.currentStencilValue, colorMask, depthMask));

        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.depthMask(false);

        if (fb.currentStencilValue == 0) {
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        } else {
            GL11.glStencilFunc(GL11.GL_EQUAL, fb.currentStencilValue, 0xFF);
        }

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
    }

    public static void updateClip() {
        Framebuffer fb = ensureBound();
        fb.currentStencilValue++;

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(depthMask);

        GL11.glStencilFunc(GL11.GL_EQUAL, fb.currentStencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    public static void beginClip(Runnable drawClipShape) {
        beginClip();

        drawClipShape.run();

        updateClip();
    }

    public static void endClip() {
        Framebuffer fb = clipBufferStack.isEmpty() ? Framebuffer.currentlyBinding : clipBufferStack.pop();

        if (fb == null) {
            return;
        }

        if (Framebuffer.currentlyBinding != fb) {
            fb.bindFramebuffer(false);
        }

        if (fb.stencilStack.isEmpty()) {
            System.err.println("Stencil stack underflow");
            return;
        }

        StencilState state = fb.stencilStack.pop();
        fb.currentStencilValue = state.stencilValue;

        GlStateManager.colorMask(state.colorMask, state.colorMask, state.colorMask, state.colorMask);
        GlStateManager.depthMask(state.depthMask);

        if (fb.currentStencilValue > 0) {
            GL11.glStencilFunc(GL11.GL_EQUAL, fb.currentStencilValue, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        } else {
            disable();
        }
    }

    public static void disable() {
        GlStateManager.disableStencilTest();
        if (Framebuffer.currentlyBinding != null) {
            Framebuffer.currentlyBinding.currentStencilValue = 0;
        }
    }

    // 清除模板缓冲区
    public static void clear() {
        Framebuffer fb = Framebuffer.currentlyBinding;
        if (fb != null) {
            fb.currentStencilValue = 0;
            fb.stencilStack.clear();
            clipBufferStack.removeIf(f -> f == fb);
        } else {
            clipBufferStack.clear();
        }
    }
}
