package tech.konata.phosphate.rendering.shader.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.rendering.shader.uniform.Uniform1i;
import tech.konata.phosphate.rendering.shader.uniform.Uniform2f;

/**
 * @author IzumiiKonata
 * Date: 2024/12/21 08:59
 */
public class MotionShader {

    private final ShaderProgram motionProgram = new ShaderProgram("motion.frag", "vertex.vsh");
    private final Uniform1i currentTexture = new Uniform1i(motionProgram, "currentTexture");
    private final Uniform1i previousTexture = new Uniform1i(motionProgram, "previousTexture");
    private final Uniform2f resolution = new Uniform2f(motionProgram, "resolution");

    public void run(int cur, int prev, float w, float h) {
        this.motionProgram.start();

        currentTexture.setValue(20);
        previousTexture.setValue(0);
        resolution.setValue(w, h);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(cur);
        GL13.glActiveTexture(GL13.GL_TEXTURE20);
        GlStateManager.bindTexture(prev);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        ShaderProgram.drawQuad(0, 0, w, h);
        ShaderProgram.stop();
    }

}
