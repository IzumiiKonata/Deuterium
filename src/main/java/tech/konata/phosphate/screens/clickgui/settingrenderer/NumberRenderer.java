package tech.konata.phosphate.screens.clickgui.settingrenderer;

import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.SettingRenderer;
import tech.konata.phosphate.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2023/12/30
 */
public class NumberRenderer extends SettingRenderer<NumberSetting<?>> {

    public NumberRenderer(NumberSetting<?> settingIn) {
        super(settingIn);
    }

    double nowWidth = 0;

    @Override
    public double render(double mouseX, double mouseY, int dWheel) {
        CFontRenderer pf20 = FontManager.pf20;

        pf20.drawString(this.setting.getName().get(), x, y + (pf20.getHeight() + 8) * 0.5 - pf20.getHeight() * 0.5 - 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        double sliderWidth = width * 0.62, sliderHeight = 6, sliderX = x + width * 0.3, sliderY = y + (pf20.getHeight() + 8) * 0.5 - sliderHeight * 0.5, sliderRadius = 2;
        this.roundedRect(sliderX, sliderY, sliderWidth, sliderHeight, sliderRadius, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));

        nowWidth = Interpolations.interpBezier(nowWidth, sliderWidth * (setting.getValue().doubleValue() - setting.getMinimum().doubleValue()) / (setting.getMaximum().doubleValue() - setting.getMinimum().doubleValue()), 0.2);
        this.roundedRectAccentColor(sliderX, sliderY, nowWidth, sliderHeight, sliderRadius - 0.5);

        pf20.drawString(setting.getStringForRender(), x + width - pf20.getStringWidth(setting.getStringForRender()), sliderY - pf20.getHeight() * 0.5 + 2.5, ThemeManager.get(ThemeManager.ThemeColor.Text));

        return pf20.getHeight() + 8;
    }

    @Override
    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long time) {

        ClickGui.getInstance().modulesPanel.lmbPressed = true;

        double sliderWidth = width * 0.62, sliderHeight = 6, sliderX = x + width * 0.3, sliderY = y + (FontManager.pf20.getHeight() + 8) * 0.5 - sliderHeight * 0.5;

        if (isHovered(mouseX, mouseY, sliderX, sliderY - 2, sliderWidth, FontManager.pf20.getHeight()) && clickResistTimer.isDelayed(500)) {

            double render = setting.getMinimum().doubleValue();
            double max = setting.getMaximum().doubleValue();
            double inc = setting.getIncrement().doubleValue();
            double valAbs = mouseX - sliderX;
            double perc = valAbs / sliderWidth;
            perc = Math.min(Math.max(0.0D, perc), 1.0D);
            double valRel = (max - render) * perc;
            double val = render + valRel;
            val = (double) Math.round(val * (1.0D / inc)) / (1.0D / inc);

            setting.loadValue(String.valueOf(val));
        }

    }
}
