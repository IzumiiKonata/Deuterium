package tech.konata.phosphate.widget.impl;

import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.widget.Widget;
import tech.konata.phosphate.widget.impl.keystrokes.CPSUtils;

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
