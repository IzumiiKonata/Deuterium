package tech.konata.phosphate.settings;

import lombok.NonNull;
import tech.konata.phosphate.utils.i18n.Localizable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModeSetting<T extends Enum<T>> extends Setting<T> {
    private final T[] constants;

    public ModeSetting(String name, T value) {
        super(name, value);
        this.constants = extractConstantsFromEnumValue(value);

        for (Enum<?> constant : constants) {
            translationMap.put(constant, Localizable.of("mode." + constant.name() + ".name"));
        }
    }

    public ModeSetting(String name, T value, Supplier<Boolean> show) {
        super(name, value, show);
        this.constants = extractConstantsFromEnumValue(value);

        for (Enum<?> constant : constants) {
            translationMap.put(constant, Localizable.of("mode." + constant.name() + ".name"));
        }
    }

    final Map<Enum<?>, Localizable> translationMap = new HashMap<>();

    public String getTranslation(Enum<?> mode) {
        return translationMap.get(mode).get();
    }

    public T[] extractConstantsFromEnumValue(T value) {
        return value.getDeclaringClass().getEnumConstants();
    }

    public String getCurMode() {
        return this.getValue().toString();
    }

    public T[] getConstants() {
        return this.constants;
    }

    public void setMode(String mode) {
        for (T constant : this.getConstants()) {
            if (constant.toString().equalsIgnoreCase(mode)) {
                T before = this.getValue();
                this.setValue(constant);
                this.onModeChanged(before, constant);
            }
        }
    }

    public void onModeChanged(T before, T now) {

    }

    @Override
    public void setValue(@NonNull T value) {
        T before = this.getValue();
        super.setValue(value);
        this.onModeChanged(before, value);
    }

    @Override
    public void loadValue(String string) {
        this.setMode(string);
    }

}
