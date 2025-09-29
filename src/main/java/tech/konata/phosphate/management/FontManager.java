package tech.konata.phosphate.management;

import lombok.SneakyThrows;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.font.GlyphCache;
import tech.konata.phosphate.settings.GlobalSettings;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author IzumiiKonata
 * @since 4/8/2023 11:09 AM
 */
public class FontManager extends AbstractManager {

    public static CFontRenderer pf12, pf14, pf14bold, pf16, pf16bold, pf18, pf18bold, pf20, pf20bold, pf25, pf25bold, pf28, pf28bold, pf40, pf60, pf100, baloo18, icon30, icon18;

    public FontManager() {
        super("FontManager");
    }

    private static void deleteLoadedTextures() {
        List<CFontRenderer> list = Arrays.asList(pf12, pf14, pf14bold, pf16, pf16bold, pf18, pf18bold, pf20, pf20bold, pf25, pf25bold, pf28, pf28bold, pf40, pf60, pf100, baloo18, icon30, icon18);

        list.forEach(c -> {
            if (c != null) {
                c.close();
            }
        });

        GlyphCache.clear();
    }

    private static boolean allLoaded() {
        List<CFontRenderer> list = Arrays.asList(pf12, pf14, pf14bold, pf16, pf16bold, pf18, pf18bold, pf20, pf20bold, pf25, pf25bold, pf28, pf28bold, pf40, pf60, pf100, baloo18, icon30, icon18);

        for (CFontRenderer cFontRenderer : list) {
            if (cFontRenderer == null) {
                return false;
            }
        }

        return true;
    }

    @SneakyThrows
    public static void waitIfNotLoaded() {

        while (!allLoaded()) {
            Thread.sleep(100);
        }

    }

    public static volatile boolean loaded = false;

    public static void loadFonts() {

        loaded = true;

        deleteLoadedTextures();

        baloo18 = create(18, "Baloo", "Baloo");

        icon30 = create(40, "icomoon", "icomoon");
        icon18 = create(25, "icomoon", "icomoon");

        String externalBold = GlobalSettings.BOLD_FONT_RENDERER_PATH.getValue();

        String boldName = "pf_middlebold";

        if (!externalBold.isEmpty() && Files.exists(new File(externalBold).toPath())) {

            File bold = new File(externalBold);

            pf14bold = createFromExternalFile(14, bold, boldName);
            pf16bold = createFromExternalFile(16, bold, boldName);
            pf18bold = createFromExternalFile(18, bold, boldName);
            pf20bold = createFromExternalFile(20, bold, boldName);
            pf25bold = createFromExternalFile(25, bold, boldName);
            pf28bold = createFromExternalFile(28, bold, boldName);

        } else {
            pf14bold = create(14, boldName, boldName);
            pf16bold = create(16, boldName, boldName);
            pf18bold = create(18, boldName, boldName);
            pf20bold = create(20, boldName, boldName);
            pf25bold = create(25, boldName, boldName);
            pf28bold = create(28, boldName, boldName);
        }

        String externalRegular = GlobalSettings.REGULAR_FONT_RENDERER_PATH.getValue();

        String normal = "pf_normal";

        if (!externalRegular.isEmpty() && Files.exists(new File(externalRegular).toPath())) {

            File regular = new File(externalRegular);

            pf12 = createFromExternalFile(12, regular, normal);
            pf14 = createFromExternalFile(14, regular, normal);
            pf16 = createFromExternalFile(16, regular, normal);
            pf18 = createFromExternalFile(18, regular, normal);
            pf20 = createFromExternalFile(20, regular, normal);
            pf25 = createFromExternalFile(25, regular, normal);
            pf28 = createFromExternalFile(28, regular, normal);
            pf40 = createFromExternalFile(40, regular, normal);
            pf60 = createFromExternalFile(60, regular, normal);
            pf100 = createFromExternalFile(100, regular, normal);
        } else {

            pf12 = create(12, normal, normal);
            pf14 = create(14, normal, normal);
            pf16 = create(16, normal, normal);
            pf18 = create(18, normal, normal);
            pf20 = create(20, normal, normal);
            pf25 = create(25, normal, normal);
            pf28 = create(28, normal, normal);
            pf40 = create(40, normal, normal);
            pf60 = create(60, normal, normal);
            pf100 = create(100, normal, normal);
        }

    }

    @SneakyThrows
    public static Font fontFromTTF(String fontName, float fontSize, int fontType) {
        Font font = Font.createFont(Font.TRUETYPE_FONT,
                Objects.requireNonNull(FontManager.class.getResourceAsStream("/assets/minecraft/" + Phosphate.NAME + "/fonts/" + fontName)));
        font = font.deriveFont(fontType, fontSize);
        return font;
    }

    @Override
    public void init() {

//        loadFonts();

//        testRenderer = create(25, "pf_normal");

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
    public static CFontRenderer create(float size, String name, String fallBackName) {

        Font fallBack;

        Font font = Font.createFont(Font.TRUETYPE_FONT, FontManager.class.getResourceAsStream("/assets/minecraft/Phosphate/fonts/" + name + ".ttf"));

        if (fallBackName.equals(name))
            fallBack = font;
        else
            fallBack = Font.createFont(Font.TRUETYPE_FONT, FontManager.class.getResourceAsStream("/assets/minecraft/Phosphate/fonts/" + fallBackName + ".ttf"));

        return new CFontRenderer(font, size * 0.5f, fallBack);
    }

    @SneakyThrows
    public static CFontRenderer createFromExternalFile(float size, File path, String fallBackName) {

        Font fallBack = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontManager.class.getResourceAsStream("/assets/minecraft/Phosphate/fonts/" + fallBackName + ".ttf")));

        return new CFontRenderer(Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(path)), size * 0.5f, fallBack);
    }

    @Override
    public void stop() {

    }
}
