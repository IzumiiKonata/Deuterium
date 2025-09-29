package tech.konata.phosphate.management;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render2DEvent;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.rendering.AccentColor;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.materialcolors.dynamiccolor.DynamicScheme;
import tech.konata.phosphate.rendering.materialcolors.dynamiccolor.MaterialDynamicColors;
import tech.konata.phosphate.rendering.materialcolors.hct.Hct;
import tech.konata.phosphate.rendering.materialcolors.scheme.SchemeFidelity;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.GlobalSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static tech.konata.phosphate.rendering.rendersystem.RenderSystem.hexColor;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
public class ThemeManager extends AbstractManager {

    @Getter
    private static ArrayList<AccentColor> colors = new ArrayList<AccentColor>();

    @Getter
    @Setter
    private static AccentColor accentColor;

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

        colors.add(new AccentColor("Amethyst", new Color(144, 99, 205), new Color(99, 67, 140)));
        colors.add(new AccentColor("Sulphur", new Color(202, 197, 49), new Color(243, 249, 167)));
        colors.add(new AccentColor("Speamint", new Color(96, 194, 165), new Color(66, 129, 108)));
        colors.add(new AccentColor("Magenta", new Color(216, 63, 123), new Color(191, 77, 151)));
        colors.add(new AccentColor("Rainbow Blue", new Color(0, 242, 96), new Color(5, 117, 230)));
        colors.add(new AccentColor("Metapolis", new Color(101, 153, 153), new Color(244, 121, 31)));
        colors.add(new AccentColor("MintYellow", new Color(100, 234, 190), new Color(254, 250, 163)));
        colors.add(new AccentColor("Lemon", new Color(252, 248, 184), new Color(255, 243, 109)));
        colors.add(new AccentColor("Chambray Blue", new Color(34, 45, 174), new Color(58, 79, 137)));
        colors.add(new AccentColor("Kye Meh", new Color(131, 96, 195), new Color(46, 191, 145)));
        colors.add(new AccentColor("Candy", new Color(211, 149, 155), new Color(191, 230, 186)));
        colors.add(new AccentColor("Neon Red", new Color(210, 39, 48), new Color(184, 27, 45)));
        colors.add(new AccentColor("Blaze Orange", new Color(254, 169, 76), new Color(253, 130, 0)));
        colors.add(new AccentColor("Melon", new Color(173, 247, 115), new Color(128, 243, 147)));
        colors.add(new AccentColor("Deep Ocean", new Color(61, 79, 143), new Color(1, 19, 63)));
        colors.add(new AccentColor("Cinnamint", new Color(74, 194, 154), new Color(189, 255, 243)));
        colors.add(new AccentColor("Purple Love", new Color(204, 43, 94), new Color(117, 58, 136)));
        colors.add(new AccentColor("Mint Blue", new Color(63, 149, 150), new Color(38, 90, 88)));
        colors.add(new AccentColor("Fruit", new Color(241, 178, 246), new Color(254, 240, 45)));
        colors.add(new AccentColor("Sunset Pink", new Color(253, 145, 21), new Color(245, 106, 230)));
        colors.add(new AccentColor("March 7th", new Color(237, 133, 211), new Color(28, 166, 222)));
        colors.add(new AccentColor("Tropical Ice", new Color(90, 227, 186), new Color(6, 133, 227)));
        colors.add(new AccentColor("Gray", new Color(189, 195, 199), new Color(44, 62, 80)));
        colors.add(new AccentColor("Purple&Blue", new Color(33, 212, 253), new Color(183, 33, 255)));
        colors.add(new AccentColor("Relaxing red", new Color(255, 251, 213), new Color(178, 10, 44)));
        colors.add(new AccentColor("Rosy Pink", new Color(255, 102, 202), new Color(191, 78, 152)));
        colors.add(new AccentColor("Amin", new Color(142, 45, 226), new Color(74, 0, 224)));
        colors.add(new AccentColor("Orange", new Color(251, 109, 32), new Color(190, 53, 38)));
        colors.add(new AccentColor("LightOrange", new Color(250, 217, 97), new Color(247, 107, 28)));
        colors.add(new AccentColor("Magic", new Color(89, 193, 115), new Color(93, 38, 193)));
        colors.add(new AccentColor("Green Spiirit", new Color(5, 135, 65), new Color(158, 227, 191)));
        colors.add(new AccentColor("Sublime", new Color(252, 70, 107), new Color(63, 94, 251)));
        colors.add(new AccentColor("ClearMint", new Color(116, 235, 213), new Color(159, 172, 230)));
        colors.add(new AccentColor("Pink Blood", new Color(226, 0, 70), new Color(255, 166, 200)));
        colors.add(new AccentColor("Pacific Blue", new Color(7, 154, 186), new Color(0, 106, 122)));
        colors.add(new AccentColor("Pink", new Color(234, 107, 149), new Color(238, 164, 123)));
        colors.add(new AccentColor("Blue", new Color(85, 184, 221), new Color(2, 94, 186)));

