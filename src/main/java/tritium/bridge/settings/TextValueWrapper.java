package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.TextValue;
import tritium.settings.StringSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:23
 */
public class TextValueWrapper extends ValueWrapper<String> implements TextValue {

    public TextValueWrapper(StringSetting setting) {
        super(setting);
    }

}
