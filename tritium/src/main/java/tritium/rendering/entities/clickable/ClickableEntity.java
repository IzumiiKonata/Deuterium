package tritium.rendering.entities.clickable;

import tritium.rendering.entities.RenderableEntity;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public class ClickableEntity extends RenderableEntity {

    private ClickHandler onClick, onRelease, onHold;

    private boolean leftPressed, rightPressed;

    public ClickableEntity(double x, double y, double width, double height, ClickHandler onClick, ClickHandler onRelease, ClickHandler onHold) {
        super(x, y, width, height);

        this.onClick = onClick;
        this.onRelease = onRelease;
        this.onHold = onHold;

    }

    public ClickableEntity(double x, double y, double width, double height, ClickHandler onClick, ClickHandler onRelease) {
        super(x, y, width, height);

        this.onClick = onClick;
        this.onRelease = onRelease;
    }

    public ClickableEntity(double x, double y, double width, double height, ClickHandler onClick) {
        super(x, y, width, height);

        this.onClick = onClick;
    }

    @Override
    public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (this.isInBounds(mouseX, mouseY)) {

            // LMB
            if (mouseButton == 0) {
                this.leftPressed = true;
            }

            // RMB
            else if (mouseButton == 1) {
                this.rightPressed = true;
            }

            this.onClick.handle(mouseX, mouseY, mouseButton);

            return true;
        }

        return false;
    }

    @Override
    public boolean handleMouseReleased(double mouseX, double mouseY, int mouseButton) {

        if (this.isInBounds(mouseX, mouseY)) {

            // LMB
            if (mouseButton == 0) {
                this.leftPressed = false;
            }

            // RMB
            else if (mouseButton == 1) {
                this.rightPressed = false;
            }

            this.onRelease.handle(mouseX, mouseY, mouseButton);
            return true;
        }

        return false;
    }

    public interface ClickHandler {

        void handle(double mouseX, double mouseY, int mouseButton);

    }

}
