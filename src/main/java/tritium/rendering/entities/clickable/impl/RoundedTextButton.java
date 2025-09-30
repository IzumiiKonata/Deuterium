package tritium.rendering.entities.clickable.impl;

import lombok.Getter;
import lombok.Setter;
import tritium.utils.i18n.Localizable;
import tritium.management.FontManager;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;
import tritium.rendering.entities.clickable.ClickableEntity;
import tritium.rendering.font.CFontRenderer;

import java.awt.*;
import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
@Setter
@Getter
public class RoundedTextButton extends ClickableEntity {

    private double radius;

    private Color color = Color.WHITE;
    private int textColor = -1;

    private Localizable text = Localizable.of("");

    private Animation yAnimation = new Animation(Easing.BEZIER, Duration.ofMillis(150));

    private Animation alphaAnim = new Animation(Easing.LINEAR, Duration.ofMillis(150));

    public RoundedTextButton(double x, double y, double width, double height, double radius, ClickHandler onClick, ClickHandler onRelease, ClickHandler onHold) {
        super(x, y, width, height, onClick, onRelease, onHold);

        this.radius = radius;
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

        double y = this.getY()/* + this.yAnimation.run(this.isInBounds(mouseX, mouseY) ? -1.5 : 0)*/;

        this.roundedRect(this.getX(), y, this.getWidth(), this.getHeight(), this.getRadius(), this.getColor());

        if (this.isInBounds(mouseX, mouseY)) {
            this.alphaAnim.run(100);
        } else {
            this.alphaAnim.run(0);
        }

        this.roundedRect(this.getX(), y, this.getWidth(), this.getHeight(), this.getRadius(), new Color(23, 23, 23, Math.min(this.getColor().getAlpha(), (int) alphaAnim.getValue())));


        CFontRenderer pf18 = FontManager.pf18;

        pf18.drawCenteredString(this.text.get(), this.getX() + this.getWidth() * 0.5, y + this.getHeight() * 0.5 - pf18.getHeight() * 0.5, this.getTextColor());

        BLOOM.add(() -> {
            this.roundedRect(this.getX(), y, this.getWidth(), this.getHeight(), this.getRadius(), new Color(0, 0, 0, (int) (this.getColor().getAlpha() * 0.6)));
        });
    }
}
