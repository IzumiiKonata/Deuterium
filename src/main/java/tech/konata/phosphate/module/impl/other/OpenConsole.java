package tech.konata.phosphate.module.impl.other;

import org.lwjglx.input.Keyboard;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.screens.ConsoleScreen;

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

        if (mc.thePlayer != null) {
            mc.displayGuiScreen(ConsoleScreen.getInstance());
        }

        this.toggle();

    }

}
