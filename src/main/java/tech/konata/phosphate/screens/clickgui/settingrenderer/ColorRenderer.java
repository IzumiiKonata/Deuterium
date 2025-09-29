package tech.konata.phosphate.screens.clickgui.settingrenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.rendering.RoundedRect;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.entities.impl.GradientRect;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.rendering.shader.StencilShader;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.settings.ColorSetting;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2023/12/31
 */
public class ColorRenderer extends SettingRenderer<ColorSetting> {

    List<Color> hueMap;
    double zWidth = 100, zHeight = 100;
    int alp = 255;

    public static ColorRenderer floatingPane = null;

    public ColorRenderer(ColorSetting settingIn) {
        super(settingIn);

        this.hueMap = new ArrayList<>();
        this.refreshHue();
    }

    @Override
    public double render(double mouseX, double mouseY, int dWheel) {

        CFontRenderer pf20 = FontManager.pf20;

        pf20.drawString(this.setting.getName().get(), x, y + (pf20.getHeight() + 8) * 0.5 - pf20.getHeight() * 0.5 - 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double rWidth = 28, rHeight = 14, rX = x + width - rWidth;

        this.roundedRect(rX, y + (pf20.getHeight() + 8) * 0.5 - rHeight * 0.5, rWidth, rHeight, 4, 2, ThemeManager.getAsColor(ThemeManager.ThemeColor.Text, 200));
        this.roundedRect(rX, y + (pf20.getHeight() + 8) * 0.5 - rHeight * 0.5, rWidth, rHeight, 4, 1.5, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, 255));
        this.roundedRect(rX, y + (pf20.getHeight() + 8) * 0.5 - rHeight * 0.5, rWidth, rHeight, 2, this.getValue().getColor());

        Color color = this.getValue().getColor();

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        String render = ("#" + toUnsignedString0(red, 4) + toUnsignedString0(green, 4) + toUnsignedString0(blue, 4)).toUpperCase();

        pf20.drawString(render, rX - rWidth - pf20.getStringWidth("#FFFFFF"), y + (pf20.getHeight() + 8) * 0.5 - pf20.getHeight() * 0.5 - 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text, 160));

        return pf20.getHeight() - 2;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        double rWidth = 28, rHeight = 14, rX = x + width - rWidth;

