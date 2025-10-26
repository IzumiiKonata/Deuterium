package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.ModeValue;
import tritium.settings.StringModeSetting;
import tritium.settings.StringSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:12
 */
public class StringModeValueWrapper extends ValueWrapper<String> implements ModeValue {

    public StringModeValueWrapper(StringModeSetting setting) {
        super(setting);
    }

    @Override
    public String[] getAllModes() {
        return ((StringModeSetting) setting).getModes().toArray(new String[0]);
    }

    @Override
    public int getModeCount() {
        // TODO 这是当前选中的模式的 index 还是所有模式的数量?
        return ((StringModeSetting) setting).getModes().size();
    }

    @Override
    public boolean isCurrentMode(String mode) {
        return ((StringModeSetting) setting).getValue().equals(mode);
    }
}
