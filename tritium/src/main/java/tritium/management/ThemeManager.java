package tritium.management;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.Render2DEvent;
import tritium.rendering.RGBA;
import tritium.rendering.animation.Interpolations;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
public class ThemeManager extends AbstractManager {

    @Getter
    @Setter
    private static Theme theme = Theme.Light;
    static {
        for (ThemeManager.ThemeColor t : ThemeManager.ThemeColor.values()) {
            Color actual = new Color(ThemeManager.getActual(t));

            t.r = actual.getRed();
            t.g = actual.getGreen();
            t.b = actual.getBlue();
        }
    }

    public ThemeManager() {
        super("ThemeManager");
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }

    private void interpCat(ThemeManager.ThemeColor from, int to, float speed) {

        int r = (to >> 16 & 255);
        int g = (to >> 8 & 255);
        int b = (to & 255);

        from.r = Interpolations.interpBezier(from.r, r, speed);
        from.g = Interpolations.interpBezier(from.g, g, speed);
        from.b = Interpolations.interpBezier(from.b, b, speed);
        from.a = Interpolations.interpBezier(from.a, 255, speed);
    }

    @Handler
    public void onRender2D(Render2DEvent e) {
        this.interp();
    }

    public void interp() {
        interpCat(ThemeColor.OnSurface, getActual(ThemeColor.OnSurface), 0.2f);
        interpCat(ThemeColor.Text, getActual(ThemeColor.Text), 0.2f);
        interpCat(ThemeColor.Surface, getActual(ThemeColor.Surface), 0.2f);
    }

    public static int get(ThemeColor c) {
        return RGBA.color((int) c.r, (int) c.g, (int) c.b, (int) c.a);
    }

    public static int get(ThemeColor c, int alpha) {
        return RGBA.color((int) c.r, (int) c.g, (int) c.b, alpha);
    }

    public static Color getAsColor(ThemeColor c) {
        return new Color(RGBA.color((int) c.r, (int) c.g, (int) c.b, (int) c.a), true);
    }

    public static Color getAsColor(ThemeColor c, int alpha) {
        return new Color(RGBA.color((int) c.r, (int) c.g, (int) c.b, alpha), true);
    }


    public static int getActual(ThemeColor c) {
        return switch (c) {
            case Surface -> theme.surface;
            case OnSurface -> theme.onSurface;
            case Text -> theme.text;
        };
    }

    @RequiredArgsConstructor
    public enum Theme {
        Light(
                RGBA.color(238, 238, 238),
                RGBA.color(254, 254, 254),
                RGBA.color(54, 54, 54)
        ),
        Dark(
                RGBA.color(34, 35, 39),
                RGBA.color(19, 19, 20),
                RGBA.color(254, 254, 254)
        );

        public final int surface, onSurface, text;
    }

    public enum ThemeColor {
        Surface,
        OnSurface,
        Text;

        public double r = 0, g = 0, b = 0, a = 255;
    }
}
