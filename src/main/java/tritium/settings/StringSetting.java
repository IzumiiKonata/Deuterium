package tritium.settings;

import tritium.bridge.settings.TextValueWrapper;

import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 20:49
 */
public class StringSetting extends Setting<String> {

    public StringSetting(String internalName, String value) {
        super(internalName, value);
    }

    public StringSetting(String name, String value, Supplier<Boolean> shouldRender) {
        super(name, value, shouldRender);
    }

    @Override
    protected void createValueWrapper() {
        this.wrapper = new TextValueWrapper(this);
    }

    public boolean onValueChanged(String before, String after) {
        return true;
    }

    @Override
    public void loadValue(String value) {
        boolean shouldChange = this.onValueChanged(this.getValue(), value);

        if (shouldChange) {
            this.setValue(value);
        }
    }
}
