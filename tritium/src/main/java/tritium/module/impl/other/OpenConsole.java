package tritium.module.impl.other;

import org.lwjgl.input.Keyboard;
import tritium.module.Module;
import tritium.screens.ConsoleScreen;

/**
 * @author IzumiiKonata
 * Date: 2025/11/11 21:28
 */
public class OpenConsole extends Module {

    public OpenConsole() {
        super("OpenConsole", Category.OTHER);
        super.setKeyBind(Keyboard.KEY_GRAVE);
        super.setShouldRender(() -> false);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.displayGuiScreen(ConsoleScreen.getInstance());
        }

        this.toggle();
    }

}
