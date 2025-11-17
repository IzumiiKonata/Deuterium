package tritium.rendering;

import lombok.experimental.UtilityClass;
import tritium.utils.math.Mth;

/**
 * @author IzumiiKonata
 * Date: 2025/11/16 12:25
 */
@UtilityClass
public class ARGB {

    public int alpha(int color) {
        return color >>> 24;
    }

    public int red(int color) {
        return color >> 16 & 0xFF;
    }

    public int green(int color) {
        return color >> 8 & 0xFF;
    }

    public int blue(int color) {
        return color & 0xFF;
    }

    public int color(int red, int green, int blue, int alpha) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public int color(int red, int green, int blue) {
        return ARGB.color(red, green, blue, 255);
    }

    public int color(float red, float green, float blue, float alpha) {
        return ARGB.color(Mth.floor(red * 255.0f), Mth.floor(green * 255.0f), Mth.floor(blue * 255.0f), Mth.floor(alpha * 255.0f));
    }

    public static int color(float red, float green, float blue) {
        return ARGB.color(red, green, blue, 1.0f);
    }

    public static int greyscale(int color) {
        int greyscale = (int)((float)ARGB.red(color) * 0.3f + (float)ARGB.green(color) * 0.59f + (float)ARGB.blue(color) * 0.11f);
        return ARGB.color(greyscale, greyscale, greyscale, ARGB.alpha(color));
    }

    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    public static int transparent(int color) {
        return color & 0xFFFFFF;
    }

    public static int color(int rgb, int alpha) {
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    public static int color(int rgb, float alpha) {
        return ARGB.as8BitChannel(alpha) << 24 | rgb & 0xFFFFFF;
    }

    public static int white(float alpha) {
        return ARGB.as8BitChannel(alpha) << 24 | 0xFFFFFF;
    }

    public static int white(int alpha) {
        return alpha << 24 | 0xFFFFFF;
    }

    public static int black(float alpha) {
        return ARGB.as8BitChannel(alpha) << 24;
    }

    public static int black(int alpha) {
        return alpha << 24;
    }

    public static int as8BitChannel(float value) {
        return Mth.floor(value * 255.0f);
    }

    public static float alphaFloat(int color) {
        return ARGB.from8BitChannel(ARGB.alpha(color));
    }

    public static float redFloat(int color) {
        return ARGB.from8BitChannel(ARGB.red(color));
    }

    public static float greenFloat(int color) {
        return ARGB.from8BitChannel(ARGB.green(color));
    }

    public static float blueFloat(int color) {
        return ARGB.from8BitChannel(ARGB.blue(color));
    }

    private static float from8BitChannel(int value) {
        return (float)value / 255.0f;
    }

    public static int toABGR(int color) {
        return color & 0xFF00FF00 | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;
    }

    public static int fromABGR(int color) {
        return ARGB.toABGR(color);
    }

    public static int srgbLerp(float alpha, int p0, int p1) {
        int a = Mth.lerpInt(alpha, ARGB.alpha(p0), ARGB.alpha(p1));
        int red = Mth.lerpInt(alpha, ARGB.red(p0), ARGB.red(p1));
        int green = Mth.lerpInt(alpha, ARGB.green(p0), ARGB.green(p1));
        int blue = Mth.lerpInt(alpha, ARGB.blue(p0), ARGB.blue(p1));
        return ARGB.color(red, green, blue, a);
    }

}
