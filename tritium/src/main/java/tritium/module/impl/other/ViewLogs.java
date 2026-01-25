package tritium.module.impl.other;

import org.lwjgl.input.Keyboard;
import tritium.module.Module;
import tritium.screens.LogScreen;

/**
 * @author IzumiiKonata
 * @since 2024/8/31 12:42
 */
public class ViewLogs extends Module {

    public ViewLogs() {

        super("ViewLogs", Category.OTHER);

        super.setKeyBind(Keyboard.KEY_BACKSLASH);
        super.setShouldRender(() -> false);

    }

    @Override
    public void onEnable() {

        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.displayGuiScreen(LogScreen.getInstance());
        }

        this.toggle();

    }

}
