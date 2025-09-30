package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import tritium.event.eventapi.Handler;
import tritium.event.events.input.MouseXYChangeEvent;
import tritium.rendering.animation.Interpolations;
import tritium.widget.Widget;
import tritium.interfaces.SharedRenderingConstants;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 2024/11/10 21:58
 */
public class MouseStrokes extends Widget {

    public MouseStrokes() {
        super("MouseStrokes");
    }

    double relativeX = 0, relativeY = 0;
    double relativeXSmooth = 0, relativeYSmooth = 0;

    @Override
    public void onRender(boolean editing) {
        double width = 100, height = 100;


        SharedRenderingConstants.NORMAL.add(() -> {

            GlStateManager.pushMatrix();
            this.doScale();

            this.renderStyledBackground(this.getX(), this.getY(), width, height, 8);

            double centerX = this.getX() + width * 0.5;
            double centerY = this.getY() + height * 0.5;

            double circleSize = 15;

            double x = centerX - circleSize * 0.5 + relativeX;
            double y = centerY - circleSize * 0.5 + relativeY;

            x = Math.max(this.getX() + 4, Math.min(this.getX() + width - circleSize - 4, x));
            y = Math.max(this.getY() + 4, Math.min(this.getY() + height - circleSize - 4, y));

            this.roundedRect(x, y, circleSize, circleSize, circleSize * 0.5 - 0.75, Color.WHITE);

            float speed = 2f;
            relativeX = Interpolations.interpBezier(relativeX, relativeXSmooth, speed);
            relativeY = Interpolations.interpBezier(relativeY, relativeYSmooth, speed);

            relativeXSmooth = Interpolations.interpBezier(relativeXSmooth, 0, speed * 0.5);
            relativeYSmooth = Interpolations.interpBezier(relativeYSmooth, 0, speed * 0.5);

            GlStateManager.popMatrix();
        });

        this.setWidth(width);
        this.setHeight(height);
    }

    @Handler
    public void onMouseDelta(MouseXYChangeEvent event) {
        relativeXSmooth += event.deltaX * 0.25;
        relativeYSmooth -= event.deltaY * 0.25;
    }

}
