package tritium.settings;

import tritium.bridge.settings.BindValueWrapper;

import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:26
 */
public class BindSetting extends Setting<Integer> {

    public BindSetting(String internalName, int keyCode) {
        super(internalName, keyCode);
    }

    public BindSetting(String internalName, int keyCode, Supplier<Boolean> shouldRender) {
        super(internalName, keyCode);
        this.setShouldRender(shouldRender);
    }

    @Override
    protected void createValueWrapper() {
        this.wrapper = new BindValueWrapper(this);
    }

    @Override
    public void loadValue(String value) {
        this.setValue(Integer.parseInt(value));
    }

}
