package tech.konata.phosphate.screens.clickgui.panels;

import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.clickgui.Panel;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.SettingEntry;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries.CustomizationsEntry;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries.LanguageEntry;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries.ThemeColorEntry;
import tech.konata.phosphate.screens.clickgui.panels.settingspanel.entries.VideoSettingsEntry;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/10/26 15:23
 */
public class SettingsPanel extends Panel implements SharedRenderingConstants {

    public SettingsPanel() {
        super("Settings");
    }

    double yScrollEntryList = 0, yScrollSmoothEntryList = 0;


    List<SettingEntry> entries = Arrays.asList(
            new LanguageEntry(),
            new ThemeColorEntry(),
            new VideoSettingsEntry(),
            new CustomizationsEntry()
    );
    SettingEntry selectedEntry = entries.get(0);

    @Override
    public void onSwitchedTo() {
        if (!Phosphate.getInstance().isObfuscated()) {
            entries = Arrays.asList(
                    new LanguageEntry(),
                    new ThemeColorEntry(),
                    new VideoSettingsEntry(),
                    new CustomizationsEntry()
            );

            selectedEntry = entries.get(0);
        }
    }

    boolean lmbPressed = false;

    @Override
    public void draw(double mouseX, double mouseY, int dWheel) {

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        FontManager.pf25bold.drawString(this.getName().get(), posX + 4, posY + 4, ThemeManager.get(ThemeManager.ThemeColor.Text));

        posX += 4;

        this.renderList(mouseX, mouseY, dWheel);

        this.renderPanel(mouseX, mouseY, dWheel);
    }

    private void renderPanel(double mouseX, double mouseY, int dWheel) {
        double offsetY = posY + 28;

        double lHeight = height - 32;

        double selectedX = posX + 112, selectedY = offsetY, selectedWidth = width - 120, selectedHeight = lHeight;
        roundedRect(selectedX, selectedY, selectedWidth, selectedHeight, 8, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        double shrink = 8;

        this.selectedEntry.onRender(mouseX, mouseY, selectedX + shrink, selectedY + shrink, selectedWidth - shrink * 2, selectedHeight - shrink * 2, dWheel);
    }

    public double smoothSelectorY = 0;

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.selectedEntry.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.selectedEntry.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        this.selectedEntry.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    private void renderList(double mouseX, double mouseY, int dWheel) {
        double offsetY = posY + 28;

        double lHeight = height - 32;

        roundedRect(posX, offsetY, 108, lHeight, 8, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        Stencil.write();
        Rect.draw(posX, offsetY + 4, 108, height - 55, -1, Rect.RectType.EXPAND);
        Stencil.erase();

        double listOffsetX = posX + 4;
        double listOffsetY = offsetY + 5;
        double listWidth = 100, listHeight = 26;
        double listSpacing = 4.7;

        double yAdd = 5;

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            yAdd *= 2;

        if (RenderSystem.isHovered(mouseX, mouseY, posX, offsetY, 108, lHeight) && dWheel != 0) {
            if (dWheel > 0)
                yScrollSmoothEntryList -= yAdd;
            else
                yScrollSmoothEntryList += yAdd;
        }

        yScrollSmoothEntryList = Interpolations.interpBezier(yScrollSmoothEntryList, 0, 0.1f);
        double delta = yScrollEntryList;
        yScrollEntryList = Interpolations.interpBezier(yScrollEntryList, yScrollEntryList + yScrollSmoothEntryList, 1f);

        if (yScrollEntryList < 0)
            yScrollEntryList = Interpolations.interpBezier(yScrollEntryList, 0, 0.2f);

        double totalHeight = (listHeight + listSpacing) * (entries.size());

        double target = totalHeight - (lHeight) + listSpacing;

        if (totalHeight > lHeight) {

            if (yScrollEntryList > target) {
                yScrollEntryList = Interpolations.interpBezier(yScrollEntryList, target, 0.2f);
            }
        } else {
            yScrollEntryList = Interpolations.interpBezier(yScrollEntryList, 0, 0.2f);
        }

        delta = yScrollEntryList - delta;
        smoothSelectorY -= delta;

        listOffsetY -= yScrollEntryList;

        int currentListIndex = entries.indexOf(selectedEntry);

        if (smoothSelectorY == 0)
            smoothSelectorY = listOffsetY + currentListIndex * (listHeight + listSpacing);

        smoothSelectorY = Interpolations.interpBezier(smoothSelectorY, listOffsetY + currentListIndex * (listHeight + listSpacing), 0.4f);

        this.roundedRectAccentColor(listOffsetX, smoothSelectorY, listWidth, listHeight, 4);


        for (SettingEntry list : entries) {

            if (listOffsetY + listHeight < posY + 47) {
                listOffsetY += listHeight + listSpacing;
                continue;
            }

            if (listOffsetY > posY + 40 + height - 48) {
                break;
            }

            RenderValues renderValues = list.getRenderValues();

            boolean hovered = RenderSystem.isHovered(mouseX, mouseY, listOffsetX, listOffsetY, listWidth, listHeight);

            if (hovered && Mouse.isButtonDown(0) && !lmbPressed) {
                lmbPressed = true;
                selectedEntry = list;
            }

            if (hovered && selectedEntry != list) {
                renderValues.hoveredAlpha = Interpolations.interpBezier(renderValues.hoveredAlpha, 60 * RenderSystem.DIVIDE_BY_255, 0.2f);
            } else {
                renderValues.hoveredAlpha = Interpolations.interpBezier(renderValues.hoveredAlpha, 0, 0.3f);
            }

            roundedRect(listOffsetX, listOffsetY, listWidth, listHeight, 4, new Color(19, 27, 31, (int) (renderValues.hoveredAlpha * 255)));

            SVGImage.draw(list.getImgLocation(), listOffsetX + 3, listOffsetY + 3, 20, 20, ThemeManager.get(ThemeManager.ThemeColor.Text));

            String name = list.getName().get();

            list.getSt().render(FontManager.pf20bold, name, listOffsetX + 25, listOffsetY + listHeight * 0.5 - FontManager.pf20bold.getHeight() * 0.5, listWidth - 3 - 20 - 3, ThemeManager.get(ThemeManager.ThemeColor.Text));

//            FontManager.pf20bold.drawString(name, listOffsetX + 25, listOffsetY + listHeight * 0.5 - FontManager.pf20bold.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

            listOffsetY += listHeight + listSpacing;
        }

        Stencil.dispose();
    }

    public static class RenderValues {

        public float hoveredAlpha = 0;

    }


    @Override
    public void init() {

    }

}
