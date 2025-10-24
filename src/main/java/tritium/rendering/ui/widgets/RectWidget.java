package tritium.rendering.ui.widgets;

import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.settings.ClientSettings;

/**
 * @author IzumiiKonata
 * Date: 2025/7/8 20:28
 */
public class RectWidget extends AbstractWidget<RectWidget> {

    public RectWidget(double x, double y, double width, double height) {
        this.setBounds(x, y, width, height);
    }

    public RectWidget() {
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getHexColor());


    }

}
