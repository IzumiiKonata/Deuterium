package tritium.module.impl.render;

import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import tritium.module.Module;
import tritium.rendering.animation.Interpolations;
import tritium.settings.BooleanSetting;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2024/1/26
 */
public class CameraPositions extends Module {

    public CameraPositions() {
        super("CameraPositions", Category.RENDER);
    }

    public NumberSetting<Double> x = new NumberSetting<>("X", 0.0, -5.0, 5.0, 0.1);
    public NumberSetting<Double> y = new NumberSetting<>("Y", 0.0, -5.0, 5.0, 0.1);
    public NumberSetting<Double> z = new NumberSetting<>("Z", 0.0, -5.0, 5.0, 0.1);
    public NumberSetting<Double> scale = new NumberSetting<>("Scale", 1.0, 0.1, 2.0, 0.05);

    public final BooleanSetting smoothThirdPerson = new BooleanSetting("Smooth Third Person", false);
    public NumberSetting<Double> smooth = new NumberSetting<>("Speed", 1.0, 1.0, 4.0, 0.1, this.smoothThirdPerson::getValue);

    public final BooleanSetting movementCamera = new BooleanSetting("Movement Camera", false);

    public final ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Simple, this.movementCamera::getValue);

    public enum Mode {
        Simple,
        Advanced
    }

    public final NumberSetting<Double> interp = new NumberSetting<>("Interpolation", 0.1, 0.1, 1.0, 0.1, () -> this.movementCamera.getValue() && this.mode.getValue() == Mode.Simple);

    public final NumberSetting<Double> rangeNear = new NumberSetting<>("Near Range", 6.0, 0.0, 20.0, 0.5, () -> this.movementCamera.getValue() && this.mode.getValue() == Mode.Advanced);

    public final NumberSetting<Double> interpNear = new NumberSetting<>("Interp Near", 0.1, 0.1, 1.0, 0.1, () -> this.movementCamera.getValue() && this.mode.getValue() == Mode.Advanced);
    public final NumberSetting<Double> interpFar = new NumberSetting<>("Interp Far", 0.1, 0.1, 1.0, 0.1, () -> this.movementCamera.getValue() && this.mode.getValue() == Mode.Advanced);
    public final NumberSetting<Double> interpYAxis = new NumberSetting<>("Interp Y Axis", 0.1, 0.1, 1.0, 0.1, () -> this.movementCamera.getValue() && this.mode.getValue() == Mode.Advanced);

    public final NumberSetting<Double> rangeReset = new NumberSetting<>("Reset Range", 20.0, 0.0, 100.0, 0.5, this.movementCamera::getValue);


    public double interpX, interpY, interpZ;

    private static int lastView = 0;

    public void offsetCamera() {

        if (!this.isEnabled() || Minecraft.getMinecraft().gameSettings.thirdPersonView != 1) {
            lastView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
            return;
        }

        final World world = Minecraft.getMinecraft().theWorld;

        Vec3 offset = new Vec3(this.x.getValue(), this.y.getValue(), this.z.getValue());
        var cameraDistance = calcCameraDistance(world, offset.lengthVector());
        Vec3 scaled = offset.normalize();
        scaled = new Vec3(scaled.xCoord * cameraDistance, scaled.yCoord * cameraDistance, scaled.zCoord * cameraDistance);
        GlStateManager.translate(scaled.xCoord, scaled.yCoord, scaled.zCoord);
        GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), this.scale.getValue());

        if (this.movementCamera.getValue()) {

            double v = this.interp.getValue() * 0.1f;
            Vec3 cameraPos = Minecraft.getMinecraft().getRenderViewEntity().getPositionEyes(Minecraft.getMinecraft().timer.renderPartialTicks);

            double diff = cameraPos.distanceTo(new Vec3(this.interpX, this.interpY, this.interpZ));


            if (this.mode.getValue() == Mode.Advanced) {

                if (diff <= this.rangeNear.getValue()) {
                    v = this.interpNear.getValue() * 0.25f;
                } else {
                    v = this.interpFar.getValue() * 0.25f;
                }

            }

            boolean shouldUpdate = (lastView != Minecraft.getMinecraft().gameSettings.thirdPersonView) || diff > this.rangeReset.getValue();

            if (shouldUpdate || this.interpX == 0 || this.interpY == 0 || this.interpZ == 0) {
                this.interpX = cameraPos.xCoord;
                this.interpY = cameraPos.yCoord;
                this.interpZ = cameraPos.zCoord;
            }

            this.interpX = Interpolations.interpBezier(this.interpX, cameraPos.xCoord, v);
            this.interpY = Interpolations.interpBezier(this.interpY, cameraPos.yCoord, v * 4f);
            this.interpZ = Interpolations.interpBezier(this.interpZ, cameraPos.zCoord, v);

            float yaw = Perspective.getCameraYaw();
            float pitch = Perspective.getCameraPitch();

            double[] t = rotateByY(cameraPos.xCoord - this.interpX, cameraPos.zCoord - this.interpZ, yaw % 360.0f);

            GlStateManager.rotate(-pitch, -1, 0, 0);
            GlStateManager.translate(-t[0], cameraPos.yCoord - this.interpY, -t[1]);
            GlStateManager.rotate(pitch, -1, 0, 0);

            lastView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
        }

    }

    // 呵呵 写这个二维向量旋转的东西我看了好久文档 这下提前预习高等数学了
    public static double[] rotateByY(double x, double z, float theta) {
        double ry = theta * Math.PI / 180;
        double outx = Math.cos(ry) * x + Math.sin(ry) * z;
        double outz = Math.cos(ry) * z - Math.sin(ry) * x;
        return new double[] { outx, outz };
    }

    private static double calcCameraDistance(World world, double distance) {
        Vec3 cameraPos = Minecraft.getMinecraft().getRenderViewEntity().getPositionEyes(Minecraft.getMinecraft().timer.renderPartialTicks);
        Vec3 cameraOffset = new Vec3(0, 0, 0);

        for (int i = 0; i < 8; i++) {
            Vec3 offset = new Vec3((i & 1) * 2, (i >> 1 & 1) * 2, (i >> 2 & 1) * 2)
                    .subtract(new Vec3(1, 1, 1));
            offset = new Vec3(offset.xCoord * 0.075D, offset.yCoord * 0.075D, offset.zCoord * 0.075D);
            Vec3 from = cameraPos.addVector(offset.xCoord, offset.yCoord, offset.zCoord);
            Vec3 to = from.addVector(cameraOffset.xCoord, cameraOffset.yCoord, cameraOffset.zCoord);
            MovingObjectPosition hitResult = world.rayTraceBlocks(from, to, false, true, false);

            if (hitResult != null) {
                double newDistance = hitResult.hitVec.distanceTo(cameraPos);

                if (newDistance < distance) {
                    distance = newDistance - 0.2;
                }
            }
        }

        return distance;
    }

}
