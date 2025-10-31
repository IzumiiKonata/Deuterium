package tritium.rendering.shader.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.uniform.Uniform1i;

/**
 * @author IzumiiKonata
 * Date: 2025/10/23 17:55
 */
public class StencilShader {

    private final ShaderProgram stencilProgram = new ShaderProgram("stencil.fsh", "vertex.vsh");
    private final Uniform1i mixTexture = new Uniform1i(stencilProgram, "mixTexture");
    private final Uniform1i stencilTexture = new Uniform1i(stencilProgram, "stencilTexture");

    public void draw(int baseTexture, int stencilTexture, double x, double y) {

        this.stencilProgram.start();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilTexture);
        RenderSystem.linearFilter();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(baseTexture);
        RenderSystem.linearFilter();

        this.mixTexture.setValue(16);
        this.stencilTexture.setValue(0);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        ShaderProgram.drawQuadFlipped(x, y, RenderSystem.getWidth(), RenderSystem.getHeight());
        ShaderProgram.stop();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
    }

}
