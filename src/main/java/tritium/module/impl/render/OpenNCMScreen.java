package tritium.module.impl.render;

import org.lwjglx.input.Keyboard;
import tritium.Tritium;
import tritium.module.Module;
import tritium.screens.ClickGui;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.other.info.Version;

/**
 * @author IzumiiKonata
 * Date: 2025/10/16 19:48
 */
public class OpenNCMScreen extends Module {

    public OpenNCMScreen() {
        super("NCM Screen", Category.RENDER);
        super.setKeyBind(Keyboard.KEY_L);
    }

    @Override
    public void onEnable() {

        if (!(mc.currentScreen instanceof NCMScreen) && mc.thePlayer != null && mc.theWorld != null)
            mc.displayGuiScreen(NCMScreen.getInstance());

        this.toggle();

    }
}
