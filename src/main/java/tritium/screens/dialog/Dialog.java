package tritium.screens.dialog;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Keyboard;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.RenderableEntity;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.entities.impl.RoundedRect;
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
    Rect base = new Rect(0, 0, 0, 0, 0, Rect.RectType.EXPAND);

    public abstract void render(double mouseX, double mouseY);

    protected void addEntity(RenderableEntity ent) {
        base.addChild(ent);
    }

    public void onRender(double mouseX, double mouseY) {
        this.drawBackgroundMask();

        this.openCloseScale = Interpolations.interpBezier(this.openCloseScale, this.isClosing() ? 1.1 : 1, 0.3);

        this.doGlPreTransforms(this.openCloseScale);

        base.setX((RenderSystem.getWidth() - width) * 0.5);
        base.setY((RenderSystem.getHeight() - height) * 0.5);
        base.setWidth(width);
        base.setHeight(height);
        base.setColor(ThemeManager.get(ThemeManager.ThemeColor.Surface, (int) (alpha * 255)));

        double spacing = 4;

        for (RenderableEntity entity : base.getContainer()) {

            width = Math.max(entity.getRelativeX() + entity.getWidth() + spacing, width);
            height = Math.max(entity.getRelativeY() + entity.getHeight() + spacing, height);

        }

        base.draw(mouseX, mouseY);

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

        if (base.keyTyped(typedChar, keyCode))
            return;

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.close();
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        base.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        base.mouseReleased(mouseX, mouseY, mouseButton);
    }

}