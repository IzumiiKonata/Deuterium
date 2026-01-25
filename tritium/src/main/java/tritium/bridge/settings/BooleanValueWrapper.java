package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.BooleanValue;
import tritium.settings.BooleanSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:31
 */
public class BooleanValueWrapper extends ValueWrapper<Boolean> implements BooleanValue {

    public BooleanValueWrapper(BooleanSetting setting) {
        super(setting);
    }

}
