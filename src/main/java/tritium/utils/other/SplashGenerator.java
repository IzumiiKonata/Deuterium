package tritium.utils.other;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.texture.DynamicTexture;
import tritium.Tritium;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
@UtilityClass
public class SplashGenerator {

    public final BufferedImage logo = SplashGenerator.generate(1000, 300);

    public final DynamicTexture t = new DynamicTexture(logo);
    public final DynamicTexture t_small = new DynamicTexture(SplashGenerator.generateSmall(logo.getWidth(), logo.getHeight()));

    public BufferedImage generate(int width, int height) {

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Font font = new Font("Calibri", Font.BOLD | Font.ITALIC, 200);

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

//        g.setColor(new Color(23, 23, 23));
//        g.drawRect(0, 0, width, height);

        g.setColor(Color.GRAY);
        g.drawString(Tritium.NAME, width * 0.5f - fm.stringWidth(Tritium.NAME) * 0.5f + 3, height * 0.5f + fm.getHeight() * 0.25f + 3);

        g.setColor(new Color(231, 231, 231));
        g.drawString(Tritium.NAME, width * 0.5f - fm.stringWidth(Tritium.NAME) * 0.5f, height * 0.5f + fm.getHeight() * 0.25f);

        return img;
    }

    public BufferedImage generateSmall(int width, int height) {

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.translate(width / 2.0, height / 2.0);
        g.scale(0.6, 0.6);
        g.translate(-width / 2.0, -height / 2.0);

        g.drawImage(logo, 0, 0, null);

        return img;

    }

}
