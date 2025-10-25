package tritium.settings;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:26
 */
public class BindSetting extends Setting<Integer> {

    public BindSetting(String internalName, int keyCode) {
        super(internalName, keyCode);
    }

    @Override
    public void loadValue(String value) {
        this.setValue(Integer.parseInt(value));
    }

}
