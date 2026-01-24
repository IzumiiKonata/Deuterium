package tritium.management;

import lombok.Getter;
import lombok.SneakyThrows;
import tritium.bridge.rendering.font.FontWrapper;
import tritium.interfaces.IFontRenderer;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.font.FontKerning;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * @since 4/8/2023 11:09 AM
 */
public class FontManager extends AbstractManager {

    public FontManager() {
        super("FontManager");
    }

    public static CFontRenderer pf12bold, pf14bold, pf16bold, pf18bold, pf20bold, pf25bold, pf28bold, pf34bold, pf40bold, pf65bold, pf50bold, pf100bold;
    public static CFontRenderer pf12, pf14, pf16, pf18, pf20, pf25, pf28, pf32, pf40;
    public static CFontRenderer icon18, icon25, icon30, icon40, tritium24, tritium42;
    public static CFontRenderer music16, music18, music30, music36, music40, music42;
    public static CFontRenderer arial14, arial18bold, arial40bold, arial60bold;

    public static CFontRenderer googleSans16, googleSans18, googleSans16Bold, googleSans18Bold, product18, tahoma18;
    public static FontWrapper googleSans16W, googleSans18W, googleSans16BoldW, googleSans18BoldW, product18W, tahoma18W;

    // 香草字体的 wrapper
    public static IFontRenderer vanilla;
    public static today.opai.api.interfaces.render.Font vanillaWrapper;

    @Getter
    private static final List<CFontRenderer> extensionCreatedFontRenderers = new CopyOnWriteArrayList<>();

    public static List<CFontRenderer> getAllFontRenderers() {

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

        getExtensionCreatedFontRenderers().forEach(c -> {
            if (c != null) {
                c.close();
            }
        });
    }

    public static void loadFonts() {
        String normalName = "pf_normal";
        String boldName = "pf_middleblack";

        tritium42 = create(42, "tritium");
        arial60bold = create(60, "arialBold");
        pf18 = create(18, normalName);

        pf12bold = create(12, boldName);
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

        googleSans16 = create(16, "googlesans");
        googleSans18 = create(18, "googlesans");
        googleSans16Bold = create(16, "googlesansbold");
        googleSans18Bold = create(18, "googlesansbold");
        product18 = create(18, "product");
        tahoma18 = create(18, "tahoma");

        googleSans16W = new FontWrapper(googleSans16);
        googleSans18W = new FontWrapper(googleSans18);
        googleSans16BoldW = new FontWrapper(googleSans16Bold);
        googleSans18BoldW = new FontWrapper(googleSans18Bold);
        product18W = new FontWrapper(product18);
        tahoma18W = new FontWrapper(tahoma18);
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
        waitUntilAllLoaded();
    }
    
    private static final HashMap<String, Font> fonts = new HashMap<>();
    private static final HashMap<String, FontKerning> fontKernings = new HashMap<>();
    
    private static Font readFont(String path) {
        return fonts.computeIfAbsent(path, p -> {
            try {
                InputStream resourceAsStream = FontManager.class.getResourceAsStream(p);
                Font font = Font.createFont(Font.TRUETYPE_FONT, resourceAsStream);
                resourceAsStream.close();
                return font;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
    
    private static FontKerning readFontKerning(String path) {

        if (true)
            return null;

        return fontKernings.computeIfAbsent(path, p -> {
            try {
                // 获取字体文件的绝对路径
                String fontPath = FontManager.class.getResource(p).getPath();
                // 如果路径包含空格或其他特殊字符，需要处理
                if (fontPath.startsWith("/")) {
                    fontPath = fontPath.substring(1);
                }
                fontPath = fontPath.replace("/", "\\");
                File fontFile = new File(fontPath);
                return new FontKerning(fontFile);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @SneakyThrows
    public static CFontRenderer create(float size, InputStream fontStream) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);

        Font fallback = readFont("/assets/minecraft/tritium/fonts/pf_normal.ttf");
        FontKerning fallbackKerning = readFontKerning("/assets/minecraft/tritium/fonts/pf_normal.ttf");
        return new CFontRenderer(font, size * 0.5f, fallbackKerning, fallback);
    }

    @SneakyThrows
    public static CFontRenderer create(float size, InputStream fontStream, InputStream fallBackStream) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        Font fallBack = Font.createFont(Font.TRUETYPE_FONT, fallBackStream);

        FontKerning fontKerning = readFontKerning("/assets/minecraft/tritium/fonts/pf_normal.ttf");
        return new CFontRenderer(font, size * 0.5f, fontKerning, fallBack);
    }

    @SneakyThrows
    public static CFontRenderer create(float size, String name) {

        Font font = readFont("/assets/minecraft/tritium/fonts/" + name + ".ttf");
        FontKerning kerning = readFontKerning("/assets/minecraft/tritium/fonts/" + name + ".ttf");

        // 中文字体默认使用 SF Pro 作为主字体
        // 因为它们的英文字母太他妈难看了
        // 丑陋不堪，，
        return switch (name) {
            case "googlesans", "product", "tahoma" -> {
                Font fallback = readFont("/assets/minecraft/tritium/fonts/pf_normal.ttf");
                FontKerning fallbackKerning = readFontKerning("/assets/minecraft/tritium/fonts/pf_normal.ttf");
                yield new CFontRenderer(font, size * 0.5f, kerning, fallback);
            }
            case "googlesansbold" -> {
                Font fallback = readFont("/assets/minecraft/tritium/fonts/pf_middleblack.ttf");
                FontKerning fallbackKerning = readFontKerning("/assets/minecraft/tritium/fonts/pf_middleblack.ttf");
                yield new CFontRenderer(font, size * 0.5f, kerning, fallback);
            }
            case "pf_normal" -> {
                Font main = readFont("/assets/minecraft/tritium/fonts/sfregular.otf");
                FontKerning mainKerning = readFontKerning("/assets/minecraft/tritium/fonts/sfregular.otf");
                yield new CFontRenderer(main, size * 0.5f, mainKerning, font);
            }
            case "pf_middleblack" -> {
                Font main = readFont("/assets/minecraft/tritium/fonts/sfbold.otf");
                FontKerning mainKerning = readFontKerning("/assets/minecraft/tritium/fonts/sfbold.otf");
                yield new CFontRenderer(main, size * 0.5f, mainKerning, font);
            }
            default -> new CFontRenderer(font, size * 0.5f, kerning, font);
        };

    }

    @Override
    public void stop() {

    }
}
