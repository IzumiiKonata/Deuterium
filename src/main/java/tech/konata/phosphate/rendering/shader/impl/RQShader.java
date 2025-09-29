package tech.konata.phosphate.rendering.shader.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.rendering.FramebufferCaching;
import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.rendering.shader.ShaderUniforms;
import tech.konata.phosphate.rendering.shader.uniform.Uniform1f;
import tech.konata.phosphate.rendering.shader.uniform.Uniform2f;
import tech.konata.phosphate.rendering.shader.uniform.Uniform4f;

import java.awt.*;

public class RQShader {

    private final ShaderProgram program = new ShaderProgram("rq.frag", "vertex.vsh");

    private final Uniform2f u_size = new Uniform2f(program, "u_size");
    private final Uniform1f u_radius = new Uniform1f(program, "u_radius");
    private final Uniform4f u_color = new Uniform4f(program, "u_color");

    /**
     * Draws a rounded rectangle at the given coordinates with the given lengths
     *
     * @param x      The top left x coordinate of the rectangle
     * @param y      The top y coordinate of the rectangle
     * @param width  The width which is used to determine the second x rectangle
     * @param height The height which is used to determine the second y rectangle
     * @param radius The radius for the corners of the rectangles (>0)
     * @param color  The color used to draw the rectangle
     */
    public void draw(float x, float y, float width, float height, final float radius, final Color color) {

        if (x > x + width) {
            float i = x;
            x = x + width;
            width = i - x;
        }

        if (y > y + height) {
            float j = y;
            y = y + height;
            height = j - y;
        }

        this.program.start();

        this.u_size.setValue(width, height);
        this.u_radius.setValue(radius);
        this.u_color.setValue(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        ShaderProgram.drawQuad(x, y, width, height);
        ShaderProgram.stop();
    }

    /**
     * Draws a rounded rectangle at the given coordinates with the given lengths
     *
     * @param x      The top left x coordinate of the rectangle
     * @param y      The top y coordinate of the rectangle
     * @param width  The width which is used to determine the second x rectangle
     * @param height The height which is used to determine the second y rectangle
     * @param radius The radius for the corners of the rectangles (>0)
     * @param color  The color used to draw the rectangle
     */
    public void draw(final double x, final double y, final double width, final double height, final double radius, final Color color) {
        draw((float) x, (float) y, (float) width, (float) height, (float) radius, color);
    }
}
