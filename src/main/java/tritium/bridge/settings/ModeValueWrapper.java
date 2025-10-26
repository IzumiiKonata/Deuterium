package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.ModeValue;
import tritium.settings.ModeSetting;
import tritium.settings.Setting;

import java.util.Arrays;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:39
 */
public class ModeValueWrapper extends ValueWrapper<String> implements ModeValue {

    public ModeValueWrapper(ModeSetting<?> setting) {
        super(setting);
    }

    @Override
    public String getValue() {
        return ((ModeSetting<?>) setting).getCurMode();
    }

    @Override
    public void setValue(String value) {
        for (Enum<?> constant : ((ModeSetting<?>) setting).getConstants()) {
            if (constant.toString().equalsIgnoreCase(value)) {
                ((ModeSetting<?>) setting).loadValue(value);
                break;
            }
        }
    }

    @Override
    public String[] getAllModes() {
        return Arrays.stream(((ModeSetting<?>) setting).getConstants()).map(Enum::name).toArray(String[]::new);
    }

    @Override
    public int getModeCount() {
        // TODO 这是当前选中的模式的 index 还是所有模式的数量?
        return ((ModeSetting<?>) setting).getConstants().length;
    }

    @Override
    public boolean isCurrentMode(String mode) {
        return ((ModeSetting<?>) setting).getValue().name().equals(mode);
    }
}
