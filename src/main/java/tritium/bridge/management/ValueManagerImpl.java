package tritium.bridge.management;

import lombok.Getter;
import today.opai.api.interfaces.managers.ValueManager;
import today.opai.api.interfaces.modules.values.*;
import tritium.rendering.HSBColor;
import tritium.settings.*;
import tritium.utils.i18n.Localizable;

import java.awt.*;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:28
 */
public class ValueManagerImpl implements ValueManager {

    @Getter
    private static final ValueManagerImpl instance = new ValueManagerImpl();

    @Override
    public BooleanValue createBoolean(String name, boolean checked) {
        BooleanSetting booleanSetting = new BooleanSetting(name, checked);

        booleanSetting.setName(Localizable.ofUntranslatable(name));

        return (BooleanValue) booleanSetting.getWrapper();
    }

    @Override
    public LabelValue createLabel(String name) {
        LabelSetting labelSetting = new LabelSetting(name);

        labelSetting.setName(Localizable.ofUntranslatable(name));

        return (LabelValue) labelSetting.getWrapper();
    }

    @Override
    public NumberValue createDouble(String name, double value, double minimum, double maximum, double step) {
        NumberSetting<Double> doubleNumberSetting = new NumberSetting<>(name, value, minimum, maximum, step);

        doubleNumberSetting.setName(Localizable.ofUntranslatable(name));

        return (NumberValue) doubleNumberSetting.getWrapper();
    }

    @Override
    public ModeValue createModes(String name, String currentMode, String[] modes) {

        StringModeSetting stringModeSetting = new StringModeSetting(name, currentMode, modes);

        stringModeSetting.setName(Localizable.ofUntranslatable(name));

        return (ModeValue) stringModeSetting.getWrapper();
    }

    @Override
    public ColorValue createColor(String name, Color color) {

        ColorSetting colorSetting = new ColorSetting(name, new HSBColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));

        colorSetting.setName(Localizable.ofUntranslatable(name));

        return (ColorValue) colorSetting.getWrapper();
    }

    @Override
    public BindValue createKeyBind(String name, int key) {
        BindSetting keyBindSetting = new BindSetting(name, key);
        keyBindSetting.setName(Localizable.ofUntranslatable(name));
        return (BindValue) keyBindSetting.getWrapper();
    }

    @Override
    public TextValue createInput(String name, String string) {
        StringSetting textSetting = new StringSetting(name, string);
        textSetting.setName(Localizable.ofUntranslatable(name));
        return (TextValue) textSetting.getWrapper();
    }
}
