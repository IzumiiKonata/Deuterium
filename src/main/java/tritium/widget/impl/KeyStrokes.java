package tritium.widget.impl;

import tritium.settings.NumberSetting;
import tritium.widget.Widget;
import tritium.widget.impl.keystrokes.Key;

/**
 * @author IzumiiKonata
 * @since 6/19/2023 11:05 AM
 */
public class KeyStrokes extends Widget {
    public Key[] keys;
    double spacing = 2;
    double keyWidth = 22;
    double keyHeight = 22;

    double spaceWidth = (keyWidth + spacing) * 2 + keyWidth;
    double spaceHeight = keyHeight / 2.0;


    double mouseWidth = keyWidth + spacing + keyWidth / 2.0 - spacing / 2.0;
    double mouseHeight = keyHeight;

    public NumberSetting<Double> size = new NumberSetting<Double>("Size", 22d, 20d, 32d, 1d) {
        @Override
        public void onValueChanged(Double last, Double now) {
            keyWidth = keyHeight = now;
            spaceWidth = (keyWidth + spacing) * 2 + keyWidth;
            spaceHeight = keyHeight / 2.0;


            mouseWidth = keyWidth + spacing + keyWidth / 2.0 - spacing / 2.0;
            mouseHeight = keyHeight;
            initKeys();
        }
    };

    public KeyStrokes() {
        super("Key Strokes");

        this.initKeys();
    }

    private void initKeys() {
        keys = new Key[7];

        keys[0] = new Key(mc.gameSettings.keyBindForward, keyWidth + spacing, 0, keyWidth, keyHeight);
        keys[1] = new Key(mc.gameSettings.keyBindLeft, 0, keyHeight + spacing, keyWidth, keyHeight);
        keys[2] = new Key(mc.gameSettings.keyBindBack, keyWidth + spacing, keyHeight + spacing, keyWidth, keyHeight);
        keys[3] = new Key(mc.gameSettings.keyBindRight, (keyWidth + spacing) * 2, keyHeight + spacing, keyWidth, keyHeight);
        keys[4] = new Key(mc.gameSettings.keyBindJump, 0, (keyHeight + spacing) * 2, spaceWidth, spaceHeight);
        keys[5] = new Key(mc.gameSettings.keyBindAttack, 0, (keyHeight + spacing) * 2 + spaceHeight + spacing, mouseWidth, mouseHeight);
        keys[6] = new Key(mc.gameSettings.keyBindUseItem, mouseWidth + spacing, (keyHeight + spacing) * 2 + spaceHeight + spacing, mouseWidth, mouseHeight);

    }


    @Override
    public void onRender(boolean editing) {
        for (Key key : this.keys) {
            key.render(this.getX(), this.getY());
        }


        this.setWidth((keyWidth + spacing) * 2 + keyWidth);
        this.setHeight((keyHeight + spacing) * 2 + spaceHeight + spacing + mouseHeight);
    }
}
