package tritium.rendering.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Location;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjglx.opengl.GLContext;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.rendersystem.RenderSystem;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GlyphGenerator {

    static final AffineTransform transformation = new AffineTransform();
    static final FontRenderContext context = new FontRenderContext(transformation, true, true);

    private static boolean canFontDisplayChar(Font f, char ch) {
//        transformation.setToIdentity();
//        return f.createGlyphVector(context, String.valueOf(ch)).getGlyphCode(0) != 0;
        return f.canDisplay(ch);
    }

    private static Font getFontForGlyph(char ch, Font f, Font... fallBackFonts) {

        if (!canFontDisplayChar(f, ch)) {
            if (fallBackFonts != null) {
                for (Font fallBackFont : fallBackFonts) {
                    if (fallBackFont != null && canFontDisplayChar(fallBackFont, ch)) {
//                        System.out.println("Can't display " + ch);
                        return fallBackFont;
                    }
                }
            }
        }

        return f;

    }

    static final BufferedImage fontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    static final Graphics2D fontGraphics = (Graphics2D) fontImage.getGraphics();

    public static void generate(CFontRenderer fr, char ch, Font f, Location identifier, GlyphLoadedCallback onLoaded) {

        double fontHeight = -1;

        Font font = getFontForGlyph(ch, f, fr.fallBackFonts);

        final FontMetrics fontMetrics = fontGraphics.getFontMetrics(font);
        final FontMetrics fontMetricsOrig = fontGraphics.getFontMetrics(f);
//        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
//        Rectangle2D stringBounds = fontMetrics.getStringBounds(String.valueOf(ch), fontGraphics);

        transformation.setToIdentity();
        GlyphVector gv = font.createGlyphVector(context, String.valueOf(ch));
//        Rectangle2D bounds = gv.getVisualBounds();
        int width = (int) Math.ceil(gv.getGlyphMetrics(0).getAdvance());
        int height = fontMetrics.getAscent() + fontMetrics.getDescent();

        Glyph glyph = new Glyph(width, height, ch);

        fr.allGlyphs[ch] = glyph;

        if (width == 0) {
            return;
        }
        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 255));
        g2d.setComposite(AlphaComposite.Src);

        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

        g2d.setFont(font);

        if (fontMetrics.getHeight() > fontHeight && font == f) {
            fontHeight = fontMetrics.getAscent() + fontMetrics.getDescent();
        }

        if (font == f) {
            g2d.drawString(String.valueOf(ch), 0, fontMetrics.getAscent());
        } else {
            // 需要和主字体对齐
            g2d.drawString(String.valueOf(ch), 0, fontMetricsOrig.getAscent());
        }

        g2d.dispose();
        fontImage.flush();

        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int rgb = bi.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                bi.setRGB(x, y, (alpha << 24) | 0xFFFFFF);
            }
        }

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
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glyph.textureId);
            RenderSystem.linearFilter();

//            if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
//                float maxAnisotropy = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
//                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
//                        Math.min(16.0f, maxAnisotropy));
//            }
            glyph.init();
        });

    }



    public interface GlyphLoadedCallback {
        void onLoaded(double fontHeight);
    }
}