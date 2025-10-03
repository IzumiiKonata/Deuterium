package tritium.rendering.ui.widgets;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.Location;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;

import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 15:11
 */
public class ImageWidget extends AbstractWidget<ImageWidget> {

    @Getter
    @Setter
    private Supplier<Location> locImg;

    public ImageWidget(Supplier<Location> locImg, double x, double y, double width, double height) {
        this.setBounds(x, y, width, height);
        this.locImg = locImg;
    }

    public ImageWidget(Location locImg, double x, double y, double width, double height) {
        this(() -> locImg, x, y, width, height);
    }

    public ImageWidget(double x, double y, double width, double height) {
        this(() -> null, x, y, width, height);
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        Location img = locImg.get();

        if (img == null)
            return;

        ITextureObject textureObject = Minecraft.getMinecraft().getTextureManager().getTexture(img);

        if (textureObject == null)
            return;

        GlStateManager.color(1, 1, 1, this.getAlpha());
        Image.draw(textureObject, this.getX(), this.getY(), this.getWidth(), this.getHeight(), Image.Type.NoColor);
    }
}
