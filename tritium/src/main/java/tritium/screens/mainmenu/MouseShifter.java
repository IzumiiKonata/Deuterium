package tritium.screens.mainmenu;

import net.minecraft.client.renderer.GlStateManager;
import tritium.rendering.animation.Animation;
import tritium.rendering.animation.Easing;

import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2023/12/24
 */
public class MouseShifter {

    Animation xAnim = new Animation(Easing.SIGMOID, Duration.ofMillis(50));
    Animation yAnim = new Animation(Easing.SIGMOID, Duration.ofMillis(50));

    public void doShift(double mouseX, double mouseY, double width, double height) {

        double shiftDist = 10;

        GlStateManager.translate(width * 0.5, height * 0.5, 0);

        double xDist = ((width * 0.5 - mouseX) / width * 0.5) * shiftDist;
        double yDist = ((height * 0.5 - mouseY) / height * 0.5) * shiftDist;

        GlStateManager.translate(-xAnim.run(xDist), -yAnim.run(yDist), 0);

        GlStateManager.translate(width * -0.5, height * -0.5, 0);

    }

}
