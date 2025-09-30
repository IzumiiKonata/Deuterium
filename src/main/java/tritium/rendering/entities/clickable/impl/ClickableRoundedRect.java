package tritium.rendering.entities.clickable.impl;

import lombok.Getter;
import lombok.Setter;
import tritium.rendering.entities.clickable.ClickableEntity;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
@Setter
@Getter
public class ClickableRoundedRect extends ClickableEntity {

    private int color;

    private RectType rectType;

    private double radius;

    public ClickableRoundedRect(double x, double y, double width, double height, double radius, int color, RectType type, ClickHandler onClick, ClickHandler onRelease, ClickHandler onHold) {
        super(x, y, width, height, onClick, onRelease, onHold);

        this.setColor(color);
        this.setRectType(type);
        this.setRadius(radius);
    }

    public void onRender(double mouseX, double mouseY) {

        if (this.getRectType() == RectType.EXPAND) {
            this.roundedRect(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), radius, new Color(this.getColor(), true));
        } else if (this.getRectType() == RectType.ABSOLUTE_POSITION) {
            this.roundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), radius, new Color(this.getColor(), true));
        }

    }

    public enum RectType {
        EXPAND,
        ABSOLUTE_POSITION
    }

}