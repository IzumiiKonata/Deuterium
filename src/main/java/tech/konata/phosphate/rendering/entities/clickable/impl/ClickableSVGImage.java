package tech.konata.phosphate.rendering.entities.clickable.impl;


import net.minecraft.util.Location;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;

public class ClickableSVGImage extends ClickEntity {

    private Location img;

    public ClickableSVGImage(Location image, double x, double y, double x1, double y1,
                             Runnable click, Runnable hold, Runnable focus, Runnable release, Runnable onBlur) {
        super(x, y, x1, y1, MouseBounds.CallType.Expand, click, hold, focus, release, onBlur);
        this.img = image;
    }

    public void draw() {
        SVGImage.draw(img, this.getX(), this.getY(), this.getX1(), this.getY1());
        super.tick();
    }

    public void draw(double mouseX, double mouseY) {
        SVGImage.draw(img, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        super.tick(mouseX, mouseY);
    }

    public Location getImage() {
        return img;
    }

    public void setImage(Location image) {
        img = image;
    }

    public double getWidth() {
        return this.getX1();
    }

    public void setWidth(double width) {
        super.setX1(width);
    }

    public double getHeight() {
        return this.getY1();
    }

    public void setHeight(double height) {
        super.setY1(height);
    }


}
