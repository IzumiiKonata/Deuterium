package tritium.rendering.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Location;
import tritium.rendering.async.AsyncGLContext;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GlyphGenerator {

    private static Font getFontForGlyph(char ch, Font f, Font... fallBackFonts) {

        if (f.canDisplay(ch)) {
            return f;
        } else {
            if (fallBackFonts != null) {
                for (Font fallBackFont : fallBackFonts) {
                    if (fallBackFont != null && fallBackFont.canDisplay(ch)) {
                        System.out.println("Can't display " + ch);
                        return fallBackFont;
                    }
                }
            }

            return f;
        }

    }

    public static void generate(CFontRenderer fr, char ch, Font f, Location identifier, GlyphLoadedCallback onLoaded) {

        double fontHeight = -1;

        final BufferedImage fontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D fontGraphics = (Graphics2D) fontImage.getGraphics();

        Font font = getFontForGlyph(ch, f, fr.fallBackFonts);

        final FontMetrics fontMetrics = fontGraphics.getFontMetrics(font);
        final FontMetrics fontMetricsOrig = fontGraphics.getFontMetrics(f);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        Rectangle2D stringBounds = fontMetrics.getStringBounds(String.valueOf(ch), fontGraphics);

        int width = (int) Math.ceil(stringBounds.getWidth());
        int height = (int) Math.ceil(stringBounds.getHeight() * 1.3f);

        Glyph glyph = new Glyph(width, height, ch);

        fr.allGlyphs[glyph.value] = glyph;

        if (width == 0) {
            return;
        }

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g2d.setColor(Color.WHITE);

        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setFont(font);

        if (fontMetrics.getHeight() > fontHeight && font == f) {
            fontHeight = stringBounds.getHeight();
        }

        if (font == f) {
            g2d.drawString(String.valueOf(ch), 0, fontMetrics.getAscent());
        } else {
            // 需要和主字体对齐
            g2d.drawString(String.valueOf(ch), 0, fontMetricsOrig.getAscent());
        }

        g2d.dispose();

        onLoaded.onLoaded(fontHeight);

        // okay this is complicated.....
        AsyncGLContext.submit(() -> {

            while (!Minecraft.getMinecraft().loaded) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException ignored) {
                }
            }

            DynamicTexture dynamicTexture = new DynamicTexture(bi, true, false);
            Minecraft.getMinecraft().getTextureManager().loadTexture(identifier, dynamicTexture);

            bi.flush();
            glyph.textureId = dynamicTexture.getGlTextureId();
            glyph.init();
        });

    }



    public interface GlyphLoadedCallback {
        void onLoaded(double fontHeight);
    }
}