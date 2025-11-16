package tritium.screens.clickgui.value.values;

import org.lwjgl.input.Mouse;
import tritium.rendering.HSBColor;
import tritium.rendering.entities.impl.GradientRect;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.settings.ColorSetting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 19:27
 */
public class ColorRenderer extends AbstractWidget<ColorRenderer> {

    private final ColorSetting setting;

    public ColorRenderer(ColorSetting setting) {
        this.setting = setting;
        this.setBounds(142, 100);
        this.setColor(setting.getRGB());

        this.hueMap = new ArrayList<>();
        this.refreshHue();
    }

    List<Color> hueMap;
    double zWidth = 100, zHeight = 100;
    public boolean lmbPressed;

    @Override
    public double getHeight() {

        this.setHidden(!setting.shouldRender());

        if (!setting.shouldRender())
            return 0;
        return super.getHeight();
    }

    @Override
    public void onRender(double mouseX, double mouseY) {
        double positionX = this.getX();
        double positionY = this.getY();

        float hue = this.getValue().getHue();
        Color color = this.getValue().getColor();

        new GradientRect(positionX, positionY, zWidth, zHeight,
                this.resetAlpha(Color.getHSBColor(hue, 1, 1), this.getMenuAlpha()), this.resetAlpha(Color.getHSBColor(hue, 1, 1), this.getMenuAlpha()), GradientRect.RenderType.Expand,
                GradientRect.GradientType.Horizontal).draw();
        new GradientRect(positionX, positionY, zWidth, zHeight,
                this.resetAlpha(Color.getHSBColor(hue, 0, 1), this.getMenuAlpha()), 0x00F, GradientRect.RenderType.Expand,
                GradientRect.GradientType.Horizontal).draw();
        new GradientRect(positionX, positionY, zWidth, zHeight, 0x00F,
                this.resetAlpha(Color.getHSBColor(hue, 1, 0), this.getMenuAlpha()), GradientRect.RenderType.Expand,
                GradientRect.GradientType.Vertical).draw();

        this.drawOutsideRect(positionX + this.getValue().getSaturation() * zWidth,
                positionY + (1.0f - this.getValue().getBrightness()) * zHeight, 1, 1, 0.5,
                new Color(32, 32, 32, this.getMenuAlpha()).getRGB());


//        double r = 4;
//        this.roundedOutline(positionX + this.getValue().getSaturation() * zWidth - r,
//                positionY + (1.0f - this.getValue().getBrightness()) * zHeight - r, r * 2, r * 2, r - 0.5, 2, Color.WHITE);
//
//        this.roundedRect(positionX + this.getValue().getSaturation() * zWidth - r,
//                positionY + (1.0f - this.getValue().getBrightness()) * zHeight - r, r * 2, r * 2, r * 0.5, -0.75, this.getValue().getColor());


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
            Rect.draw(positionX + zWidth + 4, positionY + index, 8, 1,
                    this.resetAlpha(this.hueMap.get(index), this.getMenuAlpha()), Rect.RectType.EXPAND);
        }

        Rect.draw(positionX + zWidth + 4, positionY + this.getValue().getHue() * zHeight - .5, 8, 1, RenderSystem.hexColor(0, 0, 0, getMenuAlpha()));

//        roundedRect(positionX + this.getValue().getHue() * zWidth, positionY + zHeight + 7, 0, 0, 3, 4, Color.WHITE);

        if (RenderSystem.isHovered(mouseX, mouseY, positionX + zWidth + 4, positionY - 1, 8, zHeight + 2) && Mouse.isButtonDown(0)) {
            lmbPressed = true;

            double pos = mouseY - positionY;
            if (pos < 0) {
                pos = 0;
            }
            if (pos > zHeight) {
                pos = zHeight;
            }

            float h = this.getValue().getHue();
            float dest = (float) (pos / zHeight);

            this.getValue().setHue(dest);

            if (h != dest) {
                this.setting.onValueChanged(this.getValue());
            }
        }

        for (int xExt = 0; xExt < 4; xExt++)
            for (int yExt = 0; yExt < zHeight / 2; yExt++)
                Rect.draw(positionX + zWidth + (xExt * 2) + 16, positionY + (yExt * 2), 2, 2,
                        this.resetAlpha((((xExt % 2 == 0) == (yExt % 2 == 0)) ? Color.WHITE : new Color(190, 190, 190)), this.getMenuAlpha()),
                        Rect.RectType.EXPAND);

        new GradientRect(positionX + zWidth + 16, positionY, 8, zHeight, 0x00F,
                this.resetAlpha(color, this.getMenuAlpha()),
                GradientRect.RenderType.Expand, GradientRect.GradientType.Vertical).draw();

        Rect.draw(positionX + zWidth + 16, positionY + (this.getValue().getAlpha() * 0.003921568627451F) * zHeight, 8, 1, RenderSystem.hexColor(0, 0, 0, getMenuAlpha()));
//        roundedRect(, 0, 0, 3, 4, Color.WHITE);

        if (RenderSystem.isHovered(mouseX, mouseY, positionX + zWidth + 16, positionY, 8, zHeight) && Mouse.isButtonDown(0)) {
            lmbPressed = true;

            double pos = mouseY - positionY;
            if (pos < 0) {
                pos = 0;
            }
            if (pos > zHeight) {
                pos = zHeight;
            }

            int a = this.getValue().getAlpha();
            int dest = (int) ((pos / zHeight) * 255.0f);

            this.getValue().setAlpha(dest);

            if (a != dest) {
                this.setting.onValueChanged(this.getValue());
            }
        }
    }

    private HSBColor getValue() {
        return this.setting.getValue();
    }

    private int getMenuAlpha() {
        return (int) (this.getAlpha() * 255);
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
