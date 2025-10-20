package tritium.interfaces;

import net.minecraft.client.renderer.GlStateManager;

/**
 * the interworking layer of the minecraft's font renderer and the client's font renderer
 *
 * @author IzumiiKonata
 * @since 6/20/2023 9:50 AM
 */
public interface IFontRenderer {

    float drawString(String text, double x, double y, int color);

    int drawStringWithShadow(String text, double x, double y, int color);

    int getHeight();

    int getStringWidth(String text);

    void drawCenteredString(String text, double x, double y, int color);

    default void drawCenteredString(String text, double x, double y, double scale, int color) {

        GlStateManager.pushMatrix();

        GlStateManager.translate(x - getStringWidth(text) * 0.5 * scale, y, 0);
        GlStateManager.scale(scale, scale, 1);

        drawString(text, 0, 0, color);

        GlStateManager.popMatrix();
    }

    default float drawString(String text, double x, double y, double scale, int color) {

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);

        float f = drawString(text, 0, 0, color);

        GlStateManager.popMatrix();
        return f;
    }

}
