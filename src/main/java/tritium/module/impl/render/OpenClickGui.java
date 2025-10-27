package tritium.module.impl.render;

import org.lwjgl.input.Keyboard;
import tritium.module.Module;
import tritium.screens.ClickGui;
import tritium.screens.nsf.NSFScreen;

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

        if (!(mc.currentScreen instanceof ClickGui) && mc.thePlayer != null && mc.theWorld != null) {

            if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                mc.displayGuiScreen(NSFScreen.getInstance());
            } else
                mc.displayGuiScreen(ClickGui.getInstance());

        }

        this.toggle();

    }
}
