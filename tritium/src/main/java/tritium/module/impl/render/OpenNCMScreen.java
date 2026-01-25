package tritium.module.impl.render;

import tritium.module.Module;
import tritium.screens.ncm.NCMScreen;

/**
 * @author IzumiiKonata
 * Date: 2025/10/16 19:48
 */
public class OpenNCMScreen extends Module {

    public OpenNCMScreen() {
        super("NCM Screen", Category.RENDER);
    }

    @Override
    public void onEnable() {

        if (!(mc.currentScreen instanceof NCMScreen) && mc.thePlayer != null && mc.theWorld != null)
            mc.displayGuiScreen(NCMScreen.getInstance());

        this.toggle();

    }
}
