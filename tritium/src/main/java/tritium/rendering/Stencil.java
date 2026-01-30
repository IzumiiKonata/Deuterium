package tritium.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Stencil {

    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isErasing = false;

    public static void write(Framebuffer givenFramebuffer) {
        Stencil.checkSetupFBO(givenFramebuffer);
        GL11.glClearStencil(0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 65535);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(false, false, false, false);
    }

    public static void write() {

        if (FramebufferCaching.getOverridingFramebuffer() != null) {
            Stencil.write(FramebufferCaching.getOverridingFramebuffer());
            return;
        }

        Stencil.checkSetupFBO(mc.getFramebuffer());
        GL11.glClearStencil(0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 65535);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(false, false, false, false);

    }

    public static void writeNoClear() {
        Stencil.checkSetupFBO(mc.getFramebuffer());
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 65535);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(false, false, false, false);

    }

    public static void erase() {
        isErasing = true;
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 65535);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableBlend();
    }

    public static void eraseInvert() {
        isErasing = true;
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 65535);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableBlend();
    }


    public static void dispose() {
        isErasing = false;
        GL11.glDisable(GL11.GL_STENCIL_TEST);
//        GlStateManager.disableAlpha();
//        GlStateManager.disableBlend();
//        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GlStateManager.colorMask(true, true, true, true);
    }

    public static void checkSetupFBO(Framebuffer fbo) {
//        if (fbo != null && fbo.depthBuffer > -1) {
//            Stencil.setupFBO(fbo);
//            fbo.depthBuffer = -1;
//        }
    }


    public static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(GL30.GL_RENDERBUFFER, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_STENCIL, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, stencil_depth_buffer_ID);
    }
}

