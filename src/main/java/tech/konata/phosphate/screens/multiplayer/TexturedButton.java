package tech.konata.phosphate.screens.multiplayer;

import lombok.Getter;
import net.minecraft.util.Location;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.MultipleEndpointAnimation;
import tech.konata.phosphate.rendering.entities.RenderableEntity;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.utils.timing.Timer;

import java.time.Duration;

public class TexturedButton extends RenderableEntity {

    @Getter
    private final Location img;

    private final MultipleEndpointAnimation animation;
    private final Timer timer = new Timer();
    private final long delay;
    private final Runnable action;

    boolean previousMouse = false;
    @Getter
    private final String name;
    public TexturedButton(Location img, double x, double y, double width, double height, long delay, Runnable action) {
        super(x, y, width, height);

        this.img = img;
        this.delay = delay;
        this.action = action;
        this.name = img.getResourcePath().substring(img.getResourcePath().lastIndexOf("/") + 1, img.getResourcePath().lastIndexOf("."));
        this.animation = new MultipleEndpointAnimation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(250), RenderSystem.getHeight() + 10 + height)
                .addEndpoint(RenderSystem.getHeight() + 10 + height, Duration.ofMillis(250))
                .addEndpoint(RenderSystem.getHeight() - height - 4, Duration.ofMillis(200))
                .addEndpoint(RenderSystem.getHeight() - height, Duration.ofMillis(300));
    }

    boolean previousState = false;

    public void draw(double mouseX, double mouseY, ZephyrMultiPlayerUI inst) {

        boolean hovered = /*RenderSystem.isHovered(mouseX, mouseY, 0, RenderSystem.getHeight() - 60, RenderSystem.getWidth(), 60)*/true/* && inst.dialog == null*/;

        if (hovered && !previousState) {
            previousState = true;
            this.timer.reset();
        }

        if (!hovered && previousState) {
            previousState = false;
            this.timer.reset();
        }

        double posY = this.animation.getValue();

        if (inst.deleteMode && (this.getName().equals("add") || this.getName().equals("refresh"))) {
            this.animation.run(true);
        } else {

            if (this.timer.isDelayed(delay)) {

                if (inst.deleteMode && (this.getName().equals("remove") || this.getName().equals("back"))) {
                    this.animation.run(false);

                } else {
                    if (this.getName().equals("back")) {
                        this.animation.run(true);
                    } else {
                        this.animation.run(!hovered);
                    }
                }

            }

        }

        Image.drawLinear(img, this.getX(), posY, getWidth(), getHeight(), Image.Type.Normal);

        boolean isHoveredImg = RenderSystem.isHovered(mouseX, mouseY, this.getX(), posY, getWidth(), getHeight());

        if (isHoveredImg && Mouse.isButtonDown(0) && !previousMouse && inst.dialog == null) {
            previousMouse = true;
            this.action.run();
        }

        if (!Mouse.isButtonDown(0) && previousMouse)
            previousMouse = false;
    }
}
