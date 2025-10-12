package tritium.widget.impl.keystrokes;

import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.rendersystem.RenderSystem;

import java.awt.*;

public class Circle implements SharedRenderingConstants {

    public double length = 0;
    public float alpha = 120 * RenderSystem.DIVIDE_BY_255;

    public void draw(double posX, double posY) {

        roundedRect(posX - length, posY - length, length * 2, length * 2, length, new Color(255, 255, 255, (int) (alpha * 255)));

    }

}
