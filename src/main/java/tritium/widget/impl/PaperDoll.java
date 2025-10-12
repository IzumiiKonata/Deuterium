package tritium.widget.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.ShaderUtil;
import tritium.settings.BooleanSetting;
import tritium.settings.ClientSettings;
import tritium.settings.NumberSetting;
import tritium.widget.Widget;
import tritium.interfaces.SharedRenderingConstants;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:37 PM
 */
public class PaperDoll extends Widget {

    public static boolean isRendering = false;

//    public AbstractClientPlayer renderingEntity = null;

    public float prevRenderYawOffset = 0, renderYawOffset = 0, prevCameraYaw = 0, cameraYaw = 0;

    public BooleanSetting customRotation = new BooleanSetting("Custom Rotation", false);
    public NumberSetting<Float> yaw = new NumberSetting<>("Yaw", 0f, 0f, 360f, 0.1f, () -> customRotation.getValue());
    public NumberSetting<Float> pitch = new NumberSetting<>("Pitch", 0f, -90f, 90f, 0.1f, () -> customRotation.getValue());

    public PaperDoll() {
        super("Paper Doll");
    }

    Framebuffer fb = new Framebuffer(0, 0, true);

    @Override
    public void onRender(boolean editing) {
        double posX = this.getX();
        double posY = this.getY();



        if (ClientSettings.VIDEO_PRESET.getValue() == ClientSettings.VideoPreset.Quality) {

            SharedRenderingConstants.NORMAL.add(() -> {
                GlStateManager.pushMatrix();
                this.doScale();
                GlStateManager.color(1, 1, 1, 1);

                fb = RenderSystem.createFrameBuffer(fb);

                fb.bindFramebuffer(true);
                fb.framebufferClearNoBinding();
//                GlStateManager.pushMatrix();

                drawEntityOnScreen(posX, posY, 100, mc.thePlayer);
                GlStateManager.popMatrix();

//                GlStateManager.popMatrix();

                mc.getFramebuffer().bindFramebuffer(true);

                GlStateManager.bindTexture(fb.framebufferTexture);
                ShaderUtil.drawQuads();
            });

            SharedRenderingConstants.BLOOM.add(() -> {
//                GlStateManager.pushMatrix();
//                this.doScale();

                GlStateManager.enableBlend();
                GlStateManager.disableAlpha();
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                GlStateManager.bindTexture(fb.framebufferTexture);
                GlStateManager.color(0, 0, 0, 1);
                ShaderUtil.drawQuads();
                GlStateManager.bindTexture(0);
//                GlStateManager.popMatrix();
            });
        } else {
            SharedRenderingConstants.NORMAL.add(() -> {
                GlStateManager.pushMatrix();
                this.doScale();
                GlStateManager.color(1, 1, 1, 1);
                drawEntityOnScreen(posX, posY, 100, mc.thePlayer);
                GlStateManager.popMatrix();
            });
        }

//        GlStateManager.popAttrib();

        this.setWidth(115);
        this.setHeight(210);
    }

    public void drawEntityOnScreen(double posX, double posY, double scale, AbstractClientPlayer ent) {
        isRendering = true;
//        renderingEntity = ent;
        prevRenderYawOffset = ent.prevRenderYawOffset;
        renderYawOffset = ent.renderYawOffset;
        prevCameraYaw = ent.prevCameraYaw;
        cameraYaw = ent.cameraYaw;
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        float f5 = ent.prevRenderYawOffset;
        float f6 = ent.prevRotationPitch;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enablePaperDollLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        ent.renderYawOffset = 0;
        ent.rotationYaw = 0;
        ent.rotationYawHead = 0;
        ent.prevRotationYawHead = 0;
        ent.prevRenderYawOffset = 0;
        ent.prevRotationPitch = ent.rotationPitch;


        if (customRotation.getValue()) {
            ent.renderYawOffset = ent.rotationYaw = ent.rotationYawHead = ent.prevRotationYawHead = ent.prevRenderYawOffset = yaw.getValue();
            ent.prevRotationPitch = ent.rotationPitch = pitch.getValue();
        }

        GlStateManager.translate(0.58F, -2.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        GlStateManager.disableBlend();
        RenderSystem.resetColor();
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, mc.timer.renderPartialTicks);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        ent.prevRenderYawOffset = f5;
        ent.prevRotationPitch = f6;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        isRendering = false;

    }


}
