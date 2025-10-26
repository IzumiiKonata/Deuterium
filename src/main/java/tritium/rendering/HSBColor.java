package tritium.rendering;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.awt.*;

@Getter
@Setter
public class HSBColor extends Color implements Cloneable {
    float hue, saturation, brightness;
    int alpha;

    public HSBColor(float hue, float saturation, float brightness, int alpha) {
        super(alpha << 24 | Color.HSBtoRGB(hue, saturation, brightness), true);
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.alpha = alpha;
    }

    public HSBColor(int red, int green, int blue, int alpha) {
        super(red, green, blue, alpha);
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = alpha;
    }

    @SneakyThrows
    public final HSBColor clone() {
        return (HSBColor) super.clone();
//        return new HSBColor(hue, saturation, brightness, alpha);
    }

    public Color getColor() {
        return resetAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    public Color getColor(int alpha) {
        return resetAlpha(Color.getHSBColor(hue, saturation, brightness), alpha);
    }

    @Override
    public String toString() {
        return hue + ":" + saturation + ":" + brightness + ":" + alpha;
    }

    private Color resetAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

}
