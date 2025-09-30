package tritium.widget.impl;

import tritium.settings.ModeSetting;
import tritium.widget.impl.keystrokes.CPSUtils;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 14:15
 */
public class CPSDisplay extends SimpleTextWidget {

    public CPSDisplay() {
        super("CPSDisplay");
    }

    public ModeSetting<Style> style = new ModeSetting<>("Style", Style.Full);

    public enum Style {
        Full,
        Simple
    }

    @Override
    public String getText() {
        String text = CPSUtils.left.get() + " CPS | " + CPSUtils.right.get() + " CPS";

        if (style.getValue() == Style.Simple) {
            text = CPSUtils.left.get() + " | " + CPSUtils.right.get();
        }

        return text;
    }
}
