package tech.konata.phosphate.screens.dialog;

import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.entities.RenderableEntity;
import tech.konata.phosphate.rendering.font.CFontRenderer;

public class DialogButton extends RenderableEntity {

    public final Localizable name;
    public final Runnable onClick;
    private final Dialog dialog;

    public DialogButton(Dialog instance, String name, Runnable onClick) {
        super(0, 0, 0, 0);
        this.dialog = instance;
        this.name = Localizable.of(name);
        this.onClick = onClick;
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

        roundedRectAccentColor(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 6, dialog.alpha);

        CFontRenderer fr = FontManager.pf20;

        fr.drawCenteredString(name.get(), this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5 - fr.getHeight() * 0.5, ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (dialog.alpha * 255)));

        double spacing = 4;

        this.setWidth(Math.max(120, spacing * 3 + fr.getStringWidth(name.get())));
        this.setHeight(spacing * 2 + fr.getHeight());

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (this.isInBounds(mouseX, mouseY)) {
            this.onClick.run();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
