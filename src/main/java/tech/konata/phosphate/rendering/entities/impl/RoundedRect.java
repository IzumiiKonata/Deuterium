package tech.konata.phosphate.rendering.entities.impl;

import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.rendering.entities.RenderableEntity;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
@Setter
@Getter
public class RoundedRect extends RenderableEntity {

    private int color;

    private double radius;

    public RoundedRect(double x, double y, double width, double height, double radius, int color) {
        super(x, y, width, height);

        this.setColor(color);
        this.setRadius(radius);
    }

    public void onRender(double mouseX, double mouseY) {

        this.roundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), radius, new Color(this.getColor(), true));

    }

    public enum RectType {
        EXPAND,
        ABSOLUTE_POSITION
    }

}