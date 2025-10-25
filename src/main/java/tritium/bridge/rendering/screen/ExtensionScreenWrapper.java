package tritium.bridge.rendering.screen;

import net.minecraft.client.gui.GuiScreen;
import today.opai.api.features.ExtensionScreen;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 18:56
 */
public class ExtensionScreenWrapper extends GuiScreen {

    private final ExtensionScreen extensionScreen;

    public ExtensionScreenWrapper(ExtensionScreen extensionScreen) {
        this.extensionScreen = extensionScreen;
    }

    public void drawScreen(int mouseX, int mouseY, float p) {
        extensionScreen.drawScreen(mouseX, mouseY);
    }

    public void initGui() {
        extensionScreen.initGui();
    }

    public void updateScreen() {
        extensionScreen.updateScreen();
    }

    public void onGuiClosed() {
        extensionScreen.onGuiClosed();
    }

    public void keyTyped(char chr, int key) {
        extensionScreen.keyTyped(chr, key);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        extensionScreen.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        extensionScreen.mouseMovedOrUp(mouseX, mouseY, button);
    }

}
