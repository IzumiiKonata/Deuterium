package tech.konata.phosphate.settings;

import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.widget.Widget;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public abstract class Setting<T> {

    @Getter
    private final String internalName;

    @Getter
    @Setter
    private Localizable name, description;

    @Getter
    private final T defaultValue;

    @Getter
    @Setter
    protected T value;
    @Getter
    @Setter
    private Supplier<Boolean> shouldRender = () -> true;

    public Setting(String internalName, T value) {
        this.internalName = internalName;
        this.value = value;
        this.defaultValue = this.buildDefaultValue(value);

        String lowerCase = internalName.toLowerCase();

        this.name = Localizable.of("setting." + lowerCase + ".name");
        this.description = Localizable.of("setting." + lowerCase + ".desc");
    }

    public T buildDefaultValue(T value) {
        return value;
    }

    public Setting(String name, T value, Supplier<Boolean> shouldRender) {
        this(name, value);

        this.shouldRender = shouldRender;
    }

    public abstract void loadValue(String value);

    public boolean shouldRender() {
        return this.shouldRender.get();
    }

    public String getValueForConfig() {
        return this.getValue().toString();
    }

    public void reset() {
        this.setValue(this.buildDefaultValue(this.getDefaultValue()));
    }


    public void onInit(Module module) {

    }

    public void onInit(Widget widget) {

    }

    public void onInit() {

    }

}