        accentColor = getAccentColorByName("Blue");
    }

    public static AccentColor getAccentColorByName(String name) {
        return colors.stream().filter(color -> color.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public ThemeManager() {
        super("ThemeManager");
    }

    public static int getHexAccentColor() {
        return accentColor.getColor1().getRGB();
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

        if (GlobalSettings.MUSIC_THEME.getValue() && from != ThemeColor.Text && CloudMusic.currentlyPlaying != null) {
            from.a = Interpolations.interpBezier(from.a, 80, speed);
        } else {
            from.a = Interpolations.interpBezier(from.a, 255, speed);
        }

    }

    private int mix(int a, int b, double aPerc) {

        int ra = (a >> 16 & 255);
        int ga = (a >> 8 & 255);
        int ba = (a & 255);

        int rb = (b >> 16 & 255);
        int gb = (b >> 8 & 255);
        int bb = (b & 255);

        int mixR = (int) (ra - (ra - rb) * (1 - aPerc));
        int mixG = (int) (ga - (ga - gb) * (1 - aPerc));
        int mixB = (int) (ba - (ba - bb) * (1 - aPerc));

        return RenderSystem.hexColor(mixR, mixG, mixB);

    }

    public DynamicScheme dynamicScheme = null;
    public MaterialDynamicColors mdc = new MaterialDynamicColors();


    @Handler
    public void onRender2D(Render2DEvent e) {

        if (GlobalSettings.MUSIC_THEME.getValue() && CloudMusic.currentlyPlaying != null) {

            CloudMusic.ColorPlatte cp = CloudMusic.avgColor.get(CloudMusic.currentlyPlaying.getId());

            if (cp != null) {

                int c = cp.get(0);

                if (cp.colors.length > 1) {
                    c = cp.get();

                    if (dynamicScheme == null || dynamicScheme.sourceColorArgb != c) {
                        dynamicScheme = new SchemeFidelity(Hct.fromInt(c), true, 0);
                    }

                    double v = 1 - GlobalSettings.MIX_FACTOR.getValue();

                    float speed = 0.04f;

                    interpCat(ThemeColor.OnSurface, mix(getActual(ThemeColor.OnSurface), mdc.onSurfaceVariant().getArgb(dynamicScheme), v), speed);
                    interpCat(ThemeColor.Text, mix(getActual(ThemeColor.Text), mdc.secondary().getArgb(dynamicScheme), v * 1.375), speed);
                    interpCat(ThemeColor.Surface, mix(getActual(ThemeColor.Surface), mdc.primaryFixedDim().getArgb(dynamicScheme), v), speed);

                } else {
                    if (dynamicScheme == null || dynamicScheme.sourceColorArgb != c) {
                        dynamicScheme = new SchemeFidelity(Hct.fromInt(c), true, 0);
                    }


                    double v = 1 - GlobalSettings.MIX_FACTOR.getValue();

                    interpCat(ThemeColor.OnSurface, mix(getActual(ThemeColor.OnSurface), mdc.primaryFixed().getArgb(dynamicScheme), v), 0.2f);
                    interpCat(ThemeColor.Text, mix(getActual(ThemeColor.Text), mdc.secondary().getArgb(dynamicScheme), v * 1.375), 0.2f);
                    interpCat(ThemeColor.Surface, mix(getActual(ThemeColor.Surface), mdc.primaryFixedDim().getArgb(dynamicScheme), v), 0.2f);

                }

            }

        } else {

            interpCat(ThemeColor.OnSurface, getActual(ThemeColor.OnSurface), 0.2f);
            interpCat(ThemeColor.Text, getActual(ThemeColor.Text), 0.2f);
            interpCat(ThemeColor.Surface, getActual(ThemeColor.Surface), 0.2f);

        }
    }

    public static int get(ThemeColor c) {
        return RenderSystem.hexColor((int) c.r, (int) c.g, (int) c.b, (int) c.a);
    }

    public static int get(ThemeColor c, int alpha) {
        return RenderSystem.hexColor((int) c.r, (int) c.g, (int) c.b, alpha);
    }

    public static Color getAsColor(ThemeColor c) {
        return new Color(RenderSystem.hexColor((int) c.r, (int) c.g, (int) c.b, (int) c.a), true);
    }

    public static Color getAsColor(ThemeColor c, int alpha) {
        return new Color(RenderSystem.hexColor((int) c.r, (int) c.g, (int) c.b, alpha), true);
    }

    public static Color mixAccentColor(AccentColor color, int index, int alpha) {
        return interpolateColorsBackAndForth(15, index, new Color(color.getColor1().getRed(), color.getColor1().getGreen(), color.getColor1().getBlue(), alpha), new Color(color.getColor2().getRed(), color.getColor2().getGreen(), color.getColor2().getBlue(), alpha));
    }

    public static Color mixAccentColor(int index, int alpha) {
        return interpolateColorsBackAndForth(15, index, new Color(accentColor.getColor1().getRed(), accentColor.getColor1().getGreen(), accentColor.getColor1().getBlue(), alpha), new Color(accentColor.getColor2().getRed(), accentColor.getColor2().getGreen(), accentColor.getColor2().getBlue(), alpha));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return getInterpolateColor(start, end, angle / 360f);
    }

    private static Color getInterpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount), interpolateInt(color1.getGreen(), color2.getGreen(), amount), interpolateInt(color1.getBlue(), color2.getBlue(), amount), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue){
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static List<String> getAllAccentColorNames() {

        List<String> result = new ArrayList<>();

        for (AccentColor color : colors) {
            result.add(color.getName());
        }

        return result;
    }

    public static int getActual(ThemeColor c) {
        switch (c) {
            case Surface:
                return theme.surface;
            case OnSurface:
                return theme.onSurface;
            case Text:
                return theme.text;
            default:
                throw new IllegalArgumentException("NOT FOUND!");
        }
    }

    @RequiredArgsConstructor
    public enum Theme {
        Light(
                RenderSystem.hexColor(238, 238, 238),
                RenderSystem.hexColor(254, 254, 254),
                RenderSystem.hexColor(54, 54, 54)
        ),
        Dark(
                RenderSystem.hexColor(34, 35, 39),
                RenderSystem.hexColor(19, 19, 20),
                RenderSystem.hexColor(254, 254, 254)
        ),
        LIGHT_BLUE(
                hexColor(190, 216, 238),
                hexColor(203, 224, 255),
                hexColor(255, 255, 255)
        ),
        DARK_BLUE(
                hexColor(27, 36, 52),
                hexColor(22, 28, 41),
                hexColor(121, 136, 166)
        );

        public final int surface, onSurface, text;
        public float outlineAlpha = 0;
    }

    public enum ThemeColor {
        Surface,
        OnSurface,
        Text;

        public double r = 0, g = 0, b = 0, a = 255;
    }
}
