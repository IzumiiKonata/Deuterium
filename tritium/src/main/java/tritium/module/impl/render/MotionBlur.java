package tritium.module.impl.render;

import lombok.val;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.opengl.GL11;
import tritium.module.Module;
import tritium.settings.ClientSettings;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2024/8/24 21:33
 */
public class MotionBlur extends Module {

    public MotionBlur() {
        super("Motion Blur", Category.RENDER);
    }

    public final ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Framebuffer);

    public enum Mode {
        Accum,
        Framebuffer
    }

    public final NumberSetting<Float> amount = new NumberSetting<>("Amount", 7.0f, 0.0f, 10.0f, 0.1f);
    public final NumberSetting<Integer> brightness = new NumberSetting<>("Brightness", 50, 0, 100, 1, () -> mode.getValue() == Mode.Accum) {
        @Override
        public String getStringForRender() {
            return this.getValue() + "%";
        }
    };

    private Framebuffer checkFramebufferSizes(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer == null) {
                framebuffer = new Framebuffer(width, height, true);
            } else {
                framebuffer.createBindFramebuffer(width, height);
            }
            framebuffer.setFramebufferFilter(9728);
        }
        return framebuffer;
    }

    private Framebuffer blurBufferMain = null;
    private Framebuffer blurBufferInto = null;

    private void drawTexturedRectNoBlend(
            float x,
            float y,
            float width,
            float height,
            float uMin,
            float uMax,
            float vMin,
            float vMax,
            int filter
    ) {
        GlStateManager.enableTexture2D();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10241, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10240, filter);
        val tessellator = Tessellator.getInstance();
        val worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(uMin, vMax).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(uMax, vMax).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(uMax, vMin).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(uMin, vMin).endVertex();
        tessellator.draw();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10241, 9728);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 10240, 9728);
    }

    public void doFramebuffer() {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20 || mode.getValue() != Mode.Framebuffer)
            return;
        if (mc.currentScreen == null) {
            if (OpenGlHelper.isFramebufferEnabled()) {
                val sr = ScaledResolution.get();
                val width = mc.getFramebuffer().framebufferWidth;
                val height = mc.getFramebuffer().framebufferHeight;
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.loadIdentity();
                GlStateManager.ortho(
                        0.0,
                        ((double) width / sr.getScaleFactor()),
                        ((double) height / sr.getScaleFactor()),
                        0.0,
                        2000.0,
                        4000.0
                );
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.loadIdentity();
                GlStateManager.translate(0f, 0f, -2000f);
                blurBufferMain = checkFramebufferSizes(blurBufferMain, width, height);
                blurBufferInto = checkFramebufferSizes(blurBufferInto, width, height);
                blurBufferInto.framebufferClear();
                blurBufferInto.bindFramebuffer(true);
                OpenGlHelper.glBlendFunc(770, 771, 0, 1);
                GlStateManager.disableLighting();
                GlStateManager.disableFog();
                GlStateManager.disableBlend();
                mc.getFramebuffer().bindFramebufferTexture();
                GlStateManager.color(1f, 1f, 1f, 1f);
                drawTexturedRectNoBlend(
                        0.0f,
                        0.0f,
                        ((float) width / sr.getScaleFactor()),
                        ((float) height / sr.getScaleFactor()),
                        0.0f,
                        1.0f,
                        0.0f,
                        1.0f,
                        9728
                );
                GlStateManager.enableBlend();
                blurBufferMain.bindFramebufferTexture();
                GlStateManager.color(1f, 1f, 1f, amount.getValue() / 10 - 0.1f);
                drawTexturedRectNoBlend(
                        0f,
                        0f,
                        ((float) width / sr.getScaleFactor()),
                        ((float) height / sr.getScaleFactor()),
                        0f,
                        1f,
                        1f,
                        0f,
                        9728
                );
                mc.getFramebuffer().bindFramebuffer(true);
                blurBufferInto.bindFramebufferTexture();
                GlStateManager.color(1f, 1f, 1f, 1f);
                GlStateManager.enableBlend();
                OpenGlHelper.glBlendFunc(770, 771, 1, 771);
                drawTexturedRectNoBlend(
                        0.0f,
                        0.0f,
                        ((float) width / sr.getScaleFactor()),
                        ((float) height / sr.getScaleFactor()),
                        0.0f,
                        1.0f,
                        0.0f,
                        1.0f,
                        9728
                );
                val tempBuff = blurBufferMain;
                blurBufferMain = blurBufferInto;
                blurBufferInto = tempBuff;
            }
        }
    }

    public void doMotionBlur() {

        if (mode.getValue() != Mode.Accum)
            return;

        if (ClientSettings.FRAME_PREDICT.getValue()) {
            if (mc.ingameGUI != null) {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("开启帧预测的同时使用累加动态模糊会导致问题! 已自动切换到帧缓冲模式"));
            }

            mode.setValue(Mode.Framebuffer);
            return;
        }

        float accu = this.getAccumulationValue();
        GL11.glAccum(GL11.GL_MULT, accu);
        int bright = this.brightness.getValue();
        if (bright == 50) {
            GL11.glAccum(GL11.GL_ACCUM, 1.0f - accu);
        } else {
            GL11.glAccum(GL11.GL_ACCUM, (1.0f - accu) / 100.0f * bright * 2.0f);
        }
        GL11.glAccum(GL11.GL_RETURN, 1.0f);

        if (this.lastADMCheck + 1000L < System.currentTimeMillis()) {
            this.lastADMCheck = System.currentTimeMillis();
            final int error = GL11.glGetError();
            if (error == 1282) {
                this.setEnabled(false);
                if (mc.ingameGUI != null) {
                    mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("错误: 调用glAccum返回GL Error 1282! (可能是您的显卡不支持执行累积缓冲区!)"));
                }
            }
        }
    }

    private long lastADMCheck;

    public float getAccumulationValue() {
        float percent = this.amount.getValue() * 100.0f;
        if (percent > 990.0f) {
            percent = 990.0f;
        }
        return percent / 1000.0f;
    }

}
