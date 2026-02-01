package tritium.bridge.rendering;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import today.opai.api.dataset.BlockPosition;
import today.opai.api.dataset.BoundingBox;
import today.opai.api.features.ExtensionWidget;
import today.opai.api.interfaces.dataset.Vector2f;
import today.opai.api.interfaces.functions.WorldToScreenCallback;
import today.opai.api.interfaces.game.entity.Entity;
import today.opai.api.interfaces.game.entity.Player;
import today.opai.api.interfaces.game.item.ItemStack;
import today.opai.api.interfaces.render.Image;
import today.opai.api.interfaces.render.RenderUtil;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.rendering.StencilClipManager;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;

import java.awt.*;
import java.io.InputStream;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:54
 */
public class RenderUtilImpl implements RenderUtil, SharedRenderingConstants {

    @Getter
    private static final RenderUtilImpl instance = new RenderUtilImpl();

    @Override
    public Image createImage(String base64Data) {
        return new ImageImpl(base64Data);
    }

    @Override
    public Image createImage(byte[] data) {
        return new ImageImpl(data);
    }

    @Override
    public Image createImage(InputStream data) {
        return new ImageImpl(data);
    }

    @Override
    public void renderWidgetBackground(ExtensionWidget widget) {

        this.roundedRect(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), 5.6, 1, 0, 0, 0, 80 * RenderSystem.DIVIDE_BY_255);

        StencilClipManager.beginClip(() -> Rect.draw(widget.getX() - 1, widget.getY() - 1, widget.getWidth() + 2, 18, -1));

        this.roundedRect(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), 5.6, 1, 0, 0, 0, 80 * RenderSystem.DIVIDE_BY_255);
        FontManager.pf18bold.drawString(widget.getName(), widget.getX() + 4, widget.getY() + 4.5, -1);

        StencilClipManager.endClip();
    }

    @Override
    public void worldToScreen(Entity entity, WorldToScreenCallback callback) {

    }

    @Override
    public void worldToScreen(BlockPosition position, WorldToScreenCallback callback) {

    }

    @Override
    public void worldToScreen(BoundingBox boundingBox, WorldToScreenCallback callback) {

    }

    @Override
    public Vector2f worldToScreen(float x, float y, float z) {
        return null;
    }

    @Override
    public void drawRect(float x, float y, float width, float height, Color color) {
        Rect.draw(x, y, width, height, color.getRGB());
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2, float width, Color color) {
        RenderSystem.color(color.getRGB());
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    @Override
    public void drawRoundRect(float x, float y, float width, float height, int radius, Color color) {
        this.roundedRect(x, y, width, height, radius, color);
    }

    @Override
    public void drawShadow(float x, float y, float width, float height, float radius, Color color) {

    }

    @Override
    public void drawGradientRect(float x, float y, float width, float height, Color color, Color color2) {
        RenderSystem.drawGradientRectLeftToRight(x, y, x + width, y + height, color.getRGB(), color2.getRGB());
    }

    @Override
    public void drawVerticalGradientRect(float x, float y, float width, float height, Color color, Color color2) {
        RenderSystem.drawGradientRectTopToBottom(x, y, x + width, y + height, color.getRGB(), color2.getRGB());
    }

    @Override
    public long getDelta() {
        return (long) (RenderSystem.getFrameDeltaTime() * 10L);
    }

    @Override
    public void drawPlayerHead(float x, float y, float size, Player player) {
        RenderSystem.drawPlayerHead(player.getUUID(), x, y, size);
    }

    @Override
    public void drawRoundPlayerHead(float x, float y, float size, Player player, float radius) {
        Location skinTexture = RenderSystem.playerSkinTextureCache.getSkinTexture(player.getUUID(), (l, b) -> {
        });

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject texture = textureManager.getTexture(skinTexture);
        if (skinTexture == null || texture == null)
            return;

        GlStateManager.bindTexture(texture.getGlTextureId());
        RenderSystem.resetColor();
        this.roundedRectTextured(x, y, size, size, radius);
    }

    @Override
    public void drawBoundingBox(Entity entity, Color color) {

    }

    @Override
    public void drawItem(ItemStack itemStack, float x, float y) {

    }

    @Override
    public void drawItemNoCount(ItemStack itemStack, float x, float y) {

    }

    @Override
    public void drawStencil() {
        StencilClipManager.beginClip();
    }

    @Override
    public void readStencil() {
        StencilClipManager.updateClip();
    }

    @Override
    public void endStencil() {
        StencilClipManager.endClip();
    }

    @Override
    public boolean isFrameBufferWriting() {
        return false;
    }

    @Override
    public float getPartialTicks() {
        return Minecraft.getMinecraft().timer.renderPartialTicks;
    }

    @Override
    public void drawBoundingBox(BoundingBox boundingBox, Color color) {

    }
}
