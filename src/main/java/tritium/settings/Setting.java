package tritium.settings;

import lombok.Getter;
import lombok.Setter;
import tritium.bridge.settings.ValueWrapper;
import tritium.utils.i18n.Localizable;
import tritium.module.Module;
import tritium.widget.Widget;

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
    protected T value;

    @Getter
    @Setter
    private Supplier<Boolean> shouldRender = () -> true;

    @Getter
    protected ValueWrapper wrapper;

    public Consumer valueChangedCallback = val -> {};

    public Setting(String internalName, T value) {
        this.internalName = internalName;
        this.value = value;
        this.defaultValue = this.buildDefaultValue(value);

        String lowerCase = internalName.toLowerCase();

        this.name = Localizable.of("setting." + lowerCase + ".name");
        this.description = Localizable.of("setting." + lowerCase + ".desc");

        this.createValueWrapper();
    }

    protected void createValueWrapper() {
        this.wrapper = new ValueWrapper<>(this);
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

    public void setValue(T value) {
        this.value = value;
        this.valueChangedCallback.accept(value);
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
