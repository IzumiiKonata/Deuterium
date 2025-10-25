package tritium.bridge.settings;

import org.lwjglx.input.Keyboard;
import today.opai.api.interfaces.modules.values.BindValue;
import tritium.settings.BindSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:27
 */
public class BindValueWrapper extends ValueWrapper<Integer> implements BindValue {

    public BindValueWrapper(BindSetting setting) {
        super(setting);
    }

    @Override
    public String getKeyName() {
        return Keyboard.getKeyName(this.getValue());
    }

    @Override
    public boolean isPressed() {
        return Keyboard.isKeyDown(this.getValue());
    }
}
