package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.LabelValue;
import tritium.settings.LabelSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:15
 */
public class LabelValueWrapper extends ValueWrapper<Void> implements LabelValue {

    public LabelValueWrapper(LabelSetting setting) {
        super(setting);
    }

}
