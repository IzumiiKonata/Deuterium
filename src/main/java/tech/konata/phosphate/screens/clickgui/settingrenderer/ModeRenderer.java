package tech.konata.phosphate.screens.clickgui.settingrenderer;

import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.settings.ModeSetting;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 2023/12/30
 */
public class ModeRenderer extends SettingRenderer<ModeSetting<?>> {

    public ModeRenderer(ModeSetting<?> settingIn) {
        super(settingIn);
    }

    double expandedHeight = 0;
    boolean expanded = false;

    public boolean useAlternativeColor = false;

    @Override
    public double render(double mouseX, double mouseY, int dWheel) {
        CFontRenderer pf20 = FontManager.pf20;

        pf20.drawString(this.setting.getName().get(), x, y + (pf20.getHeight() + 8) * 0.5 - pf20.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        ThemeManager.ThemeColor color = ThemeManager.ThemeColor.Surface;

        if (useAlternativeColor) {
            color = ThemeManager.ThemeColor.OnSurface;
        }

        double cbWidth = 90, cbHeight = pf20.getHeight() + 8, cbX = x + width - cbWidth, cbY = y;
        this.roundedRect(cbX, cbY, cbWidth, expandedHeight, 3, ThemeManager.getAsColor(color));
        pf20.drawString(setting.getTranslation(setting.getValue()), cbX + 4, cbY + cbHeight * 0.5 - pf20.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double totalHeight = expanded ? cbHeight * (setting.getConstants().length + 1) : cbHeight;
        expandedHeight = Interpolations.interpBezier(expandedHeight, totalHeight, 0.2);

//        Rect.draw(((int) cbX) - 1, ((int) cbY) - 1, ((int) cbWidth) + 2, ((int) expandedHeight) + 2, 0xff0090ff, Rect.RectType.EXPAND);

        RenderSystem.doScissor(((int) cbX) - 1, ((int) cbY) - 1, ((int) cbWidth) + 2, ((int) expandedHeight) + 2);

        if (expanded || expandedHeight > cbHeight + 2) {

            RenderSystem.drawHorizontalLine(cbX + 4, cbY + cbHeight, cbX + cbWidth - 8, cbY + cbHeight, 1, Color.GRAY.getRGB());

            double startX = cbX + 4, startY = cbY + cbHeight;
            for (Enum<?> constant : setting.getConstants()) {

//                if (cbHeight * 0.5 + pf20.getHeight() * 0.5 < expandedHeight)
//                    break;

                pf20.drawString(setting.getTranslation(constant), startX, startY + cbHeight * 0.5 - pf20.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

                startY += cbHeight;
            }
        }

        RenderSystem.endScissor();

        return expandedHeight;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

        double cbWidth = 90, cbHeight = FontManager.pf20.getHeight() + 8, cbX = x + width - cbWidth, cbY = y;
        double totalHeight = expanded ? cbHeight * (setting.getConstants().length + 1) : cbHeight;

        if (RenderSystem.isHovered(mouseX, mouseY, cbX, cbY, cbWidth, cbHeight) && mouseButton == 0) {
            expanded = !expanded;
            clickResistTimer.reset();
        }

        if (!RenderSystem.isHovered(mouseX, mouseY, cbX, cbY, cbWidth, totalHeight) && mouseButton == 0) {
            expanded = false;
        }

        if (expanded || expandedHeight > cbHeight + 2) {

            double startX = cbX + 4, startY = cbY + cbHeight;

            for (Enum<?> constant : setting.getConstants()) {

                if (RenderSystem.isHovered(mouseX, mouseY, startX, startY, cbWidth - 8, cbHeight) && mouseButton == 0) {
                    setting.setMode(constant.name());
                    expanded = false;
                    clickResistTimer.reset();
                }

                startY += cbHeight;
            }
        }

    }
}
