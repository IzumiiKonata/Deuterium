package tritium.utils.player.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;

/**
 * @author IzumiiKonata
 * @since 2022/7/16 10:04
 */
public class RotationUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Rotation serverRotation = new Rotation(0, 0);

    public static float[] getRotations(final BlockPos pos, final EnumFacing facing) {
        return getRotations(pos.getX(), pos.getY(), pos.getZ(), facing);
    }

    public static float[] getNeededRotations(final Vector3d current, final Vector3d target) {
        final double diffX = target.x - current.x;
        final double diffY = target.y - current.y;
        final double diffZ = target.z - current.z;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch)};
    }

    public static float[] getRotations(final double x, final double y, final double z, final EnumFacing facing) {
        final EntityPig temp = new EntityPig(mc.theWorld);
        temp.posX = x + 0.5;
        temp.posY = y + 0.5;
        temp.posZ = z + 0.5;

        temp.posX += facing.getDirectionVec().getX() * 0.5;
        temp.posY += facing.getDirectionVec().getY() * 0.5;
        temp.posZ += facing.getDirectionVec().getZ() * 0.5;

        return getRotations(temp);
    }

    public static float[] getRotations(double posX, double posY, double posZ) {
        EntityPlayerSP player = RotationUtils.mc.thePlayer;

        double x = posX - player.posX;
        double y = posY - (player.posY + (double) player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(BlockPos blockPos) {
        return getRotations(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    //Skidded from Minecraft
    public static float[] getRotations(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final double diffX = entity.posX - mc.thePlayer.posX;
        final double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY;
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase elb = (EntityLivingBase) entity;
            diffY = elb.posY + (elb.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        final float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static boolean isVisibleFOV(final Entity e, final float fov) {
        return ((Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f) : (Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f)) <= fov;
    }

    public static float getYawToEntity(final Entity e) {
        return ((Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f) : (Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f));
    }

    public static float[] getRotation(Entity a1) {
        double v1 = a1.posX - mc.thePlayer.posX;
        double v3 = a1.posY + (double) a1.getEyeHeight() * 0.9 - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
        double v5 = a1.posZ - mc.thePlayer.posZ;

        double v7 = MathHelper.ceiling_float_int((float) (v1 * v1 + v5 * v5));
        float v9 = (float) (Math.atan2(v5, v1) * 180.0 / 3.141592653589793) - 90.0f;
        float v10 = (float) (-(Math.atan2(v3, v7) * 180.0 / 3.141592653589793));
        return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(v9 - mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(v10 - mc.thePlayer.rotationPitch)};
    }

    public static VecRotation searchCenter(final AxisAlignedBB bb, final boolean predict) {

        VecRotation vecRotation = null;

        for (double xSearch = 0.15D; xSearch < 0.85D; xSearch += 0.1D) {
            for (double ySearch = 0.15D; ySearch < 1D; ySearch += 0.1D) {
                for (double zSearch = 0.15D; zSearch < 0.85D; zSearch += 0.1D) {
                    final Vec3 vec3 = new Vec3(bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch);
                    final Rotation rotation = toRotation(vec3, predict);

                    final VecRotation currentVec = new VecRotation(vec3, rotation);

                    if (vecRotation == null || (getRotationDifference(currentVec.getRotation()) < getRotationDifference(vecRotation.getRotation())))
                        vecRotation = currentVec;
                }
            }
        }

        return vecRotation;
    }

    public static double getRotationDifference(Rotation rotation) {
        return getRotationDifference(rotation, serverRotation);
    }

    private static float getAngleDifference(float a, float b) {
        return ((a - b) % 360.0F + 540.0F) % 360.0F - 180.0F;
    }

    public static double getRotationDifference(Rotation a, Rotation b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getYaw()), (a.getPitch() - b.getPitch()));
    }

    public static Rotation toRotation(final Vec3 vec, final boolean predict) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY +
                mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict)
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Rotation(MathHelper.wrapAngleTo180_float(
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F
        ), MathHelper.wrapAngleTo180_float(
                (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle = Math.abs(angle1 - angle2) % 360.0F;
        if (angle > 180.0F) {
            angle = 360.0F - angle;
        }
        return angle;
    }

    public static float getYawChange(float yaw, double posX, double posZ) {
        double deltaX = posX - Minecraft.getMinecraft().thePlayer.posX;
        double deltaZ = posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double yawToEntity = 0.0;
        if (deltaZ < 0.0 && deltaX < 0.0) {
            if (deltaX != 0.0) {
                yawToEntity = 90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX));
            }
        } else if (deltaZ < 0.0 && deltaX > 0.0) {
            if (deltaX != 0.0) {
                yawToEntity = -90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX));
            }
        } else if (deltaZ != 0.0) {
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }
        return MathHelper.wrapAngleTo180_float(-(yaw - (float) yawToEntity));
    }

    public static float getPitchChange(float pitch, Entity entity, double posY) {
        double deltaX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double deltaZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double deltaY = posY - 2.2 + (double) entity.getEyeHeight() - Minecraft.getMinecraft().thePlayer.posY;
        double distanceXZ = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));
        return -MathHelper.wrapAngleTo180_float(pitch - (float) pitchToEntity) - 2.5f;
    }

    public static float[] getRotationFromPosition(final double x, final double z, final double y) {
        final double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        final double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        final double yDiff = y - Minecraft.getMinecraft().thePlayer.posY;
        final double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        final float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0 / 3.141592653589793) - 90.0f;
        final float pitch = (float) (-(Math.atan2(yDiff, dist) * 180.0 / 3.141592653589793));
        return new float[]{yaw, pitch};
    }

    public static class VecRotation {
        Vec3 vec;
        Rotation rotation;

        public VecRotation(Vec3 vec, Rotation rotation) {
            this.vec = vec;
            this.rotation = rotation;
        }

        public Rotation getRotation() {
            return this.rotation;
        }

        public Vec3 getVec() {
            return vec;
        }
    }

    public static class Rotation {
        float yaw, pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() {
            return this.yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return this.pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public void toPlayer(EntityPlayer player) {
            if (Float.isNaN(yaw) || Float.isNaN(pitch))
                return;

            fixedSensitivity(mc.gameSettings.mouseSensitivity);

            player.rotationYaw = yaw;
            player.rotationPitch = pitch;
        }

        public void fixedSensitivity(Float sensitivity) {
            float f = sensitivity * 0.6F + 0.2F;
            float gcd = f * f * f * 1.2F;

            yaw -= yaw % gcd;
            pitch -= pitch % gcd;
        }

    }
}
