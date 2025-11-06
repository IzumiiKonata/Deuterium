package tritium.rendering.entities.clickable.impl;

import lombok.Setter;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.InterpolatableColor;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.clickable.ClickableEntity;
import tritium.rendering.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;
import tritium.utils.i18n.Localizable;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 10:44
 */
public class FlatMainMenuButton extends ClickableEntity {

    private final Localizable text;

    @Setter
    private int backgroundColor;

    private final InterpolatableColor textColor = new InterpolatableColor(getColor(ColorType.TEXT));

    private double animationsRectWidth = .0d;

    public FlatMainMenuButton(double x, double y, double width, double height, Localizable text, int backgroundColor, ClickHandler onClick) {
        super(x, y, width, height, onClick, (x1, y1, i) -> {}, (x1, y1, i) -> {});
        this.text = text;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void onRender(double mouseX, double mouseY) {
        Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), backgroundColor);

        boolean inBounds = this.isInBounds(mouseX, mouseY);
        this.textColor.interpolate(this.getColor(inBounds ? ColorType.TEXT : ColorType.UNFOCUSED), .15f);

        this.animationsRectWidth = Interpolations.interpBezier(this.animationsRectWidth, inBounds ? this.getWidth() * .5 : 0, .3f);

        CFontRenderer fr = FontManager.pf18;
        fr.drawCenteredString(
                text.get(),
                this.getX() + this.getWidth() / 2.0d,
                this.getY() + this.getHeight() / 2.0d - fr.getHeight() / 2.0d,
                this.textColor.getHexColor()
        );

        Rect.draw(
                this.getX() + this.getWidth() * .5 - this.animationsRectWidth,
                this.getY() + this.getHeight() - 1,
                this.animationsRectWidth * 2,
                1,
                0xff0090ff
        );
    }

    private int getColor(ColorType type) {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();

        if (type == ColorType.UNFOCUSED)
            return RenderSystem.hexColor(128, 128, 128);

        switch (theme) {
            case Dark:
                switch (type) {
                    case TEXT:
                        return RenderSystem.hexColor(255, 255, 255);
                }
                break;
            case Light:
                switch (type) {
                    case TEXT:
                        return RenderSystem.hexColor(0, 0, 0);
                }
        }

        return 0;
    }

    private enum ColorType {
        UNFOCUSED,
        TEXT
    }
}
