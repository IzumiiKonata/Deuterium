package tritium.widget.impl;

import net.minecraft.client.Minecraft;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 14:15
 */
public class FPSDisplay extends SimpleTextWidget {

    public FPSDisplay() {
        super("FPSDisplay");
    }

    @Override
    public String getText() {
        return Minecraft.getDebugFPS() + " FPS";
    }
}
