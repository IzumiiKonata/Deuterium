package tritium.management;

import lombok.SneakyThrows;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.font.GlyphCache;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author IzumiiKonata
 * @since 4/8/2023 11:09 AM
 */
public class FontManager extends AbstractManager {

    public FontManager() {
        super("FontManager");
    }

    public static CFontRenderer pf14bold, pf16bold, pf18bold, pf20bold, pf25bold, pf28bold, pf40bold, pf50bold, pf100bold;
    public static CFontRenderer pf12, pf14, pf16, pf18, pf20, pf25, pf28, pf32, pf40;
    public static CFontRenderer icon18, icon25, icon30, icon40, tritium24, tritium42;
    public static CFontRenderer arial18bold, arial60bold;

    public static CFontRenderer getBySize(int size) {
        return getBySize(size, false);
    }

    public static CFontRenderer getBySize(int size, boolean bold) {

        List<CFontRenderer> list = bold ? Arrays.asList(
                pf14bold, pf16bold, pf18bold, pf20bold, pf25bold, pf28bold, pf40bold, pf50bold, pf100bold
        ) : Arrays.asList(
                pf14, pf16, pf18, pf20, pf25, pf28, pf32
        );

        CFontRenderer result = list.get(0);

        for (int i = list.size() - 1; i >= 0; i--) {
            CFontRenderer cFontRenderer = list.get(i);

            int sz = (int) (cFontRenderer.sizePx * 2);

            if (sz > size && i > 0) {
                result = list.get(i - 1);
            }

        }

        return result;
    }

    public static void deleteLoadedTextures() {
        List<CFontRenderer> list = Arrays.asList(
                pf14bold, pf16bold, pf18bold, pf20bold, pf25bold, pf28bold, pf40bold, pf50bold, pf100bold,
                pf14, pf16, pf18, pf20, pf25, pf28, pf32,
                icon25, icon30, icon40
        );

        list.forEach(c -> {
            if (c != null) {
                c.close();
            }
        });

        GlyphCache.clear();
    }

    public static void loadFonts() {
        deleteLoadedTextures();

        String boldName = "pf_middlebold";

        pf14bold = create(14, boldName);
        pf16bold = create(16, boldName);
        pf18bold = create(18, boldName);
        pf20bold = create(20, boldName);
        pf25bold = create(25, boldName);
        pf28bold = create(28, boldName);
        pf40bold = create(40, boldName);
        pf50bold = create(50, boldName);
        pf100bold = create(100, boldName);

        String normalName = "pf_normal";

        pf12 = create(12, normalName);
        pf14 = create(14, normalName);
        pf16 = create(16, normalName);
        pf18 = create(18, normalName);
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
        tritium42 = create(42, "tritium");

        arial18bold = create(18, "arialBold");
        arial60bold = create(60, "arialBold");
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
