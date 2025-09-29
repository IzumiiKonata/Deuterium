package tech.konata.phosphate.rendering;

import net.minecraft.client.renderer.GlStateManager;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;

import java.awt.*;
import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2024/12/5 13:36
 */
public class CheckRenderer implements SharedRenderingConstants {

    Animation left = new Animation(Easing.EASE_IN_CUBIC, Duration.ofMillis(100));
    Animation right = new Animation(Easing.EASE_OUT_CUBIC, Duration.ofMillis(120));

    Animation lBounce = new Animation(Easing.EASE_IN_CUBIC, Duration.ofMillis(140));
    Animation rBounce = new Animation(Easing.EASE_OUT_CUBIC, Duration.ofMillis(20));
    boolean rBounced = false;
    float alpha = 0;


    public void render(double x, double y, double size, double lineSize, boolean checked) {

        double offsetX = x + size * 0.45;
        double offsetY = y + size * 0.7;

        double lWidth = size * 0.4;
        double rWidth = size * 0.6;

        if (checked) {
            left.run(1);

            if (left.isFinished()) {

                lBounce.run(0);

                right.run(1);

                if (right.isFinished()) {
                    if (!rBounced) {
                        rBounce.run(0.05);

                        if (rBounce.isFinished()) {
                            rBounced = true;
                        }
                    } else {
                        rBounce.run(0);
                    }
                }
            } else {
                right.reset();
            }

            alpha = 1;
        } else {
            left.reset();
            left.setValue(0);
            left.setStartValue(0);

            right.reset();
            right.setValue(0);
            right.setStartValue(0);

            lBounce.reset();
            lBounce.setValue(0.1);
            lBounce.setStartValue(0.1);

            rBounce.reset();
            rBounce.setValue(0);
            rBounce.setStartValue(0);

            rBounced = false;

            alpha = Interpolations.interpBezier(alpha, 0, 0.2f);
        }

        double lVal = checked ? left.getValue() : 1;
        double rVal = checked ? right.getValue() + rBounce.getValue() : 1;

        Color c = new Color(255, 255, 255, (int) (alpha * 255));

        GlStateManager.pushMatrix();
        rotateAtPos(offsetX, offsetY, 50);
        this.roundedRect(offsetX - lWidth - (checked ? lWidth * lBounce.getValue() : 0) + lineSize * 0.5, offsetY - lineSize * 0.5, lWidth * lVal, lineSize, 0.5, c);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        rotateAtPos(offsetX, offsetY, 130);
        this.roundedRect(offsetX - rWidth * rVal + lineSize * 0.5, offsetY - lineSize * 0.5, rWidth * rVal, lineSize, 0.5, c);
        GlStateManager.popMatrix();


    }

}
