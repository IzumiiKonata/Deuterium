package tech.konata.phosphate.settings;

import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringModeSetting extends Setting<String> {

    @Getter
    private final List<String> modes;

    public StringModeSetting(String name, String value, String... modes) {
        super(name, value);

        if (!this.sanityCheck(value, modes)) {
            throw new IllegalArgumentException("There's no \"" + value + "\" in " + Arrays.toString(modes) + "!");
        }

        this.modes = new ArrayList<>(Arrays.asList(modes.clone()));
    }

    public StringModeSetting(String name, String value, Supplier<Boolean> show, String... modes) {
        this(name, value, modes);
        this.setShouldRender(show);
    }

    public StringModeSetting(String name, String value, Collection<String> modes) {
        this(name, value, modes.toArray(new String[0]));
    }

    public StringModeSetting(String name, String value, Collection<String> modes, Supplier<Boolean> show) {
        this(name, value, show, modes.toArray(new String[0]));
    }

    public String getNameForRender(String modeIn) {
        return modeIn;
    }

    private boolean sanityCheck(String value, String... modes) {

        for (String mode : modes) {
            if (value.equals(mode))
                return true;
        }

        return false;
    }

    public void setMode(String mode) {
        for (String m : this.modes) {
            if (m.equalsIgnoreCase(mode)) {
                this.onModeChanged(this.getValue(), m);
                this.setValue(m);
            }
        }
    }

    public void onModeChanged(String before, String now) {

    }

    @Override
    public void setValue(@NonNull String value) {
        this.onModeChanged(this.getValue(), value);
        super.setValue(value);
    }

    @Override
    public void loadValue(String value) {
        this.setMode(value);
    }

}
