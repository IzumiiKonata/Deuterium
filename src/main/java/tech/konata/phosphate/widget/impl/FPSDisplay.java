package tech.konata.phosphate.widget.impl;

import net.minecraft.client.Minecraft;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.utils.timing.Timer;
import tech.konata.phosphate.widget.Widget;

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
