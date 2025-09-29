package tech.konata.phosphate.screens.dialog.impl;

import lombok.Getter;
import lombok.Setter;
import org.lwjglx.input.Keyboard;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.altmanager.AltScreen;
import tech.konata.phosphate.screens.dialog.Dialog;

public class DialogMicrosoftLoginProgress extends Dialog {

    public DialogMicrosoftLoginProgress() {

    }

    @Getter
    @Setter
    Localizable label = Localizable.of("altscreen.refresh.loggingin");

    @Override
    public void render(double mouseX, double mouseY) {

        double spacing = 4;

        width = 32 + FontManager.pf40.getStringWidth(label.get());
        height = 32 + FontManager.pf40.getStringHeight(label.get());

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
