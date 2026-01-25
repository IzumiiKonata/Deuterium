package tritium.widget.impl;

import tritium.settings.BooleanSetting;
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

    public BooleanSetting showRmb = new BooleanSetting("Show RMB", false);

    @Override
    public String getText() {
        String text = CPSUtils.left.get() + " CPS";

        if (showRmb.getValue()) {
            text += " | " + CPSUtils.right.get() + " CPS";
        }

        if (style.getValue() == Style.Simple) {
            text = String.valueOf(CPSUtils.left.get());

            if (showRmb.getValue()) {
                text += " | " + CPSUtils.right.get();
            }
        }

        return text;
    }
}
