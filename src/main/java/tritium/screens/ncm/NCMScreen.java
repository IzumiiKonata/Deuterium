package tritium.screens.ncm;

import lombok.Getter;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tritium.rendering.ui.container.Panel;
import tritium.screens.BaseScreen;
import tritium.screens.ncm.panels.HomePanel;

/**
 * @author IzumiiKonata
 * Date: 2025/10/16 19:47
 */
public class NCMScreen extends BaseScreen {

    @Getter
    private static NCMScreen instance = new NCMScreen();

    HomePanel homePanel = new HomePanel();

    @Override
    public void initGui() {
        homePanel.onInit();
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        int dWheel = Mouse.getDWheel2();
        this.homePanel.renderWidget(mouseX, mouseY, dWheel);
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(null);
    }

    public enum ColorType {

        GENERIC_BACKGROUND;

    }

    public int getColor(ColorType type) {

        switch (type) {
            case GENERIC_BACKGROUND:
                return 0x0E0D11;
        }

        return 0;
    }
}
