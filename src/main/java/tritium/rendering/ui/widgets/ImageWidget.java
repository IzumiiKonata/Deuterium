package tritium.rendering.ui.widgets;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 15:11
 */
public class ImageWidget extends AbstractWidget<ImageWidget> {

    @Getter
    @Setter
    private Location locImg;

    public ImageWidget(Location locImg, double x, double y, double width, double height) {
        this.setBounds(x, y, width, height);
        this.locImg = locImg;
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        GlStateManager.color(1, 1, 1, this.getAlpha());
        Image.draw(locImg, this.getX(), this.getY(), this.getWidth(), this.getHeight(), Image.Type.NoColor);
    }
}
