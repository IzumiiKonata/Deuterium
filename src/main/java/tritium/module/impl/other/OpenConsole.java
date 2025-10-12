package tritium.module.impl.other;

import org.lwjglx.input.Keyboard;
import tritium.module.Module;
import tritium.screens.ConsoleScreen;

/**
 * @author IzumiiKonata
 * @since 2024/8/31 12:42
 */
public class OpenConsole extends Module {

    public OpenConsole() {

        super("OpenConsole", Category.OTHER);

        super.setKeyBind(Keyboard.KEY_BACKSLASH);
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
