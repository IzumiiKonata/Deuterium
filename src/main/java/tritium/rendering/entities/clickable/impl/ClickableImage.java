package tritium.rendering.entities.clickable.impl;


import net.minecraft.util.Location;
import tritium.rendering.entities.impl.Image;

public class ClickableImage extends ClickEntity {

    private Image.Type type;
    private Location img;

    public ClickableImage(Location image, double x, double y, double x1, double y1, Image.Type type,
                          Runnable click, Runnable hold, Runnable focus, Runnable release, Runnable onBlur) {
        super(x, y, x1, y1, MouseBounds.CallType.Expand, click, hold, focus, release, onBlur);
        this.type = type;
        this.img = image;
    }

    public void draw() {
        Image.draw(img, this.getX(), this.getY(), this.getX1(), this.getY1(), this.type);
        super.tick();
    }

    public void draw(double mouseX, double mouseY) {
        Image.draw(img, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.type);
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

    public Image.Type getType() {
        return type;
    }

    public void setType(Image.Type type) {
        this.type = type;
    }

}
