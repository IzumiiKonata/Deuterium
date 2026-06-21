package tritium.module.impl.render;

import tritium.module.Module;
import tritium.screens.nsf.NSFPlayerScreen;

public class OpenNSFScreen extends Module {

    public OpenNSFScreen() {
        super("NSF Screen", Category.RENDER);
    }

    @Override
    public void onEnable() {

        if (!(mc.currentScreen instanceof NSFPlayerScreen) && mc.thePlayer != null && mc.theWorld != null)
            mc.displayGuiScreen(new NSFPlayerScreen());

        this.toggle();

    }
}
