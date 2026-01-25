package tritium.screens.contextmenu;

import lombok.Getter;
import lombok.Setter;
import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;

import java.time.Duration;

public abstract class ContextEntity implements SharedRenderingConstants {

    @Getter
    @Setter
    public String label;

    public Animation selectAlphaAnimation = new Animation(Easing.LINEAR, Duration.ofMillis(150L));

    public ContextEntity(String label) {
        this.label = label;
    }

    public abstract void render(ContextMenu menu, double x, double y, double width, double height, double mouseX, double mouseY, float alpha, int iAlpha);

    public boolean shouldClose(double mouseX, double mouseY) {
        return true;
    }

    public boolean shouldBeSelected(double mouseX, double mouseY) {
        return false;
    }

}
