package tech.konata.phosphate.module.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Location;
import net.minecraft.util.Vec3;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render3DEvent;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.module.impl.render.halo.HaloRenderer;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.settings.StringModeSetting;

import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/11/21 19:34
 */
public class Halo extends Module {

    public final HaloRenderer haloRenderer = new HaloRenderer();
    public StringModeSetting haloMode = new StringModeSetting("Style", "?!", "?!");

    public final BooleanSetting inFirstPerson = new BooleanSetting("First Person", false);
    public final BooleanSetting interpAnimation = new BooleanSetting("Interp Animation", false);
    public final BooleanSetting followHeadModel = new BooleanSetting("Follow Head Model", false);

    public final NumberSetting<Double> size = new NumberSetting<>("Size", 1.5, 0.1, 3.0, 0.1);
    public final NumberSetting<Float> yawRot = new NumberSetting<>("Yaw Rot", 0f, -90f, 90f, 1f);
    public final NumberSetting<Float> pitchRot = new NumberSetting<>("Pitch Rot", 0f, -90f, 90f, 1f);

    public Halo() {
        super("Halo", Category.RENDER);
        this.initHalos();
    }

    private void initHalos() {

        this.haloRenderer.getHaloDataMap().clear();
        List<String> haloNames = this.haloRenderer.getHaloNames();
        haloNames.clear();

        this.haloRenderer.addHalo(
                "砂狼 白子",
                new HaloRenderer.HaloData(
                        0.08,
                        Location.of(Phosphate.NAME + "/textures/bahalo/shiroko/layer0.png"),
                        Location.of(Phosphate.NAME + "/textures/bahalo/shiroko/layer1.png")
                )
        );

        this.haloRenderer.addHalo(
                "黑见 芹香",
                new HaloRenderer.HaloData(
                        0.08,
                        Location.of(Phosphate.NAME + "/textures/bahalo/serika/layer0.png"),
                        Location.of(Phosphate.NAME + "/textures/bahalo/serika/layer1.png")
                )
        );

        this.haloRenderer.addHalo(
                "小鸟游 星野",
                new HaloRenderer.HaloData(
                        0.04,
                        Location.of(Phosphate.NAME + "/textures/bahalo/hoshino/layer0.png"),
                        Location.of(Phosphate.NAME + "/textures/bahalo/hoshino/layer1.png"),
                        Location.of(Phosphate.NAME + "/textures/bahalo/hoshino/layer2.png")
                )
        );

        this.haloRenderer.setCurrentHalo(this.haloRenderer.getHaloDataMap().get(haloNames.get(0)));

        this.haloMode = new StringModeSetting("Style", haloNames.get(0), haloNames) {
            @Override
            public void onModeChanged(String before, String now) {
                haloRenderer.setCurrentHalo(haloRenderer.getHaloDataMap().get(now));
            }
        };

        this.getSettings().removeIf(s -> s instanceof StringModeSetting);
        this.getSettings().add(this.haloMode);
    }

    double interpX = 0, interpY = 0, interpZ = 0;

    @Handler
    public void onRender3D(Render3DEvent.OldRender3DEvent event) {

        if (mc.gameSettings.thirdPersonView == 0 && !this.inFirstPerson.getValue())
            return;

        float height = 1.4f;

        GlStateManager.pushMatrix();

        GlStateManager.translate(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ
        );

        SmallPlayerModel spm = ModuleManager.smallPlayerModel;

        if (spm.isEnabled()) {
            GlStateManager.scale(spm.bodyScale.getValue(), spm.bodyScale.getValue(), spm.bodyScale.getValue());
        }

        GlStateManager.translate(0, height, 0);

        if (spm.isEnabled()) {
            GlStateManager.scale(spm.headScale.getValue(), spm.headScale.getValue(), spm.headScale.getValue());
        }

        if (this.interpAnimation.getValue()) {
            Vec3 cameraPos = Minecraft.getMinecraft().getRenderViewEntity().getPositionEyes(Minecraft.getMinecraft().timer.renderPartialTicks);

            double diff = cameraPos.distanceTo(new Vec3(this.interpX, this.interpY, this.interpZ));

            if (diff > 6 || interpX == 0 || interpY == 0 || interpZ == 0) {
                this.interpX = cameraPos.xCoord;
                this.interpY = cameraPos.yCoord;
                this.interpZ = cameraPos.zCoord;
            }

            double speed = 0.8;
            this.interpX = Interpolations.interpBezier(this.interpX, cameraPos.xCoord, speed);
            this.interpY = Interpolations.interpBezier(this.interpY, cameraPos.yCoord, 1.6);
            this.interpZ = Interpolations.interpBezier(this.interpZ, cameraPos.zCoord, speed);

            GlStateManager.translate(
                    -(cameraPos.xCoord - this.interpX), -(cameraPos.yCoord - this.interpY), -(cameraPos.zCoord - this.interpZ)
            );
        }

        GlStateManager.rotate(mc.thePlayer.rotationYaw, 0, -1, 0);

        if (this.followHeadModel.getValue()) {
            GlStateManager.rotate(mc.thePlayer.rotationPitch, 1, 0, 0);
        }

        GlStateManager.rotate(this.yawRot.getValue(), 0, 0, 1);
        GlStateManager.rotate(this.pitchRot.getValue(), 1, 0, 0);

        GlStateManager.translate(0, 0.65, 0);

        GlStateManager.rotate(90, 1, 0, 0);

        boolean currentState = GlStateManager.cullState.cullFace.currentState;

        if (currentState)
            GlStateManager.disableCull();

        this.haloRenderer.render();

        GlStateManager.popMatrix();

        if (currentState)
            GlStateManager.enableCull();

    }

    private double calcDist(double x, double y, double z, double x2, double y2, double z2) {

        double xDiff = x - x2;
        double yDiff = y - y2;
        double zDiff = z - z2;

        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);

    }

}
