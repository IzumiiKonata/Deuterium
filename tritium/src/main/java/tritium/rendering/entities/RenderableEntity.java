package tritium.rendering.entities;

import lombok.*;
import net.minecraft.client.Minecraft;
import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.rendersystem.RenderSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 4/15/2023 8:53 PM
 */
public class RenderableEntity implements SharedRenderingConstants {

    public final Minecraft mc = Minecraft.getMinecraft();

    @Setter
    private double x;

    @Setter
    private double y;

    @Getter
    @Setter
    private double width;

    @Getter
    @Setter
    private double height;

    boolean lmbPressed = false, rmbPressed = false;

    int clickMoveTicks = 0;
    long lastClick = 0L;

    @Getter
    @Setter
    private RenderableEntity parent = null;

    @Getter
    protected final List<RenderableEntity> container = new ArrayList<>();

    public RenderableEntity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addChild(RenderableEntity ent) {

        if (ent == this)
            return;

        ent.setParent(this);
        this.container.add(ent);

    }

    public void setPosition(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    public void setBounds(double width, double height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    public void onRender(double mouseX, double mouseY) {

    }

    public void update() {

    }

    public void draw(double mouseX, double mouseY) {

        this.update();
        this.onRender(mouseX, mouseY);

        if (lmbPressed || rmbPressed) {

            if (clickMoveTicks > 1) {
                this.handleMouseClickMove(mouseX, mouseY, lmbPressed ? 0 : 1, System.currentTimeMillis() - lastClick);
            }

            clickMoveTicks++;
        }

        if (this.hasChild()) {

            for (RenderableEntity ent : this.container) {
                ent.draw(mouseX, mouseY);
            }

        }

    }

    public boolean keyTyped(char typedChar, int keyCode) {
        return false;
    }

    public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    /**
     * @return handled
     */
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        boolean b = this.childMouseClicked(mouseX, mouseY, mouseButton);

        if (b)
            return true;

        boolean b1 = this.handleMouseClicked(mouseX, mouseY, mouseButton);

        if (b1) {
            clickMoveTicks = 0;
            lastClick = System.currentTimeMillis();

            if (mouseButton == 0) {
                this.lmbPressed = true;
            }

            if (mouseButton == 1) {
                this.rmbPressed = true;
            }
        }

        return b1;
    }

    public boolean childMouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.hasChild()) {

            for (RenderableEntity ent : this.container) {
                boolean result = ent.mouseClicked(mouseX, mouseY, mouseButton);

                if (result)
                    return true;
            }

        }

        return false;
    }

    public boolean handleMouseReleased(double mouseX, double mouseY, int mouseButton) {

        return false;

    }

    /**
     * @return handled
     */
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {

        boolean b = this.childMouseReleased(mouseX, mouseY, mouseButton);

        if (b)
            return true;

        if (mouseButton == 0) {
            this.lmbPressed = false;
        }

        if (mouseButton == 1) {
            this.rmbPressed = false;
        }

        return this.handleMouseReleased(mouseX, mouseY, mouseButton);

    }

    public boolean childMouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (this.hasChild()) {

            for (RenderableEntity ent : this.container) {
                boolean result = ent.mouseReleased(mouseX, mouseY, mouseButton);

                if (result)
                    return true;
            }

        }

        return false;
    }

    public boolean handleMouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {

        return false;

    }

    public boolean mouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        boolean b = this.childHandleMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);

        if (b)
            return true;

        return this.handleMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    public boolean childHandleMouseClickMove(double mouseX, double mouseY, int mouseButton, long timeSinceLastClick) {
        if (this.hasChild()) {

            for (RenderableEntity ent : this.container) {
                boolean result = ent.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);

                if (result)
                    return true;
            }

        }

        return false;
    }


    public boolean isInBounds(double mouseX, double mouseY) {
//        Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xff0090ff, Rect.RectType.EXPAND);

        return this.isInBounds(mouseX, mouseY, this.getWidth(), this.getHeight());
    }

    public boolean isInBounds(double mouseX, double mouseY, double width, double height) {
        return RenderSystem.isHovered(mouseX, mouseY, this.getX(), this.getY(), width, height);
    }

    public boolean isChild() {
        return this.parent != null;
    }

    public boolean hasChild() {
        return !this.container.isEmpty();
    }

    public double getX() {

        if (this.isChild()) {
            return this.getParent().getX() + this.x;
        }

        return x;
    }

    public double getY() {

        if (this.isChild()) {
            return this.getParent().getY() + this.y;
        }

        return y;
    }

    public double getRelativeX() {
        return x;
    }

    public double getRelativeY() {
        return y;
    }
}