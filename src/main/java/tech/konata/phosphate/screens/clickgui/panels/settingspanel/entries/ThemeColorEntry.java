package tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries;

import net.minecraft.util.Location;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.AccentColor;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.SettingEntry;
import tech.konata.phosphate.screens.clickgui.settingrenderer.ModeRenderer;
import tech.konata.phosphate.settings.GlobalSettings;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 2024/10/26 17:34
 */
public class ThemeColorEntry extends SettingEntry {

    public ThemeColorEntry() {
        super("Theme Color");
        super.imgLocation = Location.of(Phosphate.NAME + "/textures/settings/color.svg");
    }

    Localizable lThemeColor = Localizable.of("panel.settings.theme_color.theme_color");
    Localizable lAccentColor = Localizable.of("panel.settings.theme_color.accent_color");
    Localizable lHudStyle = Localizable.of("panel.settings.theme_color.hud_style");

    boolean lmbPressed = false;

    final ModeRenderer modeRenderer = (ModeRenderer) SettingRenderer.of(GlobalSettings.HUD_STYLE);

    @Override
    public void onRender(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {
        this.renderThemeColor(mouseX, mouseY, posX, posY, width, height, dWheel);
        this.renderAccentColor(mouseX, mouseY, posX, posY, width, height, dWheel);
        this.renderHudStyle(mouseX, mouseY, posX, posY, width, height, dWheel);

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;
    }

    double hudPanelHeight = 44;

    private void renderHudStyle(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        double offsetY = posY + 220;

        roundedRect(posX, offsetY, width, hudPanelHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        FontManager.pf40.drawString(lHudStyle.get(), posX + 8, offsetY + 8, ThemeManager.get(ThemeManager.ThemeColor.Text));

        this.modeRenderer.x = posX + 8;
        this.modeRenderer.y = offsetY + 12 + FontManager.pf40.getHeight();
        this.modeRenderer.width = width - 16;
        this.modeRenderer.useAlternativeColor = true;
        hudPanelHeight = 44 + this.modeRenderer.render(mouseX, mouseY, dWheel);
    }

    public double accentColorScrollOffset = 0, accentColorScrollSmooth = 0;

    private void renderAccentColor(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        double offsetY = posY + 110;

        double panelHeight = 100;

        roundedRect(posX, offsetY, width, panelHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        FontManager.pf40.drawString(lAccentColor.get(),posX + 8, offsetY + 8, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double offsetX = posX + 16 + accentColorScrollOffset;
        double colorSize = 48;
        double colorSpacing = 8;
        double colorRadius = 8;

        double scrollSpeed = 20;

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            scrollSpeed *= 2;

        if (dWheel != 0 && isHovered(mouseX, mouseY, posX + 8, offsetY + 28, width - 16, panelHeight - 36)) {

            if (dWheel > 0) {
                accentColorScrollSmooth += scrollSpeed;
            } else {
                accentColorScrollSmooth -= scrollSpeed;
            }

        }

        accentColorScrollSmooth = Interpolations.interpBezier(accentColorScrollSmooth, 0, 0.2f);
        accentColorScrollOffset = Interpolations.interpBezier(accentColorScrollOffset, accentColorScrollOffset + accentColorScrollSmooth, 0.2f);

        if (accentColorScrollOffset > 0) {
            accentColorScrollOffset = Interpolations.interpBezier(accentColorScrollOffset, 0, 0.2f);
        }

        double totalHeight = (colorSize + colorSpacing) * (ThemeManager.getColors().size());

        double target = totalHeight - (width - 16) + colorSpacing;

        if (totalHeight > width - 16) {
            if (accentColorScrollOffset < -target) {
                accentColorScrollOffset = Interpolations.interpBezier(accentColorScrollOffset, -target, 0.2f);
            }
        } else {
            accentColorScrollOffset = Interpolations.interpBezier(accentColorScrollOffset, 0, 0.2f);
        }

        Stencil.write();
        Rect.draw(posX + 8, offsetY + 28, width - 16, panelHeight - 36, -1, Rect.RectType.EXPAND);
        Stencil.erase();

        for (AccentColor color : ThemeManager.getColors()) {

            if (offsetX + colorSize < posX + 8) {
                offsetX += colorSize + colorSpacing;
                continue;
            }

            if (offsetX > posX + width - 8)
                break;

            this.roundedRectAccentColor(offsetX, offsetY + panelHeight - 16 - colorSize, colorSize, colorSize, colorRadius, color);

            if (ThemeManager.getAccentColor() == color) {
                SVGImage.draw(Location.of(Phosphate.NAME + "/textures/check.svg"), offsetX, offsetY + panelHeight - 16 - colorSize, colorSize, colorSize);
            }

            boolean hovered = isHovered(mouseX, mouseY, offsetX, offsetY + panelHeight - 16 - colorSize, colorSize, colorSize);

            if (hovered && Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;
                ThemeManager.setAccentColor(color);
                GlobalSettings.ACCENT_COLOR.setValue(color.getName());
            }

            offsetX += colorSize + colorSpacing;
        }

        Stencil.dispose();

    }


    private void renderThemeColor(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        double panelHeight = 100;

        roundedRect(posX, posY, width, panelHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        FontManager.pf40.drawString(lThemeColor.get(), posX + 8, posY + 8, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double offsetX = posX + 16;
        double colorSize = 48;
        double colorRadius = 8;

        for (ThemeManager.Theme value : ThemeManager.Theme.values()) {
            roundedRect(offsetX, posY + panelHeight - 16 - colorSize, colorSize, colorSize, colorRadius, new Color(value.surface));

            if (value.outlineAlpha > 0) {
                this.roundedOutline(offsetX, posY + panelHeight - 16 - colorSize, colorSize, colorSize, colorRadius, 2, new Color(RenderSystem.reAlpha(ThemeManager.getAccentColor().getColor1().getRGB(), value.outlineAlpha), true));
            }

            if (ThemeManager.getTheme() == value) {
                value.outlineAlpha = Interpolations.interpBezier(value.outlineAlpha, 1, 0.2f);
            } else {
                value.outlineAlpha = Interpolations.interpBezier(value.outlineAlpha, 0, 0.2f);
            }

            boolean hovered = isHovered(mouseX, mouseY, offsetX, posY + panelHeight - 16 - colorSize, colorSize, colorSize);

            if (hovered && Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;
                ThemeManager.setTheme(value);
            }

            offsetX += 56;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.modeRenderer.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        this.modeRenderer.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.modeRenderer.mouseReleased(mouseX, mouseY, mouseButton);
    }
}
