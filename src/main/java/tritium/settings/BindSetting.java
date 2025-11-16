package tritium.settings;

import tritium.bridge.settings.BindValueWrapper;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:26
 */
public class BindSetting extends Setting<Integer> {

    public BindSetting(String internalName, int keyCode) {
        super(internalName, keyCode);
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
