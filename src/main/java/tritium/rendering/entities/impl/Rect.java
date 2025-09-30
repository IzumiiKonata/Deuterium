package tritium.rendering.entities.impl;

import lombok.Getter;
import lombok.Setter;
import tritium.rendering.entities.RenderableEntity;
import tritium.rendering.rendersystem.RenderSystem;

/**
 * @author IzumiiKonata
 * @since 4/15/2023 8:54 PM
 */

@Setter
@Getter
public class Rect extends RenderableEntity {

    private int color;

    private RectType rectType;

    public Rect(double x, double y, double width, double height, int color, RectType type) {
        super(x, y, width, height);

        this.setColor(color);
        this.setRectType(type);
    }


    public static void draw(double x, double y, double x2, double y2, int color, RectType type) {
        if (type == RectType.EXPAND) {
            RenderSystem.drawRect(x, y, x + x2, y + y2, color);
        } else if (type == RectType.ABSOLUTE_POSITION) {
            RenderSystem.drawRect(x, y, x2, y2, color);
        }
    }

    public void onRender(double mouseX, double mouseY) {

        if (this.getRectType() == RectType.EXPAND) {
            RenderSystem.drawRect(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.getColor());
        } else if (this.getRectType() == RectType.ABSOLUTE_POSITION) {
            RenderSystem.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getColor());
        }

    }

    public enum RectType {
        EXPAND,
        ABSOLUTE_POSITION
    }

}
