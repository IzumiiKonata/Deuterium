package tritium.bridge.settings;

import today.opai.api.interfaces.modules.values.NumberValue;
import tritium.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:09
 */
public class NumberValueWrapper extends ValueWrapper<Double> implements NumberValue {

    public NumberValueWrapper(NumberSetting<?> setting) {
        super(setting);
    }

    @Override
    public double getMaximum() {
        return ((NumberSetting<?>) setting).getMaximum().doubleValue();
    }

    @Override
    public double getMinimum() {
        return ((NumberSetting<?>) setting).getMinimum().doubleValue();
    }

    @Override
    public double getStep() {
        return ((NumberSetting<?>) setting).getStep().doubleValue();
    }

    @Override
    public NumberValue setSuffix(String s) {
        // TODO
        return this;
    }

    @Override
    public void setValue(Double value) {
        setting.loadValue(String.valueOf(value));
    }
}
