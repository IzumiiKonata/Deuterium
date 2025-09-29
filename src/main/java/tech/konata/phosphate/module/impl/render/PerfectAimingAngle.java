package tech.konata.phosphate.module.impl.render;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render3DEvent;
import tech.konata.phosphate.event.events.world.TickEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.utils.player.PlayerUtils;

public class PerfectAimingAngle
extends Module {

    public PerfectAimingAngle() {
        super("PerfectAimingAngle", Module.Category.RENDER);
    }

    float[] rotations = new float[] {0.0f, 0.0f};
    float[] lastRot = new float[] {0.0f, 0.0f};

    @Handler
    public void onRender3D(Render3DEvent event) {

        double distance = 4.5;

        Entity ent = PlayerUtils.getEntity(distance);
        if (!(ent instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase entity = (EntityLivingBase)ent;

        Vec3 eyePos = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks);
        double yPos = eyePos.yCoord;
        if (mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight() > entity.posY + (double)entity.getEyeHeight()) {
            yPos = entity.posY + (double)entity.getEyeHeight();
        }

        rotations = this.getRotations(entity.posX, yPos, entity.posZ);

        Vec3 hitVec = PlayerUtils.hitVecToEnt(entity, rotations[0], rotations[1], lastRot[0], lastRot[1], event.partialTicks, distance);

        if (hitVec == null) {
            return;
        }

//        double size = 0.02;
//        GL11.glPushMatrix();
//        GL11.glEnable(3042);
//        GL11.glBlendFunc(770, 771);
//        GL11.glDisable(3553);
//        GL11.glEnable(2848);
//        GL11.glDisable(2929);
//        GL11.glDepthMask(false);
//        GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        double x = hitVec.xCoord - mc.getRenderManager().renderPosX;
        double y = hitVec.yCoord - mc.getRenderManager().renderPosY;
        double z = hitVec.zCoord - mc.getRenderManager().renderPosZ;
//        RenderSystem.drawBoundingBox(new AxisAlignedBB(x - size, y - size, z - size, x + size, y + size, z + size));
//        GL11.glDisable(2848);
//        GL11.glEnable(3553);
//        GL11.glEnable(2929);
//        GL11.glDepthMask(true);
//        GL11.glDisable(3042);
//        GL11.glPopMatrix();
        Vec3 hitVec1 = PlayerUtils.hitVecToEnt(entity, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch, event.partialTicks, distance);

        if (hitVec1 == null) {
            return;
        }

        double dist = hitVec.distanceTo(hitVec1);

        this.startDrawing(x, y, z);

        double imgSize = 12;

        Image.drawLinear(Location.of(Phosphate.NAME + "/textures/hit_marker.png"), -imgSize * 0.5, -imgSize * 0.5, imgSize, imgSize, Image.Type.Normal);

//        Rect.draw(-2, -2, 4, 4, -1, Rect.RectType.EXPAND);
        this.stopDrawing();

//        float x1 = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)event.getPartialTicks() - mc.getRenderManager().renderPosX);
//        float y1 = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)event.getPartialTicks() - mc.getRenderManager().renderPosY);
//        float z1 = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)event.getPartialTicks() - mc.getRenderManager().renderPosZ);
//        this.startDrawing(x1, y1, z1);
//
//        String s = EnumChatFormatting.GREEN + "OK";
//        if (dist > 0.1 && dist < 0.15) {
//            s = EnumChatFormatting.GOLD + "L";
//        }
//        if (dist > 0.15 && dist < 0.2) {
//            s = EnumChatFormatting.GOLD + "LL";
//        }
//        if (dist >= 0.2) {
//            s = EnumChatFormatting.RED + "???";
//        }
//        this.roundedRect(20.0, -60.0, 8 + mc.fontRendererObj.getStringWidth(s), 12.0, 4.0, new Color(0, 0, 0, 160));
//        GlStateManager.translate(0.0f, 0.0f, -1.0f);
//        mc.fontRendererObj.drawString(s, 24, -58, -1);
//        this.stopDrawing();
    }

    @Handler
    public void onTick(TickEvent event) {

        if (event.isPre())
            return;

        lastRot = rotations;

    }

    private void startDrawing(double x, double y, double z) {
        float invert = mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f;
        double size = 1.0;
        GL11.glPushMatrix();
        this.startDrawing();
        GL11.glTranslated(x, y, z);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(mc.getRenderManager().playerViewX, invert, 0.0f, 0.0f);
        GL11.glScaled(-0.01666666753590107 * size, -0.01666666753590107 * size, 0.01666666753590107 * size);
    }

    public void startDrawing() {
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
    }

    private void stopDrawing() {
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public float[] getRotations(double posX, double posY, double posZ) {
        EntityPlayerSP player = mc.thePlayer;
        double x = posX - player.posX;
        double y = posY - (player.posY + (double)player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float aYaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float aPitch = (float)(-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{aYaw, aPitch};
    }
}