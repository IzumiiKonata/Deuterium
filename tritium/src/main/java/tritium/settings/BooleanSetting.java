package tritium.settings;

import tritium.bridge.settings.BooleanValueWrapper;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean>  {

    public BooleanSetting(String name, boolean enabled) {
        super(name, enabled);
    }

    public BooleanSetting(String name, boolean enabled, Supplier<Boolean> show) {
        super(name, enabled, show);
    }

    @Override
    protected void createValueWrapper() {
        this.wrapper = new BooleanValueWrapper(this);
    }

    @Override
    public void loadValue(String value) {
        this.setValue(Boolean.parseBoolean(value));
    }

    public void onToggle() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void toggle() {
        this.setValue(!this.getValue());
        this.onToggle();

        if (this.getValue()) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

}