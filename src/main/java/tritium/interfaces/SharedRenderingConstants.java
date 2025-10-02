package tritium.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjglx.opengl.Display;
import tritium.management.ThemeManager;
import tritium.rendering.FramebufferCaching;
import tritium.rendering.GLAction;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.ShaderRenderType;
import tritium.rendering.shader.Shaders;
import tritium.settings.ClientSettings;
import tritium.utils.timing.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static tritium.rendering.rendersystem.RenderSystem.DIVIDE_BY_255;


/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public interface SharedRenderingConstants {

    default void roundedRectTextured(double x, double y, double width, double height, double texX, double texY, double u, double v, double radius, double expand, float alpha) {
        Shaders.RQT_SHADER.drawSpecial(x - expand, y - expand, width + expand * 2, height + expand * 2, texX, texY, u, v, radius, alpha);
    }

    default void roundedOutlineGradient(double x, double y, double width, double height, double radius, double thickness, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        Shaders.ROGQ_SHADER.draw(x, y, width, height, radius, thickness, bottomLeft, topLeft, bottomRight, topRight);
    }

    default void roundedOutline(double x, double y, double width, double height, double radius, double thickness, Color outline) {
        Shaders.ROQ_SHADER.draw(x, y, width, height, radius, thickness, outline);
    }

    default void roundedOutline(double x, double y, double width, double height, double radius, double thickness, double expand, Color outline) {
        this.roundedOutline(x - expand, y - expand, width + expand * 2, height + expand * 2, radius, thickness, outline);
    }

    default void roundedRect(double x, double y, double width, double height, double radius, float r, float g, float b, float a) {
        Shaders.RQ_SHADER.draw(x, y, width, height, radius, r, g, b, a);
    }

    default void roundedRect(double x, double y, double width, double height, double radius, int r, int g, int b, int a) {
        Shaders.RQ_SHADER.draw(x, y, width, height, radius, r, g, b, a);
    }

    default void roundedRect(double x, double y, double width, double height, double radius, int color) {

        float f = (color >> 24 & 255) * DIVIDE_BY_255;
        float f1 = (color >> 16 & 255) * DIVIDE_BY_255;
        float f2 = (color >> 8 & 255) * DIVIDE_BY_255;
        float f3 = (color & 255) * DIVIDE_BY_255;

        Shaders.RQ_SHADER.draw(x, y, width, height, radius, f1, f2, f3, f);
    }

    default void roundedRect(double x, double y, double width, double height, double radius, Color color) {
        Shaders.RQ_SHADER.draw(x, y, width, height, radius, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    default void roundedRect(double x, double y, double width, double height, double radius, double expand, Color color) {
        Shaders.RQ_SHADER.draw(x - expand, y - expand, width + expand * 2, height + expand * 2, radius, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    default void roundedRectTextured(double x, double y, double width, double height, double radius) {
        Shaders.RQT_SHADER.draw(x, y, width, height, radius, 1);
    }

    default void roundedRectTextured(double x, double y, double width, double height, double radius, float alpha) {
        Shaders.RQT_SHADER.draw(x, y, width, height, radius, alpha);
    }

    default void roundedRectTextured(double x, double y, double width, double height, double texX, double texY, double u, double v, double radius) {
        Shaders.RQT_SHADER.draw(x, y, width, height, texX, texY, u, v, radius, 1);
    }

    default void roundedRectTextured(double x, double y, double width, double height, double texX, double texY, double u, double v, double radius, float alpha) {
        Shaders.RQT_SHADER.draw(x, y, width, height, texX, texY, u, v, radius, alpha);
    }

    default void roundedRectGradientHorizontal(double x, double y, double width, double height, double radius, Color left, Color right) {
        Shaders.RQG_SHADER.draw(x, y, width, height, radius, left, left, right, right);
    }

    default void roundedRectGradientVertical(double x, double y, double width, double height, double radius, Color top, Color bottom) {
        Shaders.RQG_SHADER.draw(x, y, width, height, radius, bottom, top, bottom, top);
    }

    default void drawGradientCornerLR(double x, double y, double width, double height, double radius, Color topLeft, Color bottomRight) {
        Color mixedColor = evenAdd(topLeft, bottomRight);
        Shaders.RQG_SHADER.draw(x, y, width, height, radius, mixedColor, topLeft, bottomRight, mixedColor);
    }

    default void drawGradientCornerRL(double x, double y, double width, double height, double radius, Color bottomLeft, Color topRight) {
        Color mixedColor = evenAdd(bottomLeft, topRight);
        Shaders.RQG_SHADER.draw(x, y, width, height, radius, bottomLeft, mixedColor, mixedColor, topRight);
    }

    default Color evenAdd(Color a, Color b) {
        return new Color(a.getRed() / 2 + b.getRed() / 2, a.getGreen() / 2 + b.getGreen() / 2, a.getBlue() / 2 + b.getBlue() / 2);
    }

    default double getWidth() {
        return RenderSystem.getWidth();
    }

    default double getHeight() {
        return RenderSystem.getHeight();
    }

    default int hexColor(int red, int green, int blue) {
        return hexColor(red, green, blue, 255);
    }

    default int hexColor(int red, int green, int blue, int alpha) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    default int reAlpha(int color, float alpha) {
        if (alpha > 1) {
            alpha = 1;
        }

        if (alpha < 0) {
            alpha = 0;
        }
        return RenderSystem.hexColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, (int) (alpha * 255));
    }

    default boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return RenderSystem.isHovered(mouseX, mouseY, x, y, width, height);
    }

    default void bloomAndBlur(Runnable r) {

        BLOOM.add(r);
        BLUR.add(r);

    }

    default void doGl(Runnable render, GLAction... actions) {

        for (GLAction action : actions) {
            action.before.run();
        }

        render.run();

        for (int i = actions.length - 1; i >= 0; i--) {
            GLAction action = actions[i];
            action.after.run();
        }

    }

    default void matrix(Runnable render) {
        GlStateManager.pushMatrix();

        render.run();

        GlStateManager.popMatrix();
    }

    default void scaleAtPos(double posX, double posY, double scale) {
        GlStateManager.translate(posX, posY, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-posX, -posY, 0);
    }

    default void rotateAtPos(double posX, double posY, float rotate) {
        GlStateManager.translate(posX, posY, 0);
        GlStateManager.rotate(rotate, 0, 0, 1);
        GlStateManager.translate(-posX, -posY, 0);
    }


    static void render2D(boolean shaders) {

        PRE_SHADER.forEach(Runnable::run);

        if (shaders) {

            if (ClientSettings.RENDER_BLUR.getValue() && !BLUR.isEmpty())
                Shaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, BLUR);

            if (ClientSettings.RENDER_GLOW.getValue() && !BLOOM.isEmpty())
                Shaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, BLOOM);
        }

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

        // 性能优化魔法: 使用帧缓冲减少屏上内容渲染的次数
        boolean shouldUseCaching = ClientSettings.RENDER2D_FRAMERATE.getValue() != 0;

        if (shouldUseCaching) {

            FramebufferCaching.render2DNormalBuffer = RenderSystem.createFrameBuffer(FramebufferCaching.render2DNormalBuffer);
            Framebuffer buf = FramebufferCaching.render2DNormalBuffer;
            buf.setFramebufferColor(0, 0, 0, 0.0F);

            int freq = ClientSettings.RENDER2D_FRAMERATE.getValue();

            if (freq == ClientSettings.RENDER2D_FRAMERATE.getMaximum()) {
                freq = Display.getDesktopDisplayMode().getFrequency();
            }

            if (updateTimer.isDelayed(1000 / freq)) {

                updateTimer.reset();

                buf.bindFramebuffer(true);
                buf.framebufferClearNoBinding();

                GlStateManager.enableTexture2D();

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

                FramebufferCaching.setOverridingFramebuffer(FramebufferCaching.render2DNormalBuffer);

                double deltaTime = RenderSystem.getFrameDeltaTime();

                double delta = 100.0 / freq;

                RenderSystem.setFrameDeltaTime(Math.max(deltaTime, delta));

                try {
                    // 这里是你 render2d 的内容
                    NORMAL.forEach(Runnable::run);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                RenderSystem.setFrameDeltaTime(deltaTime);

                FramebufferCaching.removeCurrentlyBinding();

            }

            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

            GlStateManager.disableAlpha();

            GlStateManager.enableTexture2D();

            GlStateManager.color(1, 1, 1, 1);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager.bindTexture(buf.framebufferTexture);
            ShaderProgram.drawQuad();

            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);


        } else {
            NORMAL.forEach(Runnable::run);
        }

        AFTER.forEach(Runnable::run);

    }

    Timer updateTimer = new Timer();

    static void render3DRunnables() {

        if (ClientSettings.RENDER_GLOW.getValue()) {

            if (!BLOOM.isEmpty()) {
                Shaders.POST_BLOOM_SHADER.run(ShaderRenderType.CAMERA, SharedRenderingConstants.BLOOM);

            }

            if (!UI_BLOOM_RUNNABLES.isEmpty()) {
                Shaders.UI_BLOOM_SHADER.run(ShaderRenderType.CAMERA, SharedRenderingConstants.UI_BLOOM_RUNNABLES);

            }

        }
        if (ClientSettings.RENDER_BLUR.getValue() && !BLUR.isEmpty())
            Shaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.CAMERA, SharedRenderingConstants.BLUR);
    }

    static void clearRunnables() {
        BLUR.clear();
        BLOOM.clear();
        PRE_SHADER.clear();
        UI_BLOOM_RUNNABLES.clear();
//        UI_RENDER_RUNNABLES.clear();
        NORMAL.clear();
        AFTER.clear();
        UI_POST_BLOOM_RUNNABLES.clear();

//        LIMITED_PRE_RENDER_RUNNABLES.clear();
//        LIMITED_POST_RENDER_RUNNABLES.clear();
    }

    List<Runnable> UI_BLOOM_RUNNABLES = new ArrayList<>();
    List<Runnable> UI_POST_BLOOM_RUNNABLES = new ArrayList<>();

    List<Runnable> BLUR = new ArrayList<>();
    List<Runnable> BLOOM = new ArrayList<>();
    List<Runnable> PRE_SHADER = new ArrayList<>();
    List<Runnable> NORMAL = new ArrayList<>();
    List<Runnable> AFTER = new ArrayList<>();

}
