package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import net.optifine.CustomColors;
import net.optifine.render.GlBlendState;
import net.optifine.util.FontUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import tritium.command.CommandValues;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class FontRenderer implements IResourceManagerReloadListener {
    private static final Location[] unicodePageLocations = new Location[256];

    /**
     * Array of width of all the characters in default.png
     */
    private final int[] charWidth = new int[256];

    /**
     * the height in pixels of default text
     */
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();

    /**
     * Array of the start/end column (in upper/lower nibble) for every glyph in the /font directory.
     */
    private final byte[] glyphWidth = new byte[65536];

    /**
     * Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
     * drop shadows.
     */
    private final int[] colorCode = new int[32];
    private Location locationFontTexture;
    private boolean asciiTexturesLoaded = false;

    /**
     * The RenderEngine used to load and setup glyph textures.
     */
    private final TextureManager renderEngine;

    /**
     * Current X coordinate at which to draw the next character.
     */
    private float posX;

    /**
     * Current Y coordinate at which to draw the next character.
     */
    private float posY;

    /**
     * If true, strings should be rendered with Unicode fonts instead of the default.png font
     */
    private boolean unicodeFlag;

    /**
     * If true, the Unicode Bidirectional Algorithm should be run before rendering any string.
     */
    private boolean bidiFlag;

    /**
     * Used to specify new red value for the current color.
     */
    private float red;

    /**
     * Used to specify new blue value for the current color.
     */
    private float blue;

    /**
     * Used to specify new green value for the current color.
     */
    private float green;

    /**
     * Used to speify new alpha value for the current color.
     */
    private float alpha;

    /**
     * Text color of the currently rendering string.
     */
    private int textColor;

    /**
     * Set if the "k" style (random) is active in currently rendering string
     */
    private boolean randomStyle;

    /**
     * Set if the "l" style (bold) is active in currently rendering string
     */
    private boolean boldStyle;

    /**
     * Set if the "o" style (italic) is active in currently rendering string
     */
    private boolean italicStyle;

    /**
     * Set if the "n" style (underlined) is active in currently rendering string
     */
    private boolean underlineStyle;

    /**
     * Set if the "m" style (strikethrough) is active in currently rendering string
     */
    private boolean strikethroughStyle;
    public GameSettings gameSettings;
    public Location locationFontTextureBase;
    public float offsetBold = 1.0F;
    private final float[] charWidthFloat = new float[256];
    private boolean blend = false;
    private final GlBlendState oldBlendState = new GlBlendState();

    public FontRenderer(GameSettings gameSettingsIn, Location location, TextureManager textureManagerIn, boolean unicode) {
        this.gameSettings = gameSettingsIn;
        this.locationFontTextureBase = location;
        this.locationFontTexture = location;
        this.renderEngine = textureManagerIn;
        this.unicodeFlag = unicode;
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
        this.bindTexture(this.locationFontTexture);

        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (gameSettingsIn.anaglyph) {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }

        this.readGlyphSizes();
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
        this.asciiTexturesLoaded = false;
        Arrays.fill(unicodePageLocations, null);

        this.readFontTexture();
        this.readGlyphSizes();
        stringWidthMap.clear();
        callLists.values().forEach(GLAllocation::deleteDisplayLists);
        callLists.clear();
    }

    private void readFontTexture() {
        try (NativeBackedImage bufferedimage = TextureUtil.readBufferedImage(this.getResourceInputStream(this.locationFontTexture))) {
            Properties properties = FontUtils.readFontProperties(this.locationFontTexture);
            this.blend = FontUtils.readBoolean(properties, "blend", false);
            int i = bufferedimage.getWidth();
            int j = bufferedimage.getHeight();
            int k = i / 16;
            int l = j / 16;
            float f = (float) i / 128.0F;
            float f1 = Config.limit(f, 1.0F, 2.0F);
            this.offsetBold = 1.0F / f1;
            float f2 = FontUtils.readFloat(properties, "offsetBold", -1.0F);

            if (f2 >= 0.0F) {
                this.offsetBold = f2;
            }

            int[] aint = new int[i * j];
            bufferedimage.getRGB(0, 0, i, j, aint, 0, i);

            for (int i1 = 0; i1 < 256; ++i1) {
                int j1 = i1 % 16;
                int k1 = i1 / 16;
                int l1 = 0;

                for (l1 = k - 1; l1 >= 0; --l1) {
                    int i2 = j1 * k + l1;
                    boolean flag = true;

                    for (int j2 = 0; j2 < l && flag; ++j2) {
                        int k2 = (k1 * l + j2) * i;
                        int l2 = aint[i2 + k2];
                        int i3 = l2 >> 24 & 255;

                        if (i3 > 16) {
                            flag = false;
                            break;
                        }
                    }

                    if (!flag) {
                        break;
                    }
                }

                if (i1 == 65) {
                    i1 = i1;
                }

                if (i1 == 32) {
                    if (k <= 8) {
                        l1 = (int) (2.0F * f);
                    } else {
                        l1 = (int) (1.5F * f);
                    }
                }

                this.charWidthFloat[i1] = (float) (l1 + 1) / f + 1.0F;
            }

            FontUtils.readCustomCharWidths(properties, this.charWidthFloat);

            for (int j3 = 0; j3 < this.charWidth.length; ++j3) {
                this.charWidth[j3] = Math.round(this.charWidthFloat[j3]);
            }
        } catch (Exception ioexception1) {
            throw new RuntimeException(ioexception1);
        }
    }

    private void readGlyphSizes() {
        InputStream inputstream = null;

        try {
            inputstream = this.getResourceInputStream(Location.of("font/glyph_sizes.bin"));
            inputstream.read(this.glyphWidth);
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
        } finally {
            IOUtils.closeQuietly(inputstream);
        }
    }

    /**
     * Render the given char
     */
    private float renderChar(char ch, boolean italic) {
        if (ch != 32 && ch != 160) {
            int i = charMap[ch];
            return i != -1 && !this.unicodeFlag ? this.renderDefaultChar(i, italic) : this.renderUnicodeChar(ch, italic);
        } else {
            return !this.unicodeFlag ? this.charWidthFloat[ch] : 4.0F;
        }
    }

    /**
     * Render a single character with the default.png font at current (posX,posY) location...
     */
    private float renderDefaultChar(int ch, boolean italic) {
        int i = ch % 16 * 8;
        int j = ch / 16 * 8;
        int k = italic ? 1 : 0;
        this.bindTexture(this.locationFontTexture);
        if (!asciiTexturesLoaded)
            asciiTexturesLoaded = true;
        float f = this.charWidthFloat[ch];
        float f1 = 7.99F;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f((float) i / 128.0F, (float) j / 128.0F);
        GL11.glVertex3f(this.posX + (float) k, this.posY, 0.0F);
        GL11.glTexCoord2f((float) i / 128.0F, ((float) j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - (float) k, this.posY + 7.99F, 0.0F);
        GL11.glTexCoord2f(((float) i + f1 - 1.0F) / 128.0F, (float) j / 128.0F);
        GL11.glVertex3f(this.posX + f1 - 1.0F + (float) k, this.posY, 0.0F);
        GL11.glTexCoord2f(((float) i + f1 - 1.0F) / 128.0F, ((float) j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + f1 - 1.0F - (float) k, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        return f;
    }

    private Location getUnicodePageLocation(int page) {
        if (unicodePageLocations[page] == null) {
            unicodePageLocations[page] = Location.of(String.format("textures/font/unicode_page_%02x.png", page));
            unicodePageLocations[page] = FontUtils.getHdFontLocation(unicodePageLocations[page]);
        }

        return unicodePageLocations[page];
    }

    /**
     * Load one of the /font/glyph_XX.png into a new GL texture and store the texture ID in glyphTextureName array.
     */
    private void loadGlyphTexture(int page) {
        this.bindTexture(this.getUnicodePageLocation(page));
    }

    /**
     * Render a single Unicode character at current (posX,posY) location using one of the /font/glyph_XX.png files...
     */
    private float renderUnicodeChar(char ch, boolean italic) {
        if (this.glyphWidth[ch] == 0) {
            return 0.0F;
        } else {
            int i = ch / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[ch] >>> 4;
            int k = this.glyphWidth[ch] & 15;
            float f = (float) j;
            float f1 = (float) (k + 1);
            float f2 = (float) (ch % 16 * 16) + f;
            float f3 = (float) ((ch & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = italic ? 1.0F : 0.0F;
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, 0.0F);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    /**
     * Draws the specified string with a shadow.
     */
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return this.drawString(text, x, y, color, true);
    }

    public float drawString(String text, double x, double y, int color) {
        return drawString(text, (float) x, (float) y, color, true);
    }

    public int drawStringWithShadow(String text, double x, double y, int color) {
        return this.drawString(text, (float) x, (float) y, color, true);
    }

    public int getHeight() {
        return FONT_HEIGHT;
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String text, int x, int y, int color) {
        return this.drawString(text, (float) x, (float) y, color, false);
    }

    /**
     * Draws the specified string.
     */

    public void drawCenteredString(String s, float x, float y, int textColor) {
        drawString(s, (int) (x - getStringWidth(s) / 2f), (int) y, textColor);
    }

    public void drawCenteredString(String s, double x, double y, int textColor) {
        drawString(s, (int) (x - getStringWidth(s) / 2f), (int) y, textColor, true);
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {

        this.enableAlpha();

        if (this.blend) {
            GlStateManager.getBlendState(this.oldBlendState);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
        }

        this.resetStyles();
        int i;

        if (dropShadow) {
            i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
            this.resetStyles();
            i = Math.max(i, this.renderString(text, x, y, color, false));
        } else {
            i = this.renderString(text, x, y, color, false);
        }

        if (this.blend) {
            GlStateManager.setBlendState(this.oldBlendState);
        }

        return i;
    }

    public void drawStringSpecial(String text, float x, float y, int color) {

        this.enableAlpha();

        if (this.blend) {
            GlStateManager.getBlendState(this.oldBlendState);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
        }

        this.resetStyles();
        int i;

        i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
        GlStateManager.translate(0, 0, -1);
        i = Math.max(i, this.renderString(text, x, y, color, false));

        if (this.blend) {
            GlStateManager.setBlendState(this.oldBlendState);
        }

    }

    /**
     * Reset all style flag fields in the class to false; called at the start of string rendering
     */
    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    private final String listOfAsciiChars = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000";
    private final char[] asciiCharsArray = listOfAsciiChars.toCharArray();
    private final int[] charMap;

    {
        charMap = new int['\uFFFF' + 1];
        Arrays.fill(charMap, -1);
        for (int i = 0; i < asciiCharsArray.length; i++) {
            char c = asciiCharsArray[i];
            charMap[c] = i;
        }
    }

    Map<Tuple<String, Boolean>, Integer> callLists = new HashMap<>();

    private boolean isUnicodeCharLoaded(char ch) {
        int i = ch / 256;
        return unicodePageLocations[i] != null;
    }

    private void renderStringAtPosCallLists(String text, boolean shadow) {

        GlStateManager.enableTexture2D();

        Tuple<String, Boolean> key = Tuple.of(text, shadow);
        Integer callList = callLists.get(key);
        if (callList != null) {
            // get current binding texture
//            int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GlStateManager.translate(this.posX, this.posY, 0);
            GL11.glColor4f(this.red, this.green, this.blue, this.alpha);
            GlStateManager.callList(callList);
            GlStateManager.translate(-this.posX, -this.posY, 0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0f);
            GlStateManager.resetColor();
            GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName = -1;
            return;
        }

        char[] charArray = text.toCharArray();
        int length = text.length();
        boolean canCompile = true;

        for (int i = 0; i < length; ++i) {
            char currentChar = charArray[i];

            if (currentChar == '\247' && i + 1 < length) {
                char next = charArray[i + 1];
                if (next == 'k') {
                    canCompile = false;
                    break;
                }
                continue;
            }

            int idx = charMap[currentChar];
            boolean canRenderThisChar = idx != -1 && !this.unicodeFlag ? this.asciiTexturesLoaded : this.isUnicodeCharLoaded(currentChar);
            if (!canRenderThisChar) {
                canCompile = false;
                break;
            }
        }

        int cl = -1;
        float x = this.posX, y = this.posY;
        if (canCompile) {
            cl = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(cl, GL11.GL_COMPILE);
            this.posX = this.posY = 0;
        }

        for (int i = 0; i < length; ++i) {
            char currentChar = charArray[i];

            if (currentChar == '\247' && i + 1 < length) {
                char next = charArray[i + 1];

                if (next >= '0' && next <= 'r') {
                    if (next <= 'f') {
                        this.randomStyle = false;
                        this.boldStyle = false;
                        this.strikethroughStyle = false;
                        this.underlineStyle = false;
                        this.italicStyle = false;

                        int colorIndex = ((next - 'a') >= 0 ? (next - 'a' + 10) : (next - '0')) + (shadow ? 16 : 0);

                        int hexColor = this.colorCode[colorIndex];

                        if (Config.isCustomColors()) {
                            hexColor = CustomColors.getTextColor(colorIndex, hexColor);
                        }

                        this.textColor = hexColor;
                        GL11.glColor3f((float) (hexColor >> 16) / 255.0F, (float) (hexColor >> 8 & 255) / 255.0F, (float) (hexColor & 255) / 255.0F);
                    } else if (next == 'k') {
                        this.randomStyle = true;
                    } else if (next == 'l') {
                        this.boldStyle = true;
                    } else if (next == 'm') {
                        this.strikethroughStyle = true;
                    } else if (next == 'n') {
                        this.underlineStyle = true;
                    } else if (next == 'o') {
                        this.italicStyle = true;
                    } else if (next == 'r') {
                        this.randomStyle = false;
                        this.boldStyle = false;
                        this.strikethroughStyle = false;
                        this.underlineStyle = false;
                        this.italicStyle = false;
                        GL11.glColor3f(this.red, this.blue, this.green);
                    }
                }

                ++i;
            } else {
                int randomCharValue = -1;

                if (currentChar < charMap.length && (randomCharValue = charMap[currentChar]) != -1 && this.randomStyle) {
                    int desiredCharWidth = this.getCharWidth(currentChar);
                    char charWithTheSameWidth;

                    do {
                        randomCharValue = this.fontRandom.nextInt(asciiCharsArray.length);
                        charWithTheSameWidth = asciiCharsArray[randomCharValue];
                    } while (desiredCharWidth != this.getCharWidth(charWithTheSameWidth));

                    currentChar = charWithTheSameWidth;
                }

                boolean canCharBeRandomized = randomCharValue != -1;

                float boldOffset = canCharBeRandomized && !this.unicodeFlag ? this.offsetBold : 0.5F;
                boolean flag = (currentChar == 0 || !canCharBeRandomized || this.unicodeFlag) && shadow;

                if (flag) {
                    this.posX -= boldOffset;
                    this.posY -= boldOffset;
                }

                float charWidth = this.renderChar(currentChar, this.italicStyle);

                if (flag) {
                    this.posX += boldOffset;
                    this.posY += boldOffset;
                }

                if (this.boldStyle) {
                    this.posX += boldOffset;

                    if (flag) {
                        this.posX -= boldOffset;
                        this.posY -= boldOffset;
                    }

                    this.renderChar(currentChar, this.italicStyle);
                    this.posX -= boldOffset;

                    if (flag) {
                        this.posX += boldOffset;
                        this.posY += boldOffset;
                    }

                    charWidth += boldOffset;
                }

                this.doDraw(charWidth);
            }
        }

        if (canCompile) {
            GL11.glEndList();
            callLists.put(key, cl);
            this.posX = x;
            this.posY = y;
            GlStateManager.translate(this.posX, this.posY, 0);
            GlStateManager.callList(cl);
            GlStateManager.translate(-this.posX, -this.posY, 0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0f);
            GlStateManager.resetColor();
            GlStateManager.textureState[GlStateManager.activeTextureUnit].textureName = -1;
        }
    }

    /**
     * Render a single line string at the current (posX,posY) and update posX
     */
    private void renderStringAtPos(String text, boolean shadow) {

        GlStateManager.enableTexture2D();

        char[] charArray = text.toCharArray();
        for (int i = 0; i < text.length(); ++i) {
            char currentChar = charArray[i];

            if (currentChar == '\247' && i + 1 < text.length()) {
                char next = charArray[i + 1];

                if (next >= '0' && next <= 'r') {
                    if (next <= 'f') {
                        this.randomStyle = false;
                        this.boldStyle = false;
                        this.strikethroughStyle = false;
                        this.underlineStyle = false;
                        this.italicStyle = false;

                        int colorIndex = ((next - 'a') >= 0 ? (next - 'a' + 10) : (next - '0')) + (shadow ? 16 : 0);

                        int hexColor = this.colorCode[colorIndex];

                        if (Config.isCustomColors()) {
                            hexColor = CustomColors.getTextColor(colorIndex, hexColor);
                        }

                        this.textColor = hexColor;
                        this.setColor((float) (hexColor >> 16) / 255.0F, (float) (hexColor >> 8 & 255) / 255.0F, (float) (hexColor & 255) / 255.0F, this.alpha);
                    } else if (next == 'k') {
                        this.randomStyle = true;
                    } else if (next == 'l') {
                        this.boldStyle = true;
                    } else if (next == 'm') {
                        this.strikethroughStyle = true;
                    } else if (next == 'n') {
                        this.underlineStyle = true;
                    } else if (next == 'o') {
                        this.italicStyle = true;
                    } else if (next == 'r') {
                        this.randomStyle = false;
                        this.boldStyle = false;
                        this.strikethroughStyle = false;
                        this.underlineStyle = false;
                        this.italicStyle = false;
                        this.setColor(this.red, this.blue, this.green, this.alpha);
                    }
                }

                ++i;
            } else {
                int randomCharValue = -1;

                if (currentChar < charMap.length && (randomCharValue = charMap[currentChar]) != -1 && this.randomStyle) {
                    int desiredCharWidth = this.getCharWidth(currentChar);
                    char charWithTheSameWidth;

                    do {
                        randomCharValue = this.fontRandom.nextInt(asciiCharsArray.length);
                        charWithTheSameWidth = asciiCharsArray[randomCharValue];
                    } while (desiredCharWidth != this.getCharWidth(charWithTheSameWidth));

                    currentChar = charWithTheSameWidth;
                }

                boolean canCharBeRandomized = randomCharValue != -1;

                float boldOffset = canCharBeRandomized && !this.unicodeFlag ? this.offsetBold : 0.5F;
                boolean flag = (currentChar == 0 || !canCharBeRandomized || this.unicodeFlag) && shadow;

                if (flag) {
                    this.posX -= boldOffset;
                    this.posY -= boldOffset;
                }

                float charWidth = this.renderChar(currentChar, this.italicStyle);

                if (flag) {
                    this.posX += boldOffset;
                    this.posY += boldOffset;
                }

                if (this.boldStyle) {
                    this.posX += boldOffset;

                    if (flag) {
                        this.posX -= boldOffset;
                        this.posY -= boldOffset;
                    }

                    this.renderChar(currentChar, this.italicStyle);
                    this.posX -= boldOffset;

                    if (flag) {
                        this.posX += boldOffset;
                        this.posY += boldOffset;
                    }

                    charWidth += boldOffset;
                }

                this.doDraw(charWidth);
            }
        }
    }

    protected void doDraw(float p_doDraw_1_) {
        if (this.strikethroughStyle) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION);
            worldrenderer.pos(this.posX, this.posY + (float) (this.FONT_HEIGHT / 2), 0.0D).endVertex();
            worldrenderer.pos(this.posX + p_doDraw_1_, this.posY + (float) (this.FONT_HEIGHT / 2), 0.0D).endVertex();
            worldrenderer.pos(this.posX + p_doDraw_1_, this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F, 0.0D).endVertex();
            worldrenderer.pos(this.posX, this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        if (this.underlineStyle) {
            Tessellator tessellator1 = Tessellator.getInstance();
            WorldRenderer worldrenderer1 = tessellator1.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
            int i = this.underlineStyle ? -1 : 0;
            worldrenderer1.pos(this.posX + (float) i, this.posY + (float) this.FONT_HEIGHT, 0.0D).endVertex();
            worldrenderer1.pos(this.posX + p_doDraw_1_, this.posY + (float) this.FONT_HEIGHT, 0.0D).endVertex();
            worldrenderer1.pos(this.posX + p_doDraw_1_, this.posY + (float) this.FONT_HEIGHT - 1.0F, 0.0D).endVertex();
            worldrenderer1.pos(this.posX + (float) i, this.posY + (float) this.FONT_HEIGHT - 1.0F, 0.0D).endVertex();
            tessellator1.draw();
            GlStateManager.enableTexture2D();
        }

        this.posX += p_doDraw_1_;
    }

    /**
     * Render string either left or right aligned depending on bidiFlag
     */
    private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow) {
        return this.renderString(text, (float) x, (float) y, color, dropShadow);
    }

    /**
     * Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos()
     */
    private int renderString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {

            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            if (dropShadow) {
                color = (color & 16579836) >> 2 | color & -16777216;
            }

            this.red = (float) (color >> 16 & 255) / 255.0F;
            this.blue = (float) (color >> 8 & 255) / 255.0F;
            this.green = (float) (color & 255) / 255.0F;
            this.alpha = (float) (color >> 24 & 255) / 255.0F;
            this.setColor(this.red, this.blue, this.green, this.alpha);
            this.posX = x;
            this.posY = y;
            if (CommandValues.getValues().experimental_fontrenderer_optimization)
                this.renderStringAtPosCallLists(text, dropShadow);
            else
                this.renderStringAtPos(text, dropShadow);
            return (int) this.posX;
        }
    }

    public final Map<String, Integer> stringWidthMap = new HashMap<>();

    /**
     * Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s).
     */
    public int getStringWidth(String text) {

//        RenderTextEvent call = EventManager.call(new RenderTextEvent(text));
//
//        text = call.getText();

        if (text == null) {
            return 0;
        } else {

            Integer cachedWidth = this.stringWidthMap.get(text);
            if (cachedWidth != null) {
                return cachedWidth;
            }

            float f = 0.0F;
            boolean flag = false;

            for (int i = 0; i < text.length(); ++i) {
                char c0 = text.charAt(i);
                float f1 = this.getCharWidthFloat(c0);

                if (f1 < 0.0F && i < text.length() - 1) {
                    ++i;
                    c0 = text.charAt(i);

                    if (c0 != 108 && c0 != 76) {
                        if (c0 == 114 || c0 == 82) {
                            flag = false;
                        }
                    } else {
                        flag = true;
                    }

                    f1 = 0.0F;
                }

                f += f1;

                if (flag && f1 > 0.0F) {
                    f += this.unicodeFlag ? 1.0F : this.offsetBold;
                }
            }

            int result = Math.round(f);

            this.stringWidthMap.put(text, result);

            return result;
        }
    }

    /**
     * Returns the width of this character as rendered.
     */
    public int getCharWidth(char character) {
        return Math.round(this.getCharWidthFloat(character));
    }

    private float getCharWidthFloat(char charIn) {
        if (charIn == 167) {
            return -1.0F;
        } else if (charIn != 32 && charIn != 160) {
            int i = charMap[charIn];

            if (charIn > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidthFloat[i];
            } else if (this.glyphWidth[charIn] != 0) {
                int j = this.glyphWidth[charIn] >>> 4;
                int k = this.glyphWidth[charIn] & 15;

                if (k > 7) {
                    k = 15;
                    j = 0;
                }

                ++k;
                return (float) ((k - j) / 2 + 1);
            } else {
                return 0.0F;
            }
        } else {
            return this.charWidthFloat[32];
        }
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float) width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidthFloat(c0);

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;

                if (flag1) {
                    ++f;
                }
            }

            if (f > (float) width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    /**
     * Remove all newline characters from the end of the string
     */
    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    /**
     * Splits and draws a String with wordwrap (maximum length is parameter k)
     */
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        if (this.blend) {
            GlStateManager.getBlendState(this.oldBlendState);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
        }

        this.resetStyles();
        this.textColor = textColor;
        str = this.trimStringNewline(str);
        this.renderSplitString(str, x, y, wrapWidth, false);

        if (this.blend) {
            GlStateManager.setBlendState(this.oldBlendState);
        }
    }

    /**
     * Perform actual work of rendering a multi-line string with wordwrap and with darker drop shadow color if flag is
     * set
     */
    private void renderSplitString(String str, int x, int y, int wrapWidth, boolean addShadow) {
        for (String s : this.listFormattedStringToWidth(str, wrapWidth)) {
            this.renderStringAligned(s, x, y, wrapWidth, this.textColor, addShadow);
            y += this.FONT_HEIGHT;
        }
    }

    /**
     * Returns the width of the wordwrapped String (maximum length is parameter k)
     *
     * @param str       The string to split
     * @param maxLength The maximum length of a word
     */
    public int splitStringWidth(String str, int maxLength) {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
    }

    /**
     * Set unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
     * font.
     */
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        this.unicodeFlag = unicodeFlagIn;
    }

    /**
     * Get unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
     * font.
     */
    public boolean getUnicodeFlag() {
        return this.unicodeFlag;
    }

    /**
     * Set bidiFlag to control if the Unicode Bidirectional Algorithm should be run before rendering any string.
     */
    public void setBidiFlag(boolean bidiFlagIn) {
        this.bidiFlag = bidiFlagIn;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    /**
     * Inserts newline and formatting into a string to wrap it within the specified width.
     */
    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        int i = this.sizeStringToWidth(str, wrapWidth);

        if (str.length() <= i) {
            return str;
        } else {
            // at least advance 1 char
            if (i <= 0) {
                i = 1;
            }

            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == 32 || c0 == 10;
            String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    /**
     * Determines how many characters from the string will fit into the specified width.
     */
    private int sizeStringToWidth(String str, int wrapWidth) {
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for (boolean flag = false; j < i; ++j) {
            char c0 = str.charAt(j);

            switch (c0) {
                case '\n':
                    --j;
                    break;

                case ' ':
                    k = j;

                default:
                    f += (float) this.getCharWidth(c0);

                    if (flag) {
                        ++f;
                    }

                    break;

                case '§':
                    if (j < i - 1) {
                        ++j;
                        char c1 = str.charAt(j);

                        if (c1 != 108 && c1 != 76) {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10) {
                ++j;
                k = j;
                break;
            }

            if (Math.round(f) > wrapWidth) {
                break;
            }
        }

        return j != i && k != -1 && k < j ? k : j;
    }

    /**
     * Checks if the char code is a hexadecimal character, used to set colour.
     */
    private static boolean isFormatColor(char colorChar) {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }

    /**
     * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
     */
    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 107 && formatChar <= 111 || formatChar >= 75 && formatChar <= 79 || formatChar == 114 || formatChar == 82;
    }

    /**
     * Digests a string for nonprinting formatting characters then returns a string containing only that formatting.
     */
    public static String getFormatFromString(String text) {
        String s = "";
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);

                if (isFormatColor(c0)) {
                    s = "§" + c0;
                } else if (isFormatSpecial(c0)) {
                    s = s + "§" + c0;
                }
            }
        }

        return s;
    }

    /**
     * Get bidiFlag that controls if the Unicode Bidirectional Algorithm should be run before rendering any string
     */
    public boolean getBidiFlag() {
        return this.bidiFlag;
    }

    public int getColorCode(char character) {
        int i = "0123456789abcdef".indexOf(character);

        if (i >= 0 && i < this.colorCode.length) {
            int j = this.colorCode[i];

            if (Config.isCustomColors()) {
                j = CustomColors.getTextColor(i, j);
            }

            return j;
        } else {
            return 16777215;
        }
    }

    protected void setColor(float p_setColor_1_, float p_setColor_2_, float p_setColor_3_, float p_setColor_4_) {
        GlStateManager.color(p_setColor_1_, p_setColor_2_, p_setColor_3_, p_setColor_4_);
    }

    protected void enableAlpha() {
        GlStateManager.enableAlpha();
    }

    protected void bindTexture(Location p_bindTexture_1_) {
        this.renderEngine.bindTexture(p_bindTexture_1_);
    }

    protected InputStream getResourceInputStream(Location p_getResourceInputStream_1_) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(p_getResourceInputStream_1_).getInputStream();
    }

    public int getWidth(String text) {
        return this.getStringWidth(text);
    }
}
