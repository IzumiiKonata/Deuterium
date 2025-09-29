package tech.konata.phosphate.rendering.shader.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.rendering.shader.uniform.Uniform1f;
import tech.konata.phosphate.rendering.shader.uniform.Uniform2f;
import tech.konata.phosphate.rendering.shader.uniform.Uniform4f;

import java.awt.*;

// Rounded Outline Quad
public class ROQShader {

    private final ShaderProgram program = new ShaderProgram("roq.glsl", "vertex.vsh");
    private final Uniform2f u_size = new Uniform2f(program, "u_size");
    private final Uniform1f u_radius = new Uniform1f(program, "u_radius");
    private final Uniform1f u_border_size = new Uniform1f(program, "u_border_size");
    private final Uniform4f u_color = new Uniform4f(program, "u_color");

    public void draw(float x, float y, float width, float height, float radius, float borderSize, Color color) {

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

        int programId = this.program.getProgramId();
        this.program.start();
        
        this.u_size.setValue(width, height);
        this.u_radius.setValue(radius - 2f);
        this.u_border_size.setValue(borderSize);
        this.u_color.setValue(color.getRed() * RenderSystem.DIVIDE_BY_255, color.getGreen() * RenderSystem.DIVIDE_BY_255, color.getBlue() * RenderSystem.DIVIDE_BY_255, color.getAlpha() * RenderSystem.DIVIDE_BY_255);
        
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        ShaderProgram.drawQuad(x, y, width, height);
//        GlStateManager.disableBlend();
        ShaderProgram.stop();
    }

    public void draw(double x, double y, double width, double height, double radius, double borderSize, Color color) {
        draw((float) x, (float) y, (float) width, (float) height, (float) radius, (float) borderSize, color);
    }
}
