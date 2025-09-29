package tech.konata.phosphate.screens.dialog.impl;

import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.dialog.Dialog;
import tech.konata.phosphate.screens.dialog.DialogButton;

public class DialogHowToMoveWidgets extends Dialog {

    public DialogHowToMoveWidgets() {
        super.addEntity(confirm);
    }

    DialogButton confirm = new DialogButton(this, "dialog.howtomovewidgets.confirm.name", this::close);

    Localizable lConfirmLogout = Localizable.of("dialog.howtomovewidgets.text");

    @Override
    public void render(double mouseX, double mouseY) {
        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        double spacing = 4;
        double offsetY = 60;

        double minWidth = 80;

        CFontRenderer fr = FontManager.pf40;

        fr.drawCenteredString(lConfirmLogout.get(), x + width * 0.5, y + height * 0.5 - fr.getHeight(), ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (this.alpha * 255)));

//        cancel.setWidth(Math.max(minWidth, cancel.getWidth()));
//        confirm.setWidth(Math.max(minWidth, confirm.getWidth()));

        confirm.setPosition(spacing * 2 + confirm.getWidth(), offsetY);

    }
}
