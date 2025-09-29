package tech.konata.phosphate.screens.dialog.impl.music;

import tech.konata.ncm.OptionsUtil;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.clickgui.panels.MusicPanel;
import tech.konata.phosphate.screens.dialog.Dialog;
import tech.konata.phosphate.screens.dialog.DialogButton;

public class DialogConfirmLogout extends Dialog {

    public DialogConfirmLogout() {
        super.addEntity(confirm);
        super.addEntity(cancel);

    }

    DialogButton confirm = new DialogButton(this, "dialog.confirmlogout.confirm.name", () -> {

        MusicPanel.playLists = null;
        MusicPanel.profile = null;
        MusicPanel.likeList = null;
        MusicPanel.selectedList = null;

        OptionsUtil.setCookie("");

        this.close();

    });

    DialogButton cancel = new DialogButton(this, "dialog.confirmlogout.cancel.name", this::close);

    Localizable lConfirmLogout = Localizable.of("dialog.confirmlogout.text");

    @Override
    public void render(double mouseX, double mouseY) {
        double x = RenderSystem.getWidth() * 0.5 - width * 0.5,
                y = RenderSystem.getHeight() * 0.5 - height * 0.5;

        double spacing = 4;
        double offsetY = 60;

        double minWidth = 80;

        CFontRenderer fr = FontManager.pf40;

        fr.drawCenteredString(lConfirmLogout.get(), x + width * 0.5, y + height * 0.5 - fr.getHeight(), ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (this.alpha * 255)));

        cancel.setPosition(spacing, offsetY);
        confirm.setPosition(spacing * 2 + confirm.getWidth(), offsetY);

    }
}
