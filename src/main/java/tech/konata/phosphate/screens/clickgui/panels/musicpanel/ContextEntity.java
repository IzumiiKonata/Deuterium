package tech.konata.phosphate.screens.clickgui.panels.musicpanel;

import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;

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
