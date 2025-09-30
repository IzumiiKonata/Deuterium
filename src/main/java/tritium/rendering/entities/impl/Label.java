package tritium.rendering.entities.impl;

import lombok.Getter;
import lombok.Setter;
import tritium.utils.i18n.Localizable;
import tritium.rendering.entities.RenderableEntity;
import tritium.rendering.font.CFontRenderer;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
@Getter
@Setter
public class Label extends RenderableEntity {

    private int color;

    private CFontRenderer fr;
    private Localizable text;

    private boolean centered = false;

    public Label(CFontRenderer fr, String text, double x, double y, boolean centered) {
        this(fr, text, x, y);

        this.centered = centered;
    }

    public Label(CFontRenderer fr, String text, double x, double y) {
        super(x, y, fr.getStringWidth(text), fr.getHeight());

        this.fr = fr;
        this.text = Localizable.of(text);
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

        if (centered) {
            this.fr.drawCenteredString(text.get(), this.getX(), this.getY(), this.getColor());

        } else {
            this.fr.drawString(text.get(), this.getX(), this.getY(), this.getColor());
        }

        this.setBounds(fr.getStringWidth(text.get()), fr.getHeight());

    }

    public void setFr(CFontRenderer fr) {
        this.fr = fr;

        this.setBounds(fr.getStringWidth(text.get()), fr.getHeight());
    }
}
