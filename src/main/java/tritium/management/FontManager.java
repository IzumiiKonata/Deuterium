package tritium.management;

import lombok.SneakyThrows;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.font.GlyphCache;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * @since 4/8/2023 11:09 AM
 */
public class FontManager extends AbstractManager {

    public FontManager() {
        super("FontManager");
    }

    public static CFontRenderer pf14bold, pf16bold, pf18bold, pf20bold, pf25bold, pf28bold, pf34bold, pf40bold, pf65bold, pf50bold, pf100bold;
    public static CFontRenderer pf12, pf14, pf16, pf18, pf20, pf25, pf28, pf32, pf40;
    public static CFontRenderer icon18, icon25, icon30, icon40, tritium24, tritium42;
    public static CFontRenderer music16, music18, music30, music36, music40, music42;
    public static CFontRenderer arial14, arial18bold, arial40bold, arial60bold;

    private static List<CFontRenderer> getAllFontRenderers() {

        return Arrays.stream(FontManager.class.getDeclaredFields())
                .filter(field -> field.getType() == CFontRenderer.class)
                .map(method -> {
            try {
                return (CFontRenderer) method.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

    }

    public static void deleteLoadedTextures() {
        getAllFontRenderers().forEach(c -> {
            if (c != null) {
                c.close();
            }
        });

        GlyphCache.clear();
    }

    public static void loadFonts() {
        deleteLoadedTextures();

        String normalName = "pf_normal";
        String boldName = "pf_middlebold";

        tritium42 = create(42, "tritium");
        arial60bold = create(60, "arialBold");
        pf18 = create(18, normalName);

        pf14bold = create(14, boldName);
        pf16bold = create(16, boldName);
        pf18bold = create(18, boldName);
        pf20bold = create(20, boldName);
        pf25bold = create(25, boldName);
        pf28bold = create(28, boldName);
        pf34bold = create(34, boldName);
        pf40bold = create(40, boldName);
        pf50bold = create(50, boldName);
        pf65bold = create(65, boldName);
        pf100bold = create(100, boldName);


        pf12 = create(12, normalName);
        pf14 = create(14, normalName);
        pf16 = create(16, normalName);
        pf20 = create(20, normalName);
        pf25 = create(25, normalName);
        pf28 = create(28, normalName);
        pf32 = create(32, normalName);
        pf40 = create(40, normalName);

        icon18 = create(18, "icomoon");
        icon25 = create(25, "icomoon");
        icon30 = create(30, "icomoon");
        icon40 = create(40, "icomoon");

        tritium24 = create(24, "tritium");

        arial14 = create(14, "arial");
        arial18bold = create(18, "arialBold");
        arial40bold = create(40, "arialBold");

        music16 = create(16, "music");
        music18 = create(18, "music");
        music30 = create(30, "music");
        music36 = create(36, "music");
        music40 = create(40, "music");
        music42 = create(42, "music");
    }

    @SneakyThrows
    public static void waitUntilAllLoaded() {

        while (true) {
            List<CFontRenderer> list = getAllFontRenderers();

            Thread.sleep(100);

            long count = list.stream().filter(Objects::isNull).count();

            if (count == 0)
                break;

            System.out.println("Waiting for " + count + " font renderers to be initialized.");
        }

    }

    @Override
    public void init() {

    }

    @SneakyThrows
    public static CFontRenderer create(float size, InputStream fontStream) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);

        return new CFontRenderer(font, size * 0.5f, font);
    }

    @SneakyThrows
    public static CFontRenderer create(float size, InputStream fontStream, InputStream fallBackStream) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        Font fallBack = Font.createFont(Font.TRUETYPE_FONT, fallBackStream);

        return new CFontRenderer(font, size * 0.5f, fallBack);
    }

    @SneakyThrows
    public static CFontRenderer create(float size, String name) {

        Font font = Font.createFont(Font.TRUETYPE_FONT, FontManager.class.getResourceAsStream("/assets/minecraft/tritium/fonts/" + name + ".ttf"));

        return new CFontRenderer(font, size * 0.5f, font);
    }

    @SneakyThrows
    public static CFontRenderer createFromExternalFile(float size, File path, String fallBackName) {

        Font fallBack = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontManager.class.getResourceAsStream("/assets/minecraft/tritium/fonts/" + fallBackName + ".ttf")));

        return new CFontRenderer(Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(path)), size * 0.5f, fallBack);
    }

    @Override
    public void stop() {

    }
}
