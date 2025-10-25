package tritium.bridge.settings;

import lombok.Getter;
import today.opai.api.interfaces.modules.Value;
import tritium.settings.Setting;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:26
 */
public class ValueWrapper<T> implements Value<T> {

    @Getter
    final Setting setting;

    public ValueWrapper(Setting<?> setting) {
        this.setting = setting;
    }

    @Override
    public String getDescription() {
        return this.setting.getDescription().get();
    }

    @Override
    public T getValue() {
        return (T) setting.getValue();
    }

    @Override
    public void setValue(T value) {
        setting.setValue(value);
    }

    @Override
    public String getName() {
        return setting.getInternalName();
    }

    @Override
    public void setHiddenPredicate(BooleanSupplier supplier) {
        setting.setShouldRender(() -> !supplier.getAsBoolean());
    }

    @Override
    public void setValueCallback(Consumer consumer) {
        setting.valueChangedCallback = consumer;
    }
}
