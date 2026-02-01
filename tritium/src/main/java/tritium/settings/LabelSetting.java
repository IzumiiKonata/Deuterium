package tritium.settings;

import tritium.bridge.settings.LabelValueWrapper;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:16
 */
public class LabelSetting extends Setting<Void> {

    public LabelSetting(String internalName) {
        super(internalName, null);
    }

    @Override
    protected void createValueWrapper() {
        this.wrapper = new LabelValueWrapper(this);
    }

    @Override
    public void loadValue(String value) {
        // does nothing
    }
}
