package tritium.widget.impl.keystrokes;

import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.rendersystem.RenderSystem;

public class Circle implements SharedRenderingConstants {

    public double length = 0;
    public float alpha = 120 * RenderSystem.DIVIDE_BY_255;

    public void draw(double posX, double posY) {

        roundedRect(posX - length, posY - length, length * 2, length * 2, length, 1f, 1f, 1f, alpha);

    }

}
