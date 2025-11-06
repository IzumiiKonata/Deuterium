package tritium.rendering.font;

import lombok.SneakyThrows;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import tritium.Tritium;
import tritium.interfaces.IFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.other.StringUtils;

import java.awt.*;
import java.io.Closeable;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CFontRenderer implements Closeable, IFontRenderer {

    public Glyph[] allGlyphs = new Glyph['\uFFFF' + 1];

    public Font font;
    public Font[] fallBackFonts;
    private final Map<String, Integer> stringWidthMap = new HashMap<>();
    public float sizePx;

    public CFontRenderer(Font font, float sizePx) {
        this.sizePx = sizePx;

        init(font, sizePx);
    }

    @SneakyThrows
    public CFontRenderer(Font font, float sizePx, Font... fallBackFonts) {
        this(font, sizePx);

        this.fallBackFonts = new Font[fallBackFonts.length];

        for (int i = 0; i < fallBackFonts.length; i++) {
            this.fallBackFonts[i] = fallBackFonts[i].deriveFont(sizePx * 2);
        }

//        this.fallBackFonts[fallBackFonts.length] = Font.createFont(Font.TRUETYPE_FONT, FontManager.class.getResourceAsStream("/assets/minecraft/tritium/fonts/NotoColorEmoji.ttf")).deriveFont(sizePx * 2);
//        this.fallBackFonts[fallBackFonts.length + 1] = Font.createFont(Font.TRUETYPE_FONT, FontManager.class.getResourceAsStream("/assets/minecraft/tritium/fonts/Symbola.ttf")).deriveFont(sizePx * 2);
    }

    public static String stripControlCodes(String text) {
        char[] chars = text.toCharArray();
        StringBuilder f = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '§') {
                i++;
                continue;
            }
            f.append(c);
        }
        return f.toString();
    }

    private void init(Font font, float sizePx) {
        this.font = font.deriveFont(sizePx * 2);

//        if (this.sizePx == 9.0 && this.font.getFontName().equals(".萍方-简 正规体")) {
//            for (char c : "单人游戏多设置账号管理".toCharArray()) {
//                locateGlyph(c);
//            }
//        }

        locateGlyph('A');
    }

    List<Integer> loaded = new ArrayList<>();

    public double fontHeight = -1;

    public interface GlyphLoaded {
        void loaded(Glyph glyph);
    }

    private Glyph locateGlyph(char ch) {

        Glyph gly = allGlyphs[ch];
        if (gly != null) return gly;

        GlyphGenerator.generate(this, ch, this.font, randomIdentifier(), fontHeight -> {
            this.fontHeight = Math.max(this.fontHeight, fontHeight);
        });

        return null;
    }

    @SneakyThrows
    private Glyph locateGlyphBlocking(char ch) {
        Glyph gly = allGlyphs[ch];
        if (gly != null) return gly;

        final Object lock = new Object();

        GlyphGenerator.generate(this, ch, this.font, randomIdentifier(), fontHeight -> {
            this.fontHeight = Math.max(this.fontHeight, fontHeight);

            synchronized (lock) {
                lock.notifyAll();
            }
        });

        synchronized (lock) {
            lock.wait();
        }

        return allGlyphs[ch];
    }

    public float drawString(String s, double x, double y, int color) {
        float r = ((color >> 16) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float g = ((color >> 8) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float b = ((color) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float a = ((color >> 24) & 0xff) * RenderSystem.DIVIDE_BY_255;
        drawString(s,  x,  y, r, g, b, a);
        return getStringWidth(s);
    }

    @Override
    public int drawStringWithShadow(String text, double x, double y, int color) {

        int a = (color >> 24) & 0xff;

        drawString(net.minecraft.util.StringUtils.stripControlCodes(text), x + 1, y + 1, RenderSystem.hexColor(0, 0, 0, a));
        drawString(text, x, y, color);

        return this.getStringWidth(text);
    }

    public void drawString(String s, double x, double y, Color color) {
        drawString(s,  x,  y, color.getRed() * RenderSystem.DIVIDE_BY_255, color.getGreen() * RenderSystem.DIVIDE_BY_255, color.getBlue() * RenderSystem.DIVIDE_BY_255, color.getAlpha());
    }

    private int getColorCode(char c) {
        switch (c) {
            case '0': return 0x000000;
            case '1': return 0x0000AA;
            case '2': return 0x00AA00;
            case '3': return 0x00AAAA;
            case '4': return 0xAA0000;
            case '5': return 0xAA00AA;
            case '6': return 0xFFAA00;
            case '7': return 0xAAAAAA;
            case '8': return 0x555555;
            case '9': return 0x5555FF;
            case 'A': return 0x55FF55;
            case 'B': return 0x55FFFF;
            case 'C': return 0xFF5555;
            case 'D': return 0xFF55FF;
            case 'E': return 0xFFFF55;
            case 'F': return 0xFFFFFF;

            default: return Integer.MIN_VALUE; // Default color or throw an exception
        }
    }

    public boolean drawString(String s, double x, double y, float r, float g, float b, float a) {

        float r2 = r, g2 = g, b2 = b;
        GlStateManager.pushMatrix();

        y -= 2.0f;

        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(0.5f, 0.5f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();

        boolean bl = true;

        double xOffset = 0;
        double yOffset = 0;
        boolean inSel = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inSel) {
                inSel = false;
                char c1 = Character.toUpperCase(c);
                if (c1 == 'R') {
                    r2 = r;
                    g2 = g;
                    b2 = b;
                } else {
                    int colorCode = this.getColorCode(c1);

                    if (colorCode != Integer.MIN_VALUE) {
                        int[] col = RGBIntToRGB(colorCode);

                        r2 = col[0] * RenderSystem.DIVIDE_BY_255;
                        g2 = col[1] * RenderSystem.DIVIDE_BY_255;
                        b2 = col[2] * RenderSystem.DIVIDE_BY_255;
                    }
                }
                continue;
            }

            if (c == '§') {
                inSel = true;
                continue;
            }
            if (c == '\n') {
                yOffset += this.getHeight() * 2 + 4;
                xOffset = 0;
                continue;
            }

            if (c == '（')
                c = '(';

            if (c == '）')
                c = ')';

            Glyph glyph = locateGlyph(c);
            if (glyph != null && glyph.callList != -1 && glyph.textureId != -1) {
                xOffset += glyph.render(xOffset, yOffset, r2, g2, b2, a);
            } else {
                bl = false;
            }
        }

        GlStateManager.popMatrix();
        return bl;
    }

    public void drawCenteredString(String s, double x, double y, int color) {
        _drawCenteredString(s, x, y, color);
    }

    /**
     * @return true if all the chars in the string are loaded
     */
    public boolean _drawCenteredString(String s, double x, double y, int color) {
        float r = ((color >> 16) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float g = ((color >> 8) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float b = ((color) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float a = ((color >> 24) & 0xff) * RenderSystem.DIVIDE_BY_255;

        return drawString(s,  (x - getStringWidth(s) * .5),  y, r, g, b, a);
    }

    public void drawCenteredStringWithShadow(String s, double x, double y, int color) {
        drawStringWithShadow(s,  (x - getStringWidth(s) * .5),  y, color);
    }

    public void drawCenteredStringMultiLine(String s, double x, double y, int color) {
        float r = ((color >> 16) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float g = ((color >> 8) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float b = ((color) & 0xff) * RenderSystem.DIVIDE_BY_255;
        float a = ((color >> 24) & 0xff) * RenderSystem.DIVIDE_BY_255;

        double offsetY = y;
        for (String string : s.split("\n")) {
            drawString(string,  (x - getStringWidth(string) / 2.0),  offsetY, r, g, b, a);
            offsetY += this.getStringHeight(string);
        }

    }

    public String trim(String text, double width) {
        String name = text;

        if (this.getStringWidth(name) > width) {
            int idx = name.length() - 1;
            while (true) {
                String substring = name.substring(0, idx);

                if (this.getStringWidth(substring + "...") <= width) {
                    name = substring + "...";
                    break;
                }

                idx--;
            }
        }

        return name;
    }

    public int getStringWidth(String text) {

//        RenderTextEvent call = EventManager.call(new RenderTextEvent(text));

//        text = call.getText();

        Integer i = this.stringWidthMap.get(text);
        if (i != null)
            return i;

        boolean shouldntAdd = false;

        char[] c = stripControlCodes(text).toCharArray();
        float currentLine = 0;
        float maxPreviousLines = 0;
        for (char c1 : c) {
            if (c1 == '\n') {
                maxPreviousLines = Math.max(currentLine, maxPreviousLines);
                currentLine = 0;
                continue;
            }

            if (c1 == '（')
                c1 = '(';

            if (c1 == '）')
                c1 = ')';

            Glyph glyph = locateGlyph(c1);

            if (!shouldntAdd) {
                shouldntAdd = glyph == null || glyph.width == 0;
            }

            currentLine += glyph == null ? 0 : (glyph.width * 0.5f);
        }

        if (!shouldntAdd) {
            this.stringWidthMap.put(text, (int) Math.max(currentLine, maxPreviousLines));
        }

        return (int) Math.max(currentLine, maxPreviousLines);
    }

    private final Map<String, Double> stringWidthMapD = new HashMap<>();

    public boolean areGlyphsLoaded(String text) {

        for (char c : text.toCharArray()) {

            if (c == '（')
                c = '(';

            if (c == '）')
                c = ')';

            if (c == '\n')
                continue;

            if (c == ' ')
                continue;

            if (c == '\r')
                continue;

            if (c == '\t')
                continue;

            if (c == '\247')
                continue;

            Glyph gly = allGlyphs[c];
            if (gly == null || gly.textureId == -1 || gly.callList == -1)
                return false;
        }

        return true;
    }

    public double getStringWidthD(String text) {
        Double f = this.stringWidthMapD.get(text);
        if (f != null)
            return f;

        boolean shouldntAdd = false;

        char[] c = stripControlCodes(text).toCharArray();
        double currentLine = 0;
        double maxPreviousLines = 0;
        for (char c1 : c) {
            if (c1 == '\n') {
                maxPreviousLines = Math.max(currentLine, maxPreviousLines);
                currentLine = 0;
                continue;
            }

            if (c1 == '（')
                c1 = '(';

            if (c1 == '）')
                c1 = ')';

            float charWidth = getCharWidth(c1);

            if (!shouldntAdd) {
                shouldntAdd = charWidth == 0;
            }

            currentLine += charWidth;
        }

        if (!shouldntAdd) {
            this.stringWidthMapD.put(text, Math.max(currentLine, maxPreviousLines));
        }

        return Math.max(currentLine, maxPreviousLines);
    }

    private final Map<String, Float> stringHeightMap = new HashMap<>();

    public float getStringHeight(String text) {

        Float f = this.stringHeightMap.get(text);
        if (f != null)
            return f;

        char[] c = stripControlCodes(text).toCharArray();
        if (c.length == 0) {
            c = new char[]{' '};
        }
        float currentLine = 0;
        float previous = 0;
        for (char c1 : c) {

            if (c1 == '\n') {
                if (currentLine == 0) {
                    currentLine = this.getHeight();
                }
                previous += currentLine + 4;
                currentLine = 0;
                continue;
            }

            Glyph glyph = locateGlyph(c1);

            if (glyph == null)
                return -1;

            currentLine = Math.max(this.getHeight(), currentLine);
        }

        this.stringHeightMap.put(text, currentLine + previous);

        return currentLine + previous;
    }

    public int getHeight() {

//        if (fontHeightObj == null) {
//
//            int i = (int) ((this.getStringHeight(" ") - 4) / 2);
//
//            if (i > 10) {
//                fontHeightObj = i;
//            } else {
//                return i;
//            }
//
//        }

        return (int) ((this.fontHeight - 8) * .5);
    }

    public double getFontHeight() {
        return (this.fontHeight - 8) * .5;
    }

    @Override
    public void close() {
        for (Glyph gly : allGlyphs) {
            if (gly != null) {
                if (gly.textureId != -1) {
                    GlStateManager.deleteTexture(gly.textureId);
                }

                if (gly.callList != -1 && !gly.cached) {
                    GLAllocation.deleteDisplayLists(gly.callList);
                    GlyphCache.CALL_LIST_COUNTER.set(GlyphCache.CALL_LIST_COUNTER.get() - 1);
                }
            }
        }

        allGlyphs = new Glyph['\uFFFF' + 1];
        stringHeightMap.clear();
        stringWidthMap.clear();
        stringWidthMapD.clear();
    }

    public static Location randomIdentifier() {
        return Location.of(Tritium.NAME, "temp/" + randomString());
    }

    private static String randomString() {
        return IntStream.range(0, 32)
                .mapToObj(operand -> String.valueOf((char) ('a' + new Random().nextInt('z' + 1 - 'a'))))
                .collect(Collectors.joining());
    }

    public static int[] RGBIntToRGB(int in) {
        int red = in >> 8 * 2 & 0xFF;
        int green = in >> 8 & 0xFF;
        int blue = in & 0xFF;
        return new int[]{red, green, blue};
    }

    float getCharWidth(char ch) {
        Glyph glyph = allGlyphs[ch];

        if (glyph == null)
            return .0f;

        return glyph.width * .5f;
    }

    public String[] fitWidth(String text, double width) {

        List<String> split = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        double w = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\247') {
                i += 1;
                sb.append(c);

                if (i < text.length()) {
                    sb.append(text.charAt(i));
                }

                continue;
            }

            if (c == '\n') {
                split.add(sb.toString());
                sb = new StringBuilder();
                w = 0;
                continue;
            }

            double tWidth = this.getCharWidth(c);

            if (w + tWidth < width) {
                sb.append(c);
                w += tWidth;
            } else {
                if (c == ' ') {
                    split.add(sb.toString());
                    sb = new StringBuilder(String.valueOf(c));
                    w = this.getCharWidth(c);
                } else {
                    int lastSpace = sb.toString().lastIndexOf(" ");
                    if (lastSpace != -1) {
                        String res = sb.substring(0, lastSpace);
                        split.add(res);
                        i = text.substring(0 , Math.min(text.length(), i + res.length())).lastIndexOf(res) + res.length();
                        sb = new StringBuilder();
                    } else {
                        split.add(sb.toString());
                        sb = new StringBuilder(String.valueOf(c));
                    }
                    w = 0;
                }
            }
        }
        if (sb.length() != 0) {
            split.add(sb.toString());
        }

        return split.toArray(new String[0]);
    }

    public void drawStringWithBetterShadow(String text, double x, double y, int color) {
        drawString(text, x, y, color);
    }

    public void drawOutlineCenteredString(String text, double x, double y, int color, int outlineColor) {
        drawOutlineString(text, x - getStringWidth(text) / 2.0, y, color, outlineColor);
    }

    public void drawOutlineString(String text, double x, double y, int color, int outlineColor) {
        String outlinetext = StringUtils.removeFormattingCodes(text);
        drawString(outlinetext, x + 0.5, y, outlineColor);
        drawString(outlinetext, x - 0.5, y, outlineColor);
        drawString(outlinetext, x, y + 0.5, outlineColor);
        drawString(outlinetext, x, y - 0.5, outlineColor);
        drawString(text, x, y, color);
    }

    public int getWidth(String text) {
        return this.getStringWidth(text);
    }

}
