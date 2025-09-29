package tech.konata.phosphate.screens.clickgui;

import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.screens.clickgui.settingrenderer.*;
import tech.konata.phosphate.settings.*;
import tech.konata.phosphate.utils.timing.Timer;

/**
 * @author IzumiiKonata
 * @since 2023/12/30
 */
public abstract class SettingRenderer<T extends Setting<?>> implements SharedRenderingConstants {

    public final T setting;

    public double x, y, width, height;

    public static Timer clickResistTimer = new Timer();

    public SettingRenderer(T settingIn) {
        this.setting = settingIn;
        clickResistTimer = new Timer();
        clickResistTimer.lastNs = 0;
    }

    public abstract double render(double mouseX, double mouseY, int dWheel);

    public static SettingRenderer<?> of(Setting<?> setting) {

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            return new BooleanRenderer(bs);
        }

        if (setting instanceof ModeSetting<?>) {
            ModeSetting<?> ms = (ModeSetting<?>) setting;
            return new ModeRenderer(ms);
        }

        if (setting instanceof NumberSetting<?>) {
            NumberSetting<?> ns = (NumberSetting<?>) setting;
            return new NumberRenderer(ns);
        }

        if (setting instanceof StringModeSetting) {
            StringModeSetting sms = (StringModeSetting) setting;
            return new StringModeRenderer(sms);
        }

        if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            return new ColorRenderer(cs);
        }

        if (setting instanceof StringSetting) {
            StringSetting s = (StringSetting) setting;
            return new StringRenderer(s);
        }

        return null;
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {

    }

    public void mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {

    }

    public void onKeyTyped(char typedChar, int keyCode) {

    }

}
