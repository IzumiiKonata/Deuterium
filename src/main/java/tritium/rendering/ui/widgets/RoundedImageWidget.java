package tritium.rendering.ui.widgets;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.Location;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.ui.AbstractWidget;

import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 15:11
 */
public class RoundedImageWidget extends AbstractWidget<RoundedImageWidget> {

    @Getter
    @Setter
    private Supplier<Location> locImg;

    @Getter
    private double radius = 0;

    boolean fadeIn = false;

    public RoundedImageWidget(Supplier<Location> locImg, double x, double y, double width, double height) {
        this.setBounds(x, y, width, height);
        this.locImg = locImg;
    }

    public RoundedImageWidget(Location locImg, double x, double y, double width, double height) {
        this(() -> locImg, x, y, width, height);
    }

    public RoundedImageWidget(double x, double y, double width, double height) {
        this(() -> null, x, y, width, height);
    }

    public RoundedImageWidget fadeIn() {
        fadeIn = true;
        this.setAlpha(0);
        return this;
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        Location img = locImg.get();

        if (img == null)
            return;

        ITextureObject textureObject = Minecraft.getMinecraft().getTextureManager().getTexture(img);

        if (textureObject == null)
            return;

        if (fadeIn)
            this.setAlpha(Interpolations.interpBezier(this.getAlpha(), 1.0f, 0.2f));

        GlStateManager.color(1, 1, 1, this.getAlpha());
        GlStateManager.bindTexture(textureObject.getGlTextureId());
        this.roundedRectTextured(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getRadius(), this.getAlpha());
    }

    public RoundedImageWidget setRadius(double radius) {
        this.radius = radius;
        return this;
    }
}
