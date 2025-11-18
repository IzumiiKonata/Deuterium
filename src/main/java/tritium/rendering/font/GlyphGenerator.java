package tritium.rendering.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Location;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 字形生成器
 * @author IzumiKonata
 * Date 我忘了
 */
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

    private static int getMaxFontHeight(Font originalFont, Font[] fallbackFonts) {
        int maxHeight = fontGraphics.getFontMetrics(originalFont).getAscent() +
                fontGraphics.getFontMetrics(originalFont).getDescent();

        if (fallbackFonts != null) {
            for (Font fallbackFont : fallbackFonts) {
                if (fallbackFont != null) {
                    FontMetrics fm = fontGraphics.getFontMetrics(fallbackFont);
                    int height = fm.getAscent() + fm.getDescent();
                    maxHeight = Math.max(maxHeight, height);
                }
            }
        }

        return maxHeight;
    }

    static final BufferedImage fontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    static final Graphics2D fontGraphics = (Graphics2D) fontImage.getGraphics();

    public static void generate(CFontRenderer fr, char ch, Font originalFont, Location identifier, GlyphLoadedCallback onLoaded) {
        Font fallbackFont = getFontForGlyph(ch, originalFont, fr.fallBackFonts);

        final FontMetrics fontMetrics = fontGraphics.getFontMetrics(fallbackFont);

        transformation.setToIdentity();
        GlyphVector gv = fallbackFont.createGlyphVector(context, String.valueOf(ch));
        int width = (int) Math.ceil(gv.getGlyphMetrics(0).getAdvance());
        int height = getMaxFontHeight(originalFont, fr.fallBackFonts);

        Glyph glyph = new Glyph(width, height, ch);

        fr.allGlyphs[ch] = glyph;

        if (width == 0) {
            return;
        }

        MultiThreadingUtil.runAsync(() -> {
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

            g2d.setFont(fallbackFont);

            int baselineY = (height + fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
            g2d.drawString(String.valueOf(ch), 0, baselineY);

            g2d.dispose();
            fontImage.flush();

            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {
                    int rgb = bi.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    bi.setRGB(x, y, (alpha << 24) | 0xFFFFFF);
                }
            }

            onLoaded.onLoaded(height);

            DynamicTexture dynamicTexture = new DynamicTexture(bi, true, true);
            Minecraft.getMinecraft().getTextureManager().loadTexture(identifier, dynamicTexture);

            bi.flush();
            glyph.textureId = dynamicTexture.getGlTextureId();
            MultiThreadingUtil.runOnMainThread(glyph::init);
        });

    }



    public interface GlyphLoadedCallback {
        void onLoaded(double fontHeight);
    }
}