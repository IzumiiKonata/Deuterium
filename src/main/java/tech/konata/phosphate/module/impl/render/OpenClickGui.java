package tech.konata.phosphate.module.impl.render;

import org.lwjglx.input.Keyboard;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.screens.ClickGui;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
public class OpenClickGui extends Module {

    public OpenClickGui() {
        super("ClickGui", Category.RENDER);
        super.setKeyBind(Keyboard.KEY_RSHIFT);
        super.setShouldRender(() -> false);
    }

    @Override
    public void onEnable() {

        if (mc.thePlayer != null && mc.currentScreen != ClickGui.getInstance()) {
            mc.displayGuiScreen(ClickGui.getInstance());
        }

        this.toggle();

    }
}
