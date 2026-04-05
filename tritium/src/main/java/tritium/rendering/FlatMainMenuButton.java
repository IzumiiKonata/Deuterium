package tritium.rendering;

import lombok.Getter;
import lombok.Setter;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.ClientSettings;
import tritium.utils.i18n.Localizable;

import java.util.Objects;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 10:44
 */
public class FlatMainMenuButton {

    @Getter
    @Setter
    private double x, y, width, height;

    private final Localizable text;

    private final Runnable onClick;

    @Setter
    private int backgroundColor;

    private final InterpolatableColor textColor = new InterpolatableColor(getColor(ColorType.TEXT));

    private double animationsRectWidth = .0d;

    public FlatMainMenuButton(double x, double y, double width, double height, Localizable text, int backgroundColor, Runnable onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.backgroundColor = backgroundColor;
        this.onClick = onClick;
    }

    public void render(double mouseX, double mouseY) {
        Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), backgroundColor);

        boolean inBounds = this.isInBounds(mouseX, mouseY);
        this.textColor.interpolate(this.getColor(inBounds ? ColorType.TEXT : ColorType.UNFOCUSED), .15f);

        this.animationsRectWidth = Interpolations.interpolate(this.animationsRectWidth, inBounds ? this.getWidth() * .5 : 0, .3f);

        CFontRenderer fr = FontManager.pf18;
        fr.drawCenteredString(
                text.get(),
                this.getX() + this.getWidth() / 2.0d,
                this.getY() + this.getHeight() / 2.0d - fr.getFontHeight() / 2.0d,
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

    private boolean isInBounds(double mouseX, double mouseY) {
        return RenderSystem.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }

    private int getColor(ColorType type) {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();

        if (type == ColorType.UNFOCUSED)
            return RGBA.color(128, 128, 128);

        switch (theme) {
            case Dark:
                if (Objects.requireNonNull(type) == ColorType.TEXT) {
                    return RGBA.color(255, 255, 255);
                }
                break;
            case Light:
                if (Objects.requireNonNull(type) == ColorType.TEXT) {
                    return RGBA.color(0, 0, 0);
                }
        }

        return 0;
    }

    private enum ColorType {
        UNFOCUSED,
        TEXT
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.isInBounds(mouseX, mouseY) && mouseButton == 0)
            this.onClick.run();
    }
}
