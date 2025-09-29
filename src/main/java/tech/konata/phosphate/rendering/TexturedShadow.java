package tech.konata.phosphate.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:40 AM
 */
public class TexturedShadow {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawShadow(double x, double y, double width, double height) {

        drawTexturedRect(x - 9, y - 9, 9, 9, "shadow/paneltopleft");

        drawTexturedRect(x - 9, y + height, 9, 9, "shadow/panelbottomleft");

        drawTexturedRect(x + width, y + height, 9, 9, "shadow/panelbottomright");

        drawTexturedRect(x + width, y - 9, 9, 9, "shadow/paneltopright");

        drawTexturedRect(x - 9, y, 9, height, "shadow/panelleft");

        drawTexturedRect(x + width, y, 9, height, "shadow/panelright");

        drawTexturedRect(x, y - 9, width, 9, "shadow/paneltop");

        drawTexturedRect(x, y + height, width, 9, "shadow/panelbottom");

    }

    public static void drawShadow(double x, double y, double width, double height, float alpha) {

        drawTexturedRect(x - 9, y - 9, 9, 9, "shadow/paneltopleft", alpha);

        drawTexturedRect(x - 9, y + height, 9, 9, "shadow/panelbottomleft", alpha);

        drawTexturedRect(x + width, y + height, 9, 9, "shadow/panelbottomright", alpha);

        drawTexturedRect(x + width, y - 9, 9, 9, "shadow/paneltopright", alpha);

        drawTexturedRect(x - 9, y, 9, height, "shadow/panelleft", alpha);

        drawTexturedRect(x + width, y, 9, height, "shadow/panelright", alpha);

        drawTexturedRect(x, y - 9, width, 9, "shadow/paneltop", alpha);

        drawTexturedRect(x, y + height, width, 9, "shadow/panelbottom", alpha);

    }

    public static void drawShadow(double x, double y, double width, double height, float alpha, double radius) {

        drawTexturedRect(x - radius, y - radius, radius, radius, "shadow/paneltopleft", alpha);

        drawTexturedRect(x - radius, y + height, radius, radius, "shadow/panelbottomleft", alpha);

        drawTexturedRect(x + width, y + height, radius, radius, "shadow/panelbottomright", alpha);

        drawTexturedRect(x + width, y - radius, radius, radius, "shadow/paneltopright", alpha);

        drawTexturedRect(x - radius, y, radius, height, "shadow/panelleft", alpha);

        drawTexturedRect(x + width, y, radius, height, "shadow/panelright", alpha);

        drawTexturedRect(x, y - radius, width, radius, "shadow/paneltop", alpha);

        drawTexturedRect(x, y + height, width, radius, "shadow/panelbottom", alpha);
    }

    public static void drawShadow(double x, double y, double width, double height, float alpha, double radius, double shrink) {
        drawShadow(x + shrink, y + shrink, width - shrink * 2, height - shrink * 2, alpha, radius);
    }

    public static void drawBottomShadow(double x, double y, double width, float alpha, double radius) {

        drawTexturedRect(x, y, width, radius, "shadow/panelbottom", alpha);
    }

    public static void drawTopLeftShadow(double x, double y, float alpha, double radius) {

        drawTexturedRect(x - radius, y - radius, radius, radius, "shadow/paneltopleft", alpha);
    }

    public static void drawTopRightShadow(double x, double y, float alpha, double radius) {

        drawTexturedRect(x, y - radius, radius, radius, "shadow/paneltopright", alpha);
    }

    public static void drawBottomLeftShadow(double x, double y, float alpha, double radius) {

        drawTexturedRect(x - radius, y, radius, radius, "shadow/panelbottomleft", alpha);
    }

    public static void drawBottomRightShadow(double x, double y, float alpha, double radius) {

        drawTexturedRect(x, y, radius, radius, "shadow/panelbottomright", alpha);
    }

    public static void drawTopShadow(double x, double y, double width, float alpha, double radius) {

        drawTexturedRect(x, y - radius, width, radius, "shadow/paneltop", alpha);
    }

    public static void drawRightShadow(double x, double y, double height, float alpha, double radius) {

        drawTexturedRect(x, y, radius, height, "shadow/panelright", alpha);
    }

    public static void drawLeftShadow(double x, double y, double height, float alpha, double radius) {

        drawTexturedRect(x - radius, y, radius, height, "shadow/panelleft", alpha);
    }

    public static void drawTexturedRect(double x, double y, double width, double height, String image) {
        GL11.glPushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        mc.getTextureManager().bindTexture(Location.of(Phosphate.NAME + "/textures/" + image + ".png"));
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popAttrib();
        GL11.glPopMatrix();
    }

    public static void drawTexturedRect(double x, double y, double width, double height, String image, float alpha) {
        GL11.glPushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
//        GlStateManager.disableAlpha();
        mc.getTextureManager().bindTexture(Location.of(Phosphate.NAME + "/textures/" + image + ".png"));
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
//        GlStateManager.disableBlend();
//        GlStateManager.enableAlpha();
        GL11.glPopMatrix();
    }

    public static void drawFontShadow(double x, double y, double width, double height, float alpha) {
        GlStateManager.color(1, 1, 1, alpha);
        Image.draw(Location.of(Phosphate.NAME + "/textures/shadow/textshadow.png"), (float) x - 4.0f, y - 3.0f, width + 6f, height + 12, Image.Type.NoColor);
        GlStateManager.resetColor();
    }

    public static void drawFontShadow(double x, double y, double width, double height) {
        drawFontShadow(x, y, width, height, 1.0f);
    }
}
