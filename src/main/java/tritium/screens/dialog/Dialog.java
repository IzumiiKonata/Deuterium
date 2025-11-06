package tritium.screens.dialog;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.RenderableEntity;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;

public abstract class Dialog implements SharedRenderingConstants {

    @Getter
    private boolean closing = false;
    private float maskAlpha = 0.0f;
    protected float alpha = 0.0f;

    private double openCloseScale = 1.1;

    public boolean previousMouse = true;


    public Dialog() {

    }

    public double width = 0, height = 0;
    public abstract void render(double mouseX, double mouseY);

    public void onRender(double mouseX, double mouseY) {
        this.drawBackgroundMask();

        this.openCloseScale = Interpolations.interpBezier(this.openCloseScale, this.isClosing() ? 1.1 : 1, 0.3);

        this.doGlPreTransforms(this.openCloseScale);

        Rect.draw((RenderSystem.getWidth() - width) * 0.5, (RenderSystem.getHeight() - height) * 0.5, width, height, ThemeManager.get(ThemeManager.ThemeColor.Surface, (int) (alpha * 255)));

        this.render(mouseX, mouseY);

        this.disposeTransforms();
    }

    protected void doGlPreTransforms(double scale) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5, 0);
        GlStateManager.scale(scale, scale, 0);
        GlStateManager.translate(RenderSystem.getWidth() * -0.5, RenderSystem.getHeight() * -0.5, 0);
    }

    protected void disposeTransforms() {
        GlStateManager.popMatrix();
    }

    protected void drawBackgroundMask() {
        this.maskAlpha = Interpolations.interpBezier(this.maskAlpha, this.closing ? 0.0f : 0.6f, 0.2f);
        this.alpha = Interpolations.interpBezier(this.alpha, this.closing ? 0.0f : 1f, 0.2f);
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RenderSystem.hexColor(0, 0, 0, (int) (this.maskAlpha * 255)), Rect.RectType.EXPAND);
    }

    public void close() {
        this.closing = true;
    }

    public boolean canClose() {
        return this.closing && this.maskAlpha < 0.1;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.close();
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
    }

}