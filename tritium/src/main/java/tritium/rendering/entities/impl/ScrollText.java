package tritium.rendering.entities.impl;

import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import tritium.rendering.RGBA;
import tritium.rendering.Rect;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shaders;
import tritium.utils.timing.Timer;

import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2024/9/17 22:32
 */
public class ScrollText {

    public ScrollText() {

    }

    Timer t = new Timer();

    double scrollOffset = 0;

    String cachedText = "";

    @Setter
    long waitTime = 2500;

    @Setter
    boolean oneShot = false;

    public Animation anim = new Animation(Easing.LINEAR, Duration.ofMillis(0));

    public void reset() {
        t.reset();
        scrollOffset = 0;
        anim.reset();
        anim.setStartValue(0);
        anim.setValue(0);
        leftGradientAlpha = 1f;
        isScrolling = false;
    }

    static Framebuffer fb = null, fbStencil = null;
    float leftGradientAlpha = 1f;
    public boolean isScrolling = false;

    public void render(CFontRenderer fr, String text, double x, double y, double width, int color) {

        if (!cachedText.equals(text)) {
            cachedText = text;

            this.reset();
        }

        double w = fr.getStringWidthD(text);

        if (w > width) {

            isScrolling = true;

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, Minecraft.getMinecraft().displayWidth * .5, Minecraft.getMinecraft().displayHeight * .5, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

//            GL11.glDisable(GL11.GL_STENCIL_TEST);

            fb = RenderSystem.createFrameBuffer(fb);
            fb.bindFramebuffer(true);
            fb.setFramebufferColor(1, 1, 1, 0);
            fb.framebufferClearNoBinding();

            double gradWidth = 6;
            double exp = 2;

            StencilClipManager.beginClip(() -> {
                Rect.draw(x, y - exp, width, fr.getHeight() + exp * 2, -1, Rect.RectType.EXPAND);
            });

            fr.drawString(text, x + scrollOffset, y, color);

            double dest = -(w - width + 4);

            if (anim.getDuration() != 0) {
                scrollOffset = anim.run(dest);
            } else {

                String s = "    ";

                dest = -(w + fr.getStringWidth(s));

                boolean delayed = t.isDelayed(waitTime);
                if (delayed) {
                    scrollOffset = Interpolations.interpLinear((float) scrollOffset, (float) dest, 2f);
                    this.leftGradientAlpha = Interpolations.interpLinear(this.leftGradientAlpha, 0f, 0.5f);
                }

                fr.drawString(s + text, x + w + scrollOffset, y, color);

                double delta = scrollOffset - dest;

                if (delta == 0) {
                    scrollOffset = 0;
                    t.reset();
                }

                if (delta <= gradWidth + 1) {
                    this.leftGradientAlpha = 1f;
                }

            }

            StencilClipManager.endClip();

            fbStencil = RenderSystem.createFrameBuffer(fbStencil);
            fbStencil.bindFramebuffer(true);
            fbStencil.setFramebufferColor(1, 1, 1, 0);
            fbStencil.framebufferClearNoBinding();

            RenderSystem.drawGradientRectLeftToRight(x, y - exp, x + gradWidth, y + fr.getHeight() + exp * 2, RGBA.color(1f, 1f, 1f, this.leftGradientAlpha), 0xFFFFFFFF);
            Rect.draw(x + gradWidth, y - exp, width - gradWidth * 2, fr.getHeight() + exp * 2, -1);
            RenderSystem.drawGradientRectLeftToRight(x + width - gradWidth, y - exp, x + width, y + fr.getHeight() + exp * 2, 0xFFFFFFFF, 0x00FFFFFF);

            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            Shaders.STENCIL.draw(fb.framebufferTexture, fbStencil.framebufferTexture, 0, 0, fb.framebufferWidth * .5, fb.framebufferHeight * .5);
        } else {

            isScrolling = false;

            scrollOffset = 0;
            fr.drawString(text, x, y, color);
        }

    }

}
