package tritium.rendering.entities.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import tritium.rendering.entities.RenderableEntity;

public class GradientRect extends RenderableEntity {
    double x;
    double y;
    double width;
    double height;
    int color1;
    int color2;
    RenderType type;
    GradientType type_gradient;

    public GradientRect(double x, double y, double width, double height, int color1, int color2, RenderType type,
                        GradientType type_gradient) {
        super(x, y, width, height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color1 = color1;
        this.color2 = color2;
        this.type = type;
        this.type_gradient = type_gradient;
    }

    public void draw() {
        switch (type) {
            case Expand:
                this.drawRect();
                break;
            case Position:
                this.drawRect2();
                break;
            default:
                break;
        }
    }

    private void drawRect() {
        switch (type_gradient) {
            case Vertical:
                this.drawGradient(x, y, x + width, y + height, color1, color2);
                break;
            case Horizontal:
                this.drawGradientSideways(x, y, x + width, y + height, color1, color2);
                break;
            default:
                break;
        }
    }

    private void drawRect2() {
        switch (type_gradient) {
            case Vertical:
                this.drawGradient(x, y, width, height, color1, color2);
                break;
            case Horizontal:
                this.drawGradientSideways(x, y, width, height, color1, color2);
                break;
            default:
                break;
        }
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void drawHorizontalGradientRect(double x, double y, double x1, double y1, int yColor, int y1Color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        glColor(yColor);
        GL11.glVertex2d(x, y1);
        glColor(y1Color);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y);
        glColor(yColor);
        GL11.glVertex2d(x, y);
        GL11.glEnd();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void drawGradient(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) * 0.003921568627451F;
        float f1 = (float) (col1 >> 16 & 255) * 0.003921568627451F;
        float f2 = (float) (col1 >> 8 & 255) * 0.003921568627451F;
        float f3 = (float) (col1 & 255) * 0.003921568627451F;
        float f4 = (float) (col2 >> 24 & 255) * 0.003921568627451F;
        float f5 = (float) (col2 >> 16 & 255) * 0.003921568627451F;
        float f6 = (float) (col2 >> 8 & 255) * 0.003921568627451F;
        float f7 = (float) (col2 & 255) * 0.003921568627451F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer world = tessellator.getWorldRenderer();
        world.begin(7, DefaultVertexFormats.POSITION_COLOR);
        world.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        world.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        world.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
        world.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) * 0.003921568627451F;
        float f1 = (float) (col1 >> 16 & 255) * 0.003921568627451F;
        float f2 = (float) (col1 >> 8 & 255) * 0.003921568627451F;
        float f3 = (float) (col1 & 255) * 0.003921568627451F;
        float f4 = (float) (col2 >> 24 & 255) * 0.003921568627451F;
        float f5 = (float) (col2 >> 16 & 255) * 0.003921568627451F;
        float f6 = (float) (col2 >> 8 & 255) * 0.003921568627451F;
        float f7 = (float) (col2 & 255) * 0.003921568627451F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer world = tessellator.getWorldRenderer();
        world.begin(7, DefaultVertexFormats.POSITION_COLOR);
        world.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        world.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
        world.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        world.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public RenderType getType() {
        return type;
    }

    public void setType(RenderType type) {
        this.type = type;
    }

    public GradientType getType_gradient() {
        return type_gradient;
    }

    public void setType_gradient(GradientType type_gradient) {
        this.type_gradient = type_gradient;
    }

    public int getColor1() {
        return color1;
    }

    public void setColor1(int color1) {
        this.color1 = color1;
    }

    public int getColor2() {
        return color2;
    }

    public void setColor2(int color2) {
        this.color2 = color2;
    }

    public enum RenderType {
        Expand, Position
    }

    public enum GradientType {
        Vertical, Horizontal
    }
}