package tritium.rendering.waveycapes.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import tritium.management.ModuleManager;
import tritium.rendering.waveycapes.CapeMovement;
import tritium.rendering.waveycapes.CapeStyle;
import tritium.rendering.waveycapes.Mth;
import tritium.rendering.waveycapes.WindMode;
import tritium.rendering.waveycapes.simulation.StickSimulation;

public class CustomCapeRenderLayer implements LayerRenderer<AbstractClientPlayer> {
    static final int partCount = 16;
    private final RenderPlayer playerRenderer;
    private final SmoothCapeRenderer smoothCapeRenderer;
    private ModelRenderer[] customCape;

    public CustomCapeRenderLayer(final RenderPlayer playerRenderer, final ModelBase model) {
        this.customCape = new ModelRenderer[16];
        this.smoothCapeRenderer = new SmoothCapeRenderer();
        this.playerRenderer = playerRenderer;
        this.buildMesh(model);
    }

    private static float easeOutSine(final float x) {
        return (float) Math.sin(x * 3.141592653589793 / 2.0);
    }

    private void buildMesh(final ModelBase model) {
        this.customCape = new ModelRenderer[16];
        for (int i = 0; i < 16; ++i) {
            final ModelRenderer base = new ModelRenderer(model, 0, i);
            base.setTextureSize(64, 32);
            this.customCape[i] = base.addBox(-5.0f, (float) i, -1.0f, 10, 1, 1);
        }
    }

    public void doRenderLayer(final AbstractClientPlayer abstractClientPlayer, final float nameFloat1, final float nameFloat2, final float deltaTick, final float animationTick, final float nameFloat5, final float nameFloat6, final float nameFloat7) {
        if (!ModuleManager.waveyCapes.isEnabled()) {
            return;
        }

        if (abstractClientPlayer.isInvisible()) {
            return;
        }
        if (!abstractClientPlayer.hasPlayerInfo() || abstractClientPlayer.isInvisible() || !abstractClientPlayer.isWearing(EnumPlayerModelParts.CAPE) || abstractClientPlayer.getLocationCape() == null) {
            return;
        }

        if (ModuleManager.waveyCapes.onlyLocalPlayer.getValue() && abstractClientPlayer != Minecraft.getMinecraft().thePlayer) {
            return;
        }

        if (ModuleManager.waveyCapes.capeMovement.getValue() == CapeMovement.BASIC_SIMULATION) {
            abstractClientPlayer.updateSimulation(16);
        }
        this.playerRenderer.bindTexture(abstractClientPlayer.getLocationCape());
        if (ModuleManager.waveyCapes.capeStyle.getValue() == CapeStyle.SMOOTH) {
            this.smoothCapeRenderer.renderSmoothCape(this, abstractClientPlayer, deltaTick);
        } else {
            final ModelRenderer[] parts = this.customCape;
            for (int part = 0; part < 16; ++part) {
                final ModelRenderer model = parts[part];
                GlStateManager.pushMatrix();
                this.modifyPoseStack(abstractClientPlayer, deltaTick, part);
                model.render(0.0625f);
                GlStateManager.popMatrix();
            }
        }
    }

    private void modifyPoseStack(final AbstractClientPlayer abstractClientPlayer, final float h, final int part) {
        if (ModuleManager.waveyCapes.capeMovement.getValue() == CapeMovement.BASIC_SIMULATION) {
            this.modifyPoseStackSimulation(abstractClientPlayer, h, part);
            return;
        }
        this.modifyPoseStackVanilla(abstractClientPlayer, h, part);
    }

    private void modifyPoseStackSimulation(AbstractClientPlayer abstractClientPlayer, final float delta, final int part) {
        StickSimulation simulation = abstractClientPlayer.getSimulation();
        GlStateManager.translate(0.0, 0.0, 0.125);
        float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
        if (z > 0.0f) {
            z = 0.0f;
        }
        final float y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);
        final float sidewaysRotationOffset = 0.0f;
        float partRotation = (float) (-Math.atan2(y, z));
        partRotation = Math.max(partRotation, 0.0f);
        if (partRotation != 0.0f) {
            partRotation = (float) (3.141592653589793 - partRotation);
        }
        partRotation *= 57.2958;
        partRotation *= 2.0f;
        float height = 0.0f;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0f;
            GlStateManager.translate(0.0f, 0.15f, 0.0f);
        }
        final float naturalWindSwing = this.getNatrualWindSwing(part);
        GlStateManager.rotate(6.0f + height + naturalWindSwing, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(sidewaysRotationOffset / 2.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(-sidewaysRotationOffset / 2.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(0.0f, y / 16.0f, z / 16.0f);
        GlStateManager.translate(0.0, 0.03, -0.03);
        GlStateManager.translate(0.0f, part * 1.0f / 16.0f, (float) (0 / 16));
        GlStateManager.translate(0.0f, -part * 1.0f / 16.0f, (float) (0 / 16));
        GlStateManager.translate(0.0, -0.03, 0.03);
    }

    void modifyPoseStackVanilla(final AbstractClientPlayer abstractClientPlayer, final float h, final int part) {
        GlStateManager.translate(0.0, 0.0, 0.125);
        final double d = Mth.lerp(h, abstractClientPlayer.prevChasingPosX, abstractClientPlayer.chasingPosX) - Mth.lerp(h, abstractClientPlayer.prevPosX, abstractClientPlayer.posX);
        final double e = Mth.lerp(h, abstractClientPlayer.prevChasingPosY, abstractClientPlayer.chasingPosY) - Mth.lerp(h, abstractClientPlayer.prevPosY, abstractClientPlayer.posY);
        final double m = Mth.lerp(h, abstractClientPlayer.prevChasingPosZ, abstractClientPlayer.chasingPosZ) - Mth.lerp(h, abstractClientPlayer.prevPosZ, abstractClientPlayer.posZ);
        final float n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        final double o = Math.sin(n * 0.017453292f);
        final double p = -Math.cos(n * 0.017453292f);
        float height = (float) e * 10.0f;
        height = MathHelper.clamp_float(height, -6.0f, 32.0f);
        float swing = (float) (d * o + m * p) * easeOutSine(0.0625f * part) * 100.0f;
        swing = MathHelper.clamp_float(swing, 0.0f, 150.0f * easeOutSine(0.0625f * part));
        float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0f;
        sidewaysRotationOffset = MathHelper.clamp_float(sidewaysRotationOffset, -20.0f, 20.0f);
        final float t = Mth.lerp(h, abstractClientPlayer.prevCameraYaw, abstractClientPlayer.cameraYaw);
        height += (float) (Math.sin(Mth.lerp(h, abstractClientPlayer.prevDistanceWalkedModified, abstractClientPlayer.distanceWalkedModified) * 6.0f) * 32.0 * t);
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0f;
            GlStateManager.translate(0.0f, 0.15f, 0.0f);
        }
        final float naturalWindSwing = this.getNatrualWindSwing(part);
        GlStateManager.rotate(6.0f + swing / 2.0f + height + naturalWindSwing, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(sidewaysRotationOffset / 2.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(-sidewaysRotationOffset / 2.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
    }

    float getNatrualWindSwing(final int part) {
        if (ModuleManager.waveyCapes.windMode.getValue() == WindMode.WAVES) {
            final long highlightedPart = System.currentTimeMillis() / 3L % 360L;
            final float relativePart = (part + 1) / 16.0f;
            return (float) (Math.sin(Math.toRadians(relativePart * 360.0f - highlightedPart)) * 3.0);
        }
        return 0.0f;
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}
