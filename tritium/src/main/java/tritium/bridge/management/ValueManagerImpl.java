package tritium.bridge.management;

import lombok.Getter;
import today.opai.api.interfaces.managers.ValueManager;
import today.opai.api.interfaces.modules.values.*;
import tritium.bridge.settings.ValueWrapper;
import tritium.rendering.HSBColor;
import tritium.settings.*;
import tritium.utils.i18n.Localizable;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:28
 */
public class ValueManagerImpl implements ValueManager {

    @Getter
    private static final ValueManagerImpl instance = new ValueManagerImpl();

    @Getter
    private static final Map<ValueWrapper<?>, Setting<?>> wrapperToSettingMap = new ConcurrentHashMap<>();

    @Override
    public BooleanValue createBoolean(String name, boolean checked) {
        BooleanSetting booleanSetting = new BooleanSetting(name, checked);

        booleanSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(booleanSetting.getWrapper(), booleanSetting);

        return (BooleanValue) booleanSetting.getWrapper();
    }

    @Override
    public LabelValue createLabel(String name) {
        LabelSetting labelSetting = new LabelSetting(name);

        labelSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(labelSetting.getWrapper(), labelSetting);

        return (LabelValue) labelSetting.getWrapper();
    }

    @Override
    public NumberValue createDouble(String name, double value, double minimum, double maximum, double step) {
        NumberSetting<Double> doubleNumberSetting = new NumberSetting<>(name, value, minimum, maximum, step);

        doubleNumberSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(doubleNumberSetting.getWrapper(), doubleNumberSetting);

        return (NumberValue) doubleNumberSetting.getWrapper();
    }

    @Override
    public ModeValue createModes(String name, String currentMode, String[] modes) {

        StringModeSetting stringModeSetting = new StringModeSetting(name, currentMode, modes);

        stringModeSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(stringModeSetting.getWrapper(), stringModeSetting);

        return (ModeValue) stringModeSetting.getWrapper();
    }

    @Override
    public ColorValue createColor(String name, Color color) {

        ColorSetting colorSetting = new ColorSetting(name, new HSBColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));

        colorSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(colorSetting.getWrapper(), colorSetting);

        return (ColorValue) colorSetting.getWrapper();
    }

    @Override
    public BindValue createKeyBind(String name, int key) {
        BindSetting keyBindSetting = new BindSetting(name, key);
        keyBindSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(keyBindSetting.getWrapper(), keyBindSetting);

        return (BindValue) keyBindSetting.getWrapper();
    }

    @Override
    public TextValue createInput(String name, String string) {
        StringSetting textSetting = new StringSetting(name, string);
        textSetting.setName(Localizable.ofUntranslatable(name));

        wrapperToSettingMap.put(textSetting.getWrapper(), textSetting);

        return (TextValue) textSetting.getWrapper();
    }
}
