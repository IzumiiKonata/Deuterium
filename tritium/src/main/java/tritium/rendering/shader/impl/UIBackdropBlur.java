package tritium.rendering.shader.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjglx.BufferUtils;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.uniform.Uniform1i;
import tritium.rendering.shader.uniform.Uniform2f;
import tritium.rendering.shader.uniform.UniformFB;

import java.nio.FloatBuffer;

public class UIBackdropBlur {

    private static final int RADIUS = 5;
    private static final float COMPRESSION = .5f;

    private final ShaderProgram blurProgram = new ShaderProgram("blur.frag", "vertex.vsh");

    private Framebuffer maskBuffer;
    private Framebuffer scratchBuffer;

    private GaussianKernel kernel = new GaussianKernel(0);

    private final Uniform1i u_radius = new Uniform1i(blurProgram, "u_radius");
    private final UniformFB u_kernel = new UniformFB(blurProgram, "u_kernel");
    private final Uniform1i u_diffuse_sampler = new Uniform1i(blurProgram, "u_diffuse_sampler");
    private final Uniform1i u_other_sampler = new Uniform1i(blurProgram, "u_other_sampler");
    private final Uniform2f u_texel_size = new Uniform2f(blurProgram, "u_texel_size");
    private final Uniform2f u_direction = new Uniform2f(blurProgram, "u_direction");

    public void draw(double x, double y, double width, double height, Runnable maskRenderer) {

        if (!Display.isVisible() || width <= 0 || height <= 0)
            return;

        Minecraft mc = Minecraft.getMinecraft();

        Framebuffer target = Framebuffer.currentlyBinding;
        if (target == null)
            target = mc.getFramebuffer();

        this.maskBuffer = RenderSystem.createFrameBuffer(this.maskBuffer);
        this.scratchBuffer = RenderSystem.createFrameBuffer(this.scratchBuffer);

        if (this.kernel.getSize() != RADIUS) {
            this.kernel = new GaussianKernel(RADIUS);
            this.kernel.compute();

            FloatBuffer buffer = BufferUtils.createFloatBuffer(RADIUS);
            buffer.put(this.kernel.getKernel());
            buffer.flip();

            this.blurProgram.start();
            this.u_radius.setValue(RADIUS);
            this.u_kernel.setValue(buffer);
            ShaderProgram.stop();
        }

        double pad = RADIUS + 2;
        RenderSystem.doScissor(x - pad, y - pad, width + pad * 2, height + pad * 2);

        this.maskBuffer.bindFramebuffer(true);
        this.maskBuffer.setFramebufferColor(0, 0, 0, 0);
        this.maskBuffer.framebufferClearNoBinding();
        GlStateManager.disableStencilTest();
        maskRenderer.run();

        this.blurProgram.start();
        this.u_diffuse_sampler.setValue(0);
        this.u_other_sampler.setValue(20);
        this.u_texel_size.setValue((float) (1.0F / RenderSystem.getWidth()), (float) (1.0F / RenderSystem.getHeight()));

        this.scratchBuffer.bindFramebuffer(true);
        this.scratchBuffer.framebufferClearNoBinding();
        GlStateManager.disableStencilTest();
        this.u_direction.setValue(COMPRESSION, 0.0F);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableAlpha();
        target.bindFramebufferTexture();
        ShaderProgram.drawQuadFlipped();

        target.bindFramebuffer(true);
        this.u_direction.setValue(0.0F, COMPRESSION);
        this.scratchBuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE20);
        this.maskBuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ShaderProgram.drawQuadFlipped();

        GlStateManager.disableBlend();
        ShaderProgram.stop();

        RenderSystem.endScissor();

        if (target.currentStencilValue > 0) {
            GlStateManager.enableStencilTest();
            GL11.glStencilFunc(GL11.GL_EQUAL, target.currentStencilValue, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        }

        GlStateManager.enableAlpha();
    }
}
