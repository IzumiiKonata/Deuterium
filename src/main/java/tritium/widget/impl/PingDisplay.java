package tritium.widget.impl;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 14:15
 */
public class PingDisplay extends SimpleTextWidget {

    public PingDisplay() {
        super("PingDisplay");
    }


    @Override
    public String getText() {
        String text = "SinglePlayer";

        if (mc.getCurrentServerData() != null) {
            text = mc.getCurrentServerData().pingToServer + "ms";
        }

        return text;
    }
}
