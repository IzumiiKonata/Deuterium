package tritium.rendering.shader.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.uniform.Uniform1f;
import tritium.rendering.shader.uniform.Uniform4f;

import java.awt.*;

/**
 * @author IzumiiKonata
 * Date: 2025/3/2 20:43
 */
public class RoundShader {

    private final ShaderProgram program = new ShaderProgram("round.frag", "vertex.vsh");

    private final Uniform1f u_radius = new Uniform1f(program, "u_radius");
    private final Uniform4f u_color = new Uniform4f(program, "u_color");
    private final Uniform1f u_angle = new Uniform1f(program, "u_angle");

    public void draw(float x, float y, final float radius, float angle, final Color color) {

        this.program.start();

        this.u_radius.setValue(radius);
        this.u_color.setValue(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        this.u_angle.setValue(angle);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        ShaderProgram.drawQuad(x, y, radius * 2, radius * 2);
        ShaderProgram.stop();
    }

}
