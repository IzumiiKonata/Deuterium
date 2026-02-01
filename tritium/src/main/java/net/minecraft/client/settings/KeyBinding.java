package net.minecraft.client.settings;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Setter;
import net.minecraft.client.resources.I18n;

import java.util.List;
import java.util.Set;

public class KeyBinding implements Comparable<KeyBinding> {
    private static final List<KeyBinding> keybindArray = Lists.newArrayList();
    private static final Int2ObjectMap<KeyBinding> hash = new Int2ObjectOpenHashMap<>();
    private static final Set<String> keybindSet = Sets.newHashSet();
    private final String keyDescription;
    private final int keyCodeDefault;
    private final String keyCategory;
    private int keyCode;

    /**
     * Is the key held down?
     */
    @Setter
    public boolean pressed;
    public boolean pressedOverride = false;
    private int pressTime;

    public static void onTick(int keyCode) {
        if (keyCode != 0) {
            KeyBinding keybinding = hash.get(keyCode);

            if (keybinding != null) {
                ++keybinding.pressTime;
            }
        }
    }

    public static void setKeyBindState(int keyCode, boolean pressed) {
        if (keyCode != 0) {
            KeyBinding keybinding = hash.get(keyCode);

            if (keybinding != null) {
                keybinding.pressed = pressed;
            }
        }
    }

    public static void unPressAllKeys() {
        for (KeyBinding keybinding : keybindArray) {
            keybinding.unpressKey();
        }
    }

    public static void resetKeyBindingArrayAndHash() {
        hash.clear();

        for (KeyBinding keybinding : keybindArray) {
            hash.put(keybinding.keyCode, keybinding);
        }
    }

    public static Set<String> getKeybinds() {
        return keybindSet;
    }

    public KeyBinding(String description, int keyCode, String category) {
        this.keyDescription = description;
        this.keyCode = keyCode;
        this.keyCodeDefault = keyCode;
        this.keyCategory = category;
        keybindArray.add(this);
        hash.put(keyCode, this);
        keybindSet.add(category);
    }

    /**
     * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
     */
    public boolean isKeyDown() {
        return this.pressedOverride || this.pressed;
    }

    public String getKeyCategory() {
        return this.keyCategory;
    }

    /**
     * Returns true on the initial key press. For continuous querying use {@link isKeyDown()}. Should be used in key
     * events.
     */
    public boolean isPressed() {
        if (this.pressTime == 0) {
            return false;
        } else {
            --this.pressTime;
            return true;
        }
    }

    public void setZero() {
        this.pressTime = 0;
    }

    public void unpressKey() {
        this.pressTime = 0;
        this.pressed = false;
    }

    public String getKeyDescription() {
        return this.keyDescription;
    }

    public int getKeyCodeDefault() {
        return this.keyCodeDefault;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int compareTo(KeyBinding p_compareTo_1_) {
        int i = I18n.format(this.keyCategory).compareTo(I18n.format(p_compareTo_1_.keyCategory));

        if (i == 0) {
            i = I18n.format(this.keyDescription).compareTo(I18n.format(p_compareTo_1_.keyDescription));
        }

        return i;
    }


}
