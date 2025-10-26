package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.ColorValue;
import tritium.rendering.HSBColor;
import tritium.settings.ColorSetting;
import tritium.settings.Setting;

import java.awt.*;
import java.util.function.Consumer;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:32
 */
public class ColorValueWrapper extends ValueWrapper<Color> implements ColorValue {

    public ColorValueWrapper(ColorSetting setting) {
        super(setting);
    }

    @Override
    public boolean isAlphaAllowed() {
        return true;
    }

    @Override
    public ColorValue setAlphaAllowed(boolean value) {
        return this;
    }

    @Override
    public Color getValue() {
        return ((HSBColor) super.getValue()).getColor();
    }

    @Override
    public void setValue(Color value) {
        float[] hsb = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        this.setting.loadValue(hsb[0] + ":" + hsb[1] + ":" + hsb[2] + ":" + value.getAlpha());
    }
}
