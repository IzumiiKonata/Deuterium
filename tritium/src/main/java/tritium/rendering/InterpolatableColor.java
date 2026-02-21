package tritium.rendering;

import tritium.rendering.animation.Interpolations;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 10:56
 */
public class InterpolatableColor {

    private float r, g, b, a;

    public InterpolatableColor(int hexColor) {
        this(((hexColor >> 24) & 0xFF) / 255.0f, ((hexColor >> 16) & 0xFF) / 255.0f, ((hexColor >> 8) & 0xFF) / 255.0f, (hexColor & 0xFF) / 255.0f);
    }

    public InterpolatableColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int getHexColor() {
        return (int) (r * 255) << 24 | (int) (g * 255) << 16 | (int) (b * 255) << 8 | (int) (a * 255);
    }

    public void interpolate(float r, float g, float b, float a, float speed) {
        this.r = Interpolations.interpolate(this.r, r, speed);
        this.g = Interpolations.interpolate(this.g, g, speed);
        this.b = Interpolations.interpolate(this.b, b, speed);
        this.a = Interpolations.interpolate(this.a, a, speed);
    }

    public void interpolate(int hexColor, float speed) {
        interpolate(((hexColor >> 24) & 0xFF) / 255.0f, ((hexColor >> 16) & 0xFF) / 255.0f, ((hexColor >> 8) & 0xFF) / 255.0f, (hexColor & 0xFF) / 255.0f, speed);
    }

}
