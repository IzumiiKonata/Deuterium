package tritium.rendering.shader;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import tritium.Tritium;
import tritium.rendering.shader.special.ShaderRenderer;

public class StencilShader {
    private static ShaderRenderer stencilProgram;
    private static ShaderRenderer clearProgram;
    private static ShaderRenderer stencilProgram2;
    private static ShaderRenderer stencilProgram3;

    public static void render(int backgroundSource, int stencilSource) {
        if (stencilProgram == null) {
            stencilProgram = new ShaderRenderer("tritium/shaders/stencil.fsh");
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.blendFunc(770, 771);
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

        stencilProgram.init();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilSource);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(backgroundSource);

        GL20.glUniform1i(stencilProgram.getUniform("mixTexture"), 16);
        GL20.glUniform1i(stencilProgram.getUniform("stencilTexture"), 0);
        ShaderRenderer.drawQuads();
        stencilProgram.unload();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);
    }

    public static void clear(int stencilSource, int originalSource, float factor, float alpha) {
        if (clearProgram == null) {
            clearProgram = new ShaderRenderer("tritium/shaders/clear.fsh");
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.blendFunc(770, 771);
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

        clearProgram.init();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilSource);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(originalSource);

        GL20.glUniform1i(clearProgram.getUniform("mixTexture"), 16);
        GL20.glUniform1i(clearProgram.getUniform("stencilTexture"), 0);
        clearProgram.setUniformf("factor", factor);
        clearProgram.setUniformf("alpha", alpha);
        ShaderRenderer.drawQuads();
        clearProgram.unload();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);
    }

    public static void render(int backgroundSource, int originalSource, int stencilSource) {
        if (stencilProgram2 == null) {
            stencilProgram2 = new ShaderRenderer("tritium/shaders/stencil2.fsh");
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

        stencilProgram2.init();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilSource);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(backgroundSource);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE15);
        GlStateManager.bindTexture(originalSource);

        GL20.glUniform1i(stencilProgram2.getUniform("mixTexture"), 15);
        GL20.glUniform1i(stencilProgram2.getUniform("mixTexture2"), 16);
        GL20.glUniform1i(stencilProgram2.getUniform("stencilTexture"), 0);
        ShaderRenderer.drawQuads();
        stencilProgram2.unload();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE15);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);
    }

    public static void render(int backgroundSource, int stencilSource, float brightness, float saturation, float contrast) {
        if (stencilProgram3 == null) {
            stencilProgram3 = new ShaderRenderer("tritium/shaders/blend.fsh");
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

        stencilProgram3.init();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(stencilSource);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(backgroundSource);

        GL20.glUniform1i(stencilProgram3.getUniform("mixTexture"), 16);
        GL20.glUniform1i(stencilProgram3.getUniform("stencilTexture"), 0);
        GL20.glUniform1f(stencilProgram3.getUniform("brightness"), brightness);
        GL20.glUniform1f(stencilProgram3.getUniform("saturation"), saturation);
        GL20.glUniform1f(stencilProgram3.getUniform("contrast"), contrast);
        ShaderRenderer.drawQuads();
        stencilProgram3.unload();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE15);
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);
    }
}