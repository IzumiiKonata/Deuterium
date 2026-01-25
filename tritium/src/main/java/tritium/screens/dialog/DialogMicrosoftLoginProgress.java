package tritium.screens.dialog;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;
import tritium.utils.i18n.Localizable;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.screens.altmanager.AltScreen;

public class DialogMicrosoftLoginProgress extends Dialog {

    public DialogMicrosoftLoginProgress() {

    }

    @Getter
    @Setter
    Localizable label = Localizable.of("altscreen.refresh.loggingin");

    @Override
    public void render(double mouseX, double mouseY) {

        double spacing = 4;

        String text = label.get();
        width = 32 + FontManager.pf40.getStringWidthD(text);
        height = 32 + FontManager.pf40.getStringHeight(text);

        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        FontManager.pf40.drawCenteredStringMultiLine(label.get(), RenderSystem.getWidth() * 0.5, y + 16, ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (this.alpha * 255)));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            AltScreen.getInstance().setDialog(null);
        }
    }
}