        if (isHovered(mouseX, mouseY, rX, y + (FontManager.pf20.getHeight() + 8) * 0.5 - rHeight * 0.5, rWidth, rHeight)) {

            if (this == floatingPane) {
                floatingPane = null;
                return;
            }

            floatingPane = this;
            this.lmbPressed = true;
            paneX = this.x + width - rWidth - 2;
            paneY = this.y + rHeight + 8;
        }

    }

    final char[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    void formatUnsignedInt(int val, int shift, char[] buf, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[val & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

    }

    private String toUnsignedString0(int val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedInt(val, shift, buf, chars);

        // Use special constructor which takes over "buf".

        String s = new String(buf);

        if (s.length() == 1) {
            s = "0" + s;
        }

        return s;
    }

    public double paneX, paneY;

    RoundedRect texRR = new RoundedRect();

    public boolean lmbPressed;

    public void renderPane(double mouseX, double mouseY) {

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        double paneWidth = 116;
        double paneHeight = 170;

        roundedRect(paneX, paneY, paneWidth, paneHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, 255));

        if (!isHovered(mouseX, mouseY, paneX, paneY, paneWidth, paneHeight) && Mouse.isButtonDown(0) && !lmbPressed) {
            floatingPane = null;
            return;
        }

        double positionX = paneX + 8;
        double positionY = paneY + 38;

        Color color = this.getValue().getColor();
        roundedRect(paneX + 8, paneY + 8, paneWidth - 16, 16, 3, color);

        CFontRenderer pf16 = FontManager.pf16;

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        String render = ("#" + toUnsignedString0(red, 4) + toUnsignedString0(green, 4) + toUnsignedString0(blue, 4)).toUpperCase();

        pf16.drawCenteredString(render, paneX + 8 + (paneWidth - 16) * 0.5, paneY + 28, ThemeManager.get(ThemeManager.ThemeColor.Text));

        float hue = this.getValue().getHue();

        new GradientRect(positionX, positionY, zWidth, zHeight,
                Color.getHSBColor(hue, 1, 1).getRGB(), Color.getHSBColor(hue, 1, 1).getRGB(), GradientRect.RenderType.Expand,
                GradientRect.GradientType.Horizontal).draw();
        new GradientRect(positionX, positionY, zWidth, zHeight,
                this.resetAlpha(Color.getHSBColor(hue, 0, 1), this.getMenuAlpha()), 0x00F, GradientRect.RenderType.Expand,
                GradientRect.GradientType.Horizontal).draw();
        new GradientRect(positionX, positionY, zWidth, zHeight, 0x00F,
                this.resetAlpha(Color.getHSBColor(hue, 1, 0), this.getMenuAlpha()), GradientRect.RenderType.Expand,
                GradientRect.GradientType.Vertical).draw();

        double r = 4;
        this.roundedOutline(positionX + this.getValue().getSaturation() * zWidth - r,
                positionY + (1.0f - this.getValue().getBrightness()) * zHeight - r, r * 2, r * 2, r - 0.5, 2, Color.WHITE);

        this.roundedRect(positionX + this.getValue().getSaturation() * zWidth - r,
                positionY + (1.0f - this.getValue().getBrightness()) * zHeight - r, r * 2, r * 2, r * 0.5, -0.75, this.getValue().getColor());


        if (RenderSystem.isHovered(mouseX, mouseY, positionX - 4, positionY - 4, zWidth + 8, zHeight + 6) && Mouse.isButtonDown(0)) {
            lmbPressed = true;
            float posX = (float) ((float) mouseX - (positionX));
            float posY = (float) ((float) mouseY - (positionY));
            if (posX < 0) {
                posX = 0;
            }
            if (posX > zWidth) {
                posX = (float) zWidth;
            }
            if (posY < 0) {
                posY = 0;
            }
            if (posY > zHeight) {
                posY = (float) zHeight;
            }

            float saturation = this.getValue().getSaturation();
            float destSaturation = (float) (posX / zWidth);

            float brightness = this.getValue().getBrightness();
            float destBrightness = (float) ((zHeight - posY) / zHeight);

            this.getValue().setSaturation(destSaturation);
            this.getValue().setBrightness(destBrightness);

            if (saturation != destSaturation || brightness != destBrightness) {
                this.setting.onValueChanged(this.getValue());
            }

        }

        for (int index = 0; index < zHeight; index++) {
            Rect.draw(positionX + index, positionY + zHeight + 4, 1, 6,
                    this.resetAlpha(this.hueMap.get(index), this.getMenuAlpha()), Rect.RectType.EXPAND);
        }

        roundedRect(positionX + this.getValue().getHue() * zHeight, positionY + zHeight + 7, 0, 0, 3, 4, Color.WHITE);

        if (RenderSystem.isHovered(mouseX, mouseY, positionX - 1, positionY + zHeight + 2, zWidth + 4, 6 + 4) && Mouse.isButtonDown(0)) {
            lmbPressed = true;

            double pos = mouseX - positionX;
            if (pos < 0) {
                pos = 0;
            }
            if (pos > zWidth) {
                pos = zWidth;
            }

            float h = this.getValue().getHue();
            float dest = (float) (pos / zWidth);

            this.getValue().setHue(dest);

            if (h != dest) {
                this.setting.onValueChanged(this.getValue());
            }
        }

        for (int xExt = 0; xExt < zWidth / 2; xExt++)
            for (int yExt = 0; yExt < 3; yExt++)
                Rect.draw(positionX + (xExt * 2), positionY + zHeight + 16.5F + (yExt * 2), 2, 2,
                        this.resetAlpha((((xExt % 2 == 0) == (yExt % 2 == 0)) ? Color.WHITE : new Color(190, 190, 190)), this.getMenuAlpha()),
                        Rect.RectType.EXPAND);

        new GradientRect(positionX, positionY + zHeight + 16.5F, zWidth, 6, 0x00F,
                this.resetAlpha(color, this.getMenuAlpha()),
                GradientRect.RenderType.Expand, GradientRect.GradientType.Horizontal).draw();

        roundedRect(positionX + (this.getValue().getAlpha() * 0.003921568627451F) * zWidth, positionY + zHeight + 19.5f, 0, 0, 3, 4, Color.WHITE);


//        RenderSystem.circle(, 4, -1);

        if (RenderSystem.isHovered(mouseX, mouseY, positionX - 2, positionY + zHeight + 16.5F - 2, zWidth + 4, 6 + 4) && Mouse.isButtonDown(0)) {
            lmbPressed = true;

            double pos = mouseX - positionX;
            if (pos < 0) {
                pos = 0;
            }
            if (pos > zWidth) {
                pos = zWidth;
            }

            int a = this.getValue().getAlpha();
            int dest = (int) ((pos / zWidth) * 255.0f);

            this.getValue().setAlpha(dest);

            if (a != dest) {
                this.setting.onValueChanged(this.getValue());
            }
        }

//        for (int yExt = 0; yExt < zHeight / 2; yExt++)
//            for (int xExt = 0; xExt < 2; xExt++)
//                Rect.draw(positionX + zWidth + 28 + (xExt * 2), positionY + (yExt * 2), 2, 2,
//                        this.resetAlpha((((yExt % 2 == 0) == (xExt % 2 == 0)) ? Color.WHITE : new Color(190, 190, 190)),
//                                this.getMenuAlpha()),
//                        Rect.RectType.EXPAND);
//
//        Rect.draw(positionX + zWidth + 28, positionY, 4, zHeight,
//                this.resetAlpha(this.getValue().getColor(),
//                        Math.min(this.getMenuAlpha(), this.getValue().getColor().getAlpha())),
//                Rect.RectType.EXPAND);

//        FontManager.pf16.drawString(this.getValue().getHue() + ":" + this.getValue().getSaturation() + ":" + this.getValue().getBrightness(), positionX + zWidth + 50, positionY, Color.BLACK.getRGB());

    }

    private HSBColor getValue() {
        return this.setting.getValue();
    }

    public void setRenderAlpha(int alpha) {
        alp = alpha;
    }

    private int getMenuAlpha() {
        return alp;
    }

    private void refreshHue() {
        this.hueMap.clear();
        for (int index = 0; index < zHeight; index++) {
            this.hueMap.add(Color.getHSBColor((float) (index / (zHeight)), 1.0f, 1.0f));
        }
    }

    private int resetAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }

    private void drawOutsideRect(double x, double y, double x2, double y2, double zWidth, int color) {
        this.drawOutsideRect2(x, y, x + x2, y + y2, zWidth, color);
    }

    private void drawOutsideRect2(double x, double y, double x2, double y2, double zWidth, int color) {
        if (x > x2) {
            double i = x;
            x = x2;
            x2 = i;
        }

        if (y > y2) {
            double j = y;
            y = y2;
            y2 = j;
        }

        Rect.draw(x, y - zWidth, x - zWidth, y2, color, Rect.RectType.ABSOLUTE_POSITION);
        Rect.draw(x, y, x2 + zWidth, y - zWidth, color, Rect.RectType.ABSOLUTE_POSITION);
        Rect.draw(x2, y, x2 + zWidth, y2 + zWidth, color, Rect.RectType.ABSOLUTE_POSITION);
        Rect.draw(x - zWidth, y2, x2, y2 + zWidth, color, Rect.RectType.ABSOLUTE_POSITION);
    }
}
