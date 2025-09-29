package tech.konata.phosphate.screens.multiplayer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.special.ShaderRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RoundedRectWithTriangle {
    double lastwidth;
    double lastheight;
    double lastradius;
    DynamicTexture tex;

    public RoundedRectWithTriangle() {
        super();
    }

    public void draw(double x, double y, double width, double height, double radius, int color)  {
        if (new Color(color).getAlpha() == 0) return;
        if (tex == null || lastwidth != width || lastheight != height || lastradius != radius) {
            BufferedImage bufferedImage = new BufferedImage((int) width * 2 + 20, (int) height * 2 + 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            g.setColor(Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            Polygon triangle = new Polygon();
            int w = 8;
                triangle.addPoint((int) (width) + 12, 0);
                triangle.addPoint((int) (width - w / 2) + 12, 5);
                triangle.addPoint((int) (width + w / 2) + 12, 5);
            g.fillPolygon(triangle);
                g.fillRoundRect(0, 5, (int) (width * 2), (int) (height * 2), (int) radius, (int) radius);

            tex = new DynamicTexture(bufferedImage);
        }

        lastwidth = width;
        lastheight = height;
        lastradius = radius;
        GlStateManager.bindTexture(tex.getGlTextureId());
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        RenderSystem.color(color);
        ShaderRenderer.drawBind(x * 2, y * 2, width * 2 + 20, height * 2 + 20);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
    }
}
