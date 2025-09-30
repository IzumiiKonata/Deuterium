package tritium.rendering.shader.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjglx.opengl.Display;
import tritium.rendering.shader.Shader;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.ShaderRenderType;
import tritium.rendering.shader.uniform.Uniform1f;
import tritium.rendering.shader.uniform.Uniform1i;
import tritium.rendering.shader.uniform.Uniform2f;
import tritium.rendering.shader.uniform.UniformFB;
import tritium.settings.ClientSettings;
import tritium.utils.timing.Timer;

import java.nio.FloatBuffer;
import java.util.List;

public class BloomShader extends Shader {

    private final ShaderProgram bloomProgram = new ShaderProgram("bloom.frag", "vertex.vsh");
    private Framebuffer inputFramebuffer = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, true);
    private Framebuffer outputFramebuffer = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, true);
    private GaussianKernel gaussianKernel = new GaussianKernel(0);

    private Framebuffer cacheBuffer = new Framebuffer(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, true);

    Timer updateTimer = new Timer();
    boolean cache = false;

    private final Uniform1f u_radius = new Uniform1f(bloomProgram, "u_radius");
    private final UniformFB u_kernel = new UniformFB(bloomProgram, "u_kernel");

    private final Uniform1i u_diffuse_sampler = new Uniform1i(bloomProgram, "u_diffuse_sampler");
    private final Uniform1i u_other_sampler = new Uniform1i(bloomProgram, "u_other_sampler");
    private final Uniform2f u_texel_size = new Uniform2f(bloomProgram, "u_texel_size");
    private final Uniform2f u_direction = new Uniform2f(bloomProgram, "u_direction");


    @Override
    public void run(final ShaderRenderType type, List<Runnable> runnable) {
        // Prevent rendering
        if (!Display.isVisible()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        this.update();

        boolean useCaching = ClientSettings.SHADERS_FRAMERATE.getValue() != 0;

        int freq = ClientSettings.SHADERS_FRAMERATE.getValue();
        if (freq == ClientSettings.RENDER2D_FRAMERATE.getMaximum()) {
            freq = Display.getDesktopDisplayMode().getFrequency();
        }

        // 使用大神framebuffer缓冲优化
        if (useCaching) {

            if (updateTimer.isDelayed(1000 / freq)) {
                updateTimer.reset();
                cache = true;
                this.runNoCaching(type, runnable);
                cache = false;
            }

            mc.getFramebuffer().bindFramebuffer(true);

            GlStateManager.disableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(cacheBuffer.framebufferTexture);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            ShaderProgram.drawQuad();

        } else {
            this.runNoCaching(type, runnable);
        }

    }

    // shader的逻辑在这里
    @Override
    public void runNoCaching(ShaderRenderType type, List<Runnable> runnable) {

        // Prevent rendering
        if (!Display.isVisible()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        this.update();

        switch (type) {
            // Render3D
            case CAMERA: {
                this.setActive(!runnable.isEmpty());

                if (this.isActive()) {
                    RendererLivingEntity.NAME_TAG_RANGE = 0;
                    RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 0;

                    this.inputFramebuffer.bindFramebuffer(true);
                    this.inputFramebuffer.framebufferClearNoBinding();

                    runnable.forEach(Runnable::run);

                    if (cache) {
                        cacheBuffer.bindFramebuffer(true);
                        cacheBuffer.framebufferClearNoBinding();
                    } else {
                        mc.getFramebuffer().bindFramebuffer(true);
                    }

                    RendererLivingEntity.NAME_TAG_RANGE = 64;
                    RendererLivingEntity.NAME_TAG_RANGE_SNEAK = 32;

                    RenderHelper.disableStandardItemLighting();
                    mc.entityRenderer.disableLightmap();
                }
                break;
            }
            // Render2D
            case OVERLAY: {
                this.setActive(this.isActive() || !runnable.isEmpty());

                if (this.isActive()) {
                    this.inputFramebuffer.bindFramebuffer(true);
                    this.inputFramebuffer.framebufferClearNoBinding();
                    runnable.forEach(Runnable::run);

                    // TODO: make radius and other things as a setting
                    final int radius = 8;
                    final float compression = 2F;
                    final int programId = this.bloomProgram.getProgramId();

                    this.outputFramebuffer.bindFramebuffer(true);
                    this.outputFramebuffer.framebufferClearNoBinding();

                    GlStateManager.disableAlpha();

                    this.bloomProgram.start();

                    if (this.gaussianKernel.getSize() != radius) {
                        this.gaussianKernel = new GaussianKernel(radius);
                        this.gaussianKernel.compute();

                        final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
                        buffer.put(this.gaussianKernel.getKernel());
                        buffer.flip();

                        u_radius.setValue(radius);
                        u_kernel.setValue(buffer);
                    }

                    u_diffuse_sampler.setValue(0);
                    u_other_sampler.setValue(20);
                    u_texel_size.setValue(1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
                    u_direction.setValue(compression, 0.0F);

                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
                    GlStateManager.disableAlpha();

                    inputFramebuffer.bindFramebufferTexture();
                    ShaderProgram.drawQuad();

                    if (cache) {
                        cacheBuffer.bindFramebuffer(true);
                        cacheBuffer.framebufferClearNoBinding();
                        GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE , GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
                    } else {
                        mc.getFramebuffer().bindFramebuffer(true);
                        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                    }

                    u_direction.setValue(0.0F, compression);
                    outputFramebuffer.bindFramebufferTexture();
                    GL13.glActiveTexture(GL13.GL_TEXTURE20);
                    inputFramebuffer.bindFramebufferTexture();
                    GL13.glActiveTexture(GL13.GL_TEXTURE0);
                    ShaderProgram.drawQuad();
                    GlStateManager.disableBlend();

                    ShaderProgram.stop();
                }

                break;
            }

        }
    }

    @Override
    public void update() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.displayWidth != inputFramebuffer.framebufferWidth || mc.displayHeight != inputFramebuffer.framebufferHeight) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

            outputFramebuffer.deleteFramebuffer();
            outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

            cacheBuffer.deleteFramebuffer();
            cacheBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
//            inputFramebuffer.framebufferClear();
//            outputFramebuffer.framebufferClear();
        }

        inputFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        outputFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        cacheBuffer.setFramebufferColor(0, 0, 0, 0.0F);

    }
}