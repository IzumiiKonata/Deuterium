package tritium.rendering;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.rendersystem.RenderSystem;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RoundedRect {
    double lastx;
    double lasty;
    double lastwidth;
    double lastheight;
    double lastradius;
    int color;
    protected DynamicTexture tex;

    public RoundedRect() {
        super();
    }

    public void draw(double x, double y, double width, double height, double radius, int color) {
        if (lastx != x || lasty != y || lastwidth != width || lastheight != height || lastradius != radius) {

            AsyncGLContext.submit(() -> {
                BufferedImage bufferedImage = new BufferedImage((int) width * 2, (int) height * 2, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
                g.setColor(Color.WHITE);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g.fillRoundRect(0, 0, (int) (width * 2), (int) (height * 2), (int) radius, (int) radius);

                tex = new DynamicTexture(bufferedImage);
            });

            lastx = x;
            lasty = x;
            lastwidth = width;
            lastheight = height;
            lastradius = radius;
        }

        if (tex != null) {
            GL11.glPushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(0.5D, 0.5D, 0.5D);
            GlStateManager.translate(-x, -y, 0);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            RenderSystem.color(color);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(tex.getGlTextureId());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getGlTextureId());

            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            GL11.glPopMatrix();
        }
    }
}
