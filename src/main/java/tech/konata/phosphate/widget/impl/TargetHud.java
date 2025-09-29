package tech.konata.phosphate.widget.impl;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import org.lwjglx.opengl.Display;
import org.lwjglx.util.glu.GLU;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.packet.SendPacketEvent;
import tech.konata.phosphate.event.events.rendering.Render3DEvent;
import tech.konata.phosphate.event.events.world.TickEvent;
import tech.konata.phosphate.event.events.world.WorldChangedEvent;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.animation.MultipleEndpointAnimation;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.utils.timing.Timer;
import tech.konata.phosphate.widget.Widget;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author IzumiiKonata
 * @since 6/27/2023 2:49 PM
 */
public class TargetHud extends Widget {

    public ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Simple);
    public Map<EntityLivingBase, TargetBean> targets = new HashMap<>();

    @Handler
    public void onWorldChanged(WorldChangedEvent event) {
        targets.clear();
    }

    ;

    @Handler
    public void onRender3D(Render3DEvent event) {
        if (mode.getValue() != Mode.FollowTarget)
            return;

        for (TargetBean value : this.targets.values()) {
            value.updateFollowPositions(event);
        }
    }

    ;

    @Handler
    public void onAttack(SendPacketEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) event.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {

            Entity entityFromWorld = ((C02PacketUseEntity) event.getPacket()).getEntityFromWorld(mc.theWorld);

            if (entityFromWorld instanceof EntityLivingBase) {
                if (!this.targets.containsKey((EntityLivingBase) entityFromWorld)) {
                    this.targets.put((EntityLivingBase) entityFromWorld, new TargetBean((EntityLivingBase) entityFromWorld));
                } else {
                    this.targets.get((EntityLivingBase) entityFromWorld).target = (EntityLivingBase) entityFromWorld;
                }
                this.targets.get((EntityLivingBase) entityFromWorld).mainTimer.reset();
            }
        }
    }

    ;
    long delay = 5000;

    @Handler
    public void onTick(TickEvent event) {
        delay = 5000;
        this.targets.forEach((t, targetHud) -> {
            if (targetHud.mainTimer.isDelayed(delay) && !targetHud.isCloseRequested && targetHud.percent >= 0.9) {
                targetHud.isCloseRequested = true;
            }
        });
    }

    ;
    DecimalFormat df = new DecimalFormat("#.##");
    int ordinary = 0;

    public TargetHud() {
        super("Target Hud");
    }

    @Override
    public void onDisable() {
        targets.clear();
    }

    @Override
    public void onRender(boolean editing) {

        double width = 150, height = 44;

        this.setWidth(width);
        this.setHeight(height);

        if (editing) {
            if (!this.targets.containsKey(mc.thePlayer))
                this.targets.put(mc.thePlayer, new TargetBean(mc.thePlayer));
            this.targets.get(mc.thePlayer).target = mc.thePlayer;

            this.targets.get(mc.thePlayer).mainTimer.reset();
            this.targets.get(mc.thePlayer).isCloseRequested = false;
            ordinary = 0;
            this.targets.get(mc.thePlayer).onRender(true, width, height);
        }

        if (targets.isEmpty()) {
            return;
        }


        if (editing)
            ordinary = 1;
        else
            ordinary = 0;

        for (TargetBean value : this.targets.values()) {
            if ((value.target == mc.thePlayer || value.target instanceof EntityPlayerSP)) {
                if (!value.mainTimer.isDelayed(delay) && editing) {
                    value.mainTimer.lastNs = (System.currentTimeMillis() + delay * 2) * 1_000_000;
                    value.percent = 0;
                }
                continue;
            }
            value.onRender(editing, width, height);
            ++ordinary;
        }
    }

    public enum Mode {
        Simple,
        FollowTarget
    }

    class TargetBean implements SharedRenderingConstants {
        final Location skin;
        private final Timer mainTimer = new Timer();
        private final MultipleEndpointAnimation animation = new MultipleEndpointAnimation(Easing.BEZIER, Duration.ofMillis(150), 0.0)
                .addEndpoint(0.0, Duration.ofMillis(150))
                .addEndpoint(1.1, Duration.ofMillis(150))
                .addEndpoint(1.0, Duration.ofMillis(150));
        public float percent = 0;
        public double damageDealt = 0;
        public Timer damageTimer = new Timer();
        public double lastHealth;
        double posX, posY;
        EntityLivingBase target;
        double hurtAnimPerc = 1.0;
        float hurtAnimAlpha = 0;
        int nextRowLimit = 3;
        private boolean isCloseRequested = false;
        private double targetHealthWidth = 0, targetHealthPercent = 0;
        private double healthBarHeight = 10;

        public TargetBean(EntityLivingBase target) {
            Render<Entity> entityRender = mc.getRenderManager().getEntityRenderObject(target);
            this.skin = entityRender.getTexture(target);
            this.target = target;
            this.animation.reset();
            this.lastHealth = this.target.getHealth();
        }

        public void updateFollowPositions(Render3DEvent event) {
            float pTicks = mc.timer.renderPartialTicks;

            Vector4d position = this.convertTo2D(target, event);

            if (position == null) return;
            this.posX = Interpolations.interpBezier(this.posX, position.z, 0.2f);
            this.posY = Interpolations.interpBezier(this.posY, position.w - (position.w - position.y) / 2, 0.2f);
        }

        public void onRender(boolean isEditing, double width, double height) {

            if (target == null) {
                ordinary -= 1;
                return;
            }

            double posX;
            double posY;

            if (mode.getValue() == Mode.FollowTarget) {
                posX = this.posX;
                posY = this.posY;
            } else {
                posY = getY() + this.posY;
                posX = getX() + this.posX;
            }

            if (mode.getValue() == Mode.Simple) {
                this.posX = Interpolations.interpBezier(this.posX, (ordinary / nextRowLimit) * 160.0, 0.2f);

                this.posY = Interpolations.interpBezier(this.posY, ordinary % nextRowLimit * 60.0, 0.2f);
            }


//            BLOOM.add(() -> {
//                if (this.mainTimer.isDelayed(delay) && !isCloseRequested && percent <= 0.4)
//                    return;
//
//                GlStateManager.pushMatrix();
//
//                RenderSystem.translateAndScale(posX + 62.5, posY + 33, percent);
//                GlStateManager.translate(0, hurtDownAnim, 0);
//
//                Rect.draw(posX, posY, width, height, hexColor(0, 0, 0, 100), Rect.RectType.EXPAND);
//
//                GlStateManager.popMatrix();
//            });
//
//            BLUR.add(() -> {
//                if (this.mainTimer.isDelayed(delay) && !isCloseRequested && percent <= 0.4)
//                    return;
//
//                GlStateManager.pushMatrix();
//
//                RenderSystem.translateAndScale(posX + 62.5, posY + 33, percent);
//                GlStateManager.translate(0, hurtDownAnim, 0);
//
//                Rect.draw(posX, posY, width, height, -1, Rect.RectType.EXPAND);
//
//                GlStateManager.popMatrix();
//            });

            if (this.mainTimer.isDelayed(delay) && !isCloseRequested && percent <= 0.4) {
                ordinary -= 1;
                return;
            }

            if (!isEditing) {
                if (percent <= 0.4 && (this.isCloseRequested/* || (!mc.thePlayer.canEntityBeSeen(target) || !RotationUtils.isVisibleFOV(target, 180))*/)) {
                    this.isCloseRequested = false;
                    this.animation.reset();
                    ordinary -= 1;
                }
            }

            NORMAL.add(() -> {
                GlStateManager.pushMatrix();

                TargetHud.this.doScale();

                //Animation
                if (!isEditing) {
                    percent = (float) animation.run(isCloseRequested);

                    if (percent != 0 || percent < 0.95) {
                        GlStateManager.translate(posX + width * 0.5, posY + height * 0.5, 0);
                        GlStateManager.scale(percent, percent, 0);
                        GlStateManager.translate(-(posX + width * 0.5), -(posY + height * 0.5), 0);
                    }
                }

                TargetHud.this.renderStyledBackground(posX, posY, width, height, 8);

                if (target.getHealth() != lastHealth) {
                    double delta = lastHealth - target.getHealth();
                    if (delta >= 0)
                        damageDealt = delta;
                    lastHealth = target.getHealth();
                    damageTimer.reset();
                }

                if (damageTimer.isDelayed(1000)) {
                    lastHealth = target.getHealth();
                    damageDealt = 0;
                    damageTimer.reset();
                }

                GlStateManager.pushMatrix();
                hurtAnimPerc = Interpolations.interpBezier(hurtAnimPerc, (double) target.hurtTime / (target.maxHurtTime + 0.00001), 0.4);
                hurtAnimAlpha = (float) Interpolations.interpBezier(hurtAnimAlpha, hurtAnimPerc * 120 * RenderSystem.DIVIDE_BY_255, 0.6f);

                double skinSize = height - 8;

                GlStateManager.translate(posX + 4 + skinSize * 0.5, posY + 4 + skinSize * 0.5, 0);

                GlStateManager.color(1, 1, 1, 1);

                boolean bVanilla = GlobalSettings.HUD_STYLE.getValue() == GlobalSettings.HudStyle.Vanilla;

                mc.getTextureManager().bindTexture(skin);
                double wtf = 1 / 64.0;
                if (bVanilla) {
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                    GlStateManager.enableAlpha();
                    Gui.drawScaledCustomSizeModalRect(-skinSize * 0.5, -skinSize * 0.5, 8.0f, 8.0f, 8, 8, skinSize, skinSize, 64.0f, 64.0f);
                } else {
                    roundedRectTextured(-skinSize * 0.5, -skinSize * 0.5, skinSize, skinSize, 8 * wtf, 8 * wtf, 16 * wtf, 16 * wtf, 8);
                }

                if (target instanceof EntityPlayer) {
                    if (((EntityPlayer) target).isWearing(EnumPlayerModelParts.HAT)) {

                        if (bVanilla) {
                            Gui.drawScaledCustomSizeModalRect(-skinSize * 0.5, -skinSize * 0.5, 40.0f, 8.0f, 8, 8, skinSize, skinSize, 64.0f, 64.0f);
                        } else {
                            roundedRectTextured(-skinSize * 0.5, -skinSize * 0.5, skinSize, skinSize, 40 * wtf, 8 * wtf, 48 * wtf, 16 * wtf, 8);
                        }

                    }
                }

                GlStateManager.bindTexture(0);

                GlStateManager.popMatrix();

                FontManager.pf25bold.drawStringWithBetterShadow(target.getName(), posX + 8 + skinSize, posY + 4, -1);

                List<String> info = Arrays.asList(
                        "Health: " + df.format(target.getHealth()),
                        mc.thePlayer.getHealth() > target.getHealth() ? "Winning" : (mc.thePlayer.getHealth() == target.getHealth() ? "Draw" : "Losing")
                );

                double offsetX = posX + 8 + skinSize;
                double offsetY = posY + 6 + FontManager.pf25.getHeight();

                FontManager.pf14.drawString(String.join(", ", info), offsetX, offsetY, -1);

                GlStateManager.color(1, 1, 1, 1);
                double healthBarWidth = width - (12 + skinSize);
                double totalHealth = target.getMaxHealth() + target.getAbsorptionAmount();
                double healthWidth = target.getHealth() / totalHealth * healthBarWidth;
                double healthBarX = posX + 8 + skinSize;
                double healthBarY = posY + height - healthBarHeight - 5;

                if (Double.isNaN(targetHealthWidth)) {
                    targetHealthWidth = healthWidth;
                }

                targetHealthWidth = Interpolations.interpBezier(targetHealthWidth, healthWidth, 0.4f);

                if (bVanilla) {
                    Rect.draw(healthBarX, healthBarY, healthBarWidth, healthBarHeight, hexColor(128, 128, 128, 128), Rect.RectType.EXPAND);
                    Rect.draw(healthBarX, healthBarY, targetHealthWidth, healthBarHeight, RenderSystem.reAlpha(this.getHealthColor(target.getHealth() / target.getMaxHealth()), 0.5f), Rect.RectType.EXPAND);
                } else {
                    roundedRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight, 3, new Color(128, 128, 128, 128));
                    roundedRect(healthBarX, healthBarY, targetHealthWidth, healthBarHeight, 3, new Color(RenderSystem.reAlpha(this.getHealthColor(target.getHealth() / target.getMaxHealth()), 0.5f), true));
                }

                targetHealthPercent = Interpolations.interpBezierApprox(targetHealthPercent, (((target.getHealth()) / target.getMaxHealth()) * 100), 0.15);

                if (healthBarHeight > 8) {
                    String percentText = (int) targetHealthPercent + "%";

                    double percentX;

                    if (targetHealthWidth - (FontManager.pf14.getStringWidth(percentText) + 6) < 0) {
                        percentX = healthBarX + targetHealthWidth + 4;
                    } else {
                        percentX = healthBarX + targetHealthWidth - FontManager.pf16.getStringWidth(percentText) - 2;
                    }

                    FontManager.pf16.drawStringWithBetterShadow(percentText, percentX, healthBarY + healthBarHeight / 2.0 - FontManager.pf16.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));

                }


                GlStateManager.popMatrix();
//            GlStateManager.popAttrib();
            });


        }

        public Vector4d convertTo2D(EntityLivingBase entity, Render3DEvent event) {
            final double renderX = mc.getRenderManager().renderPosX;
            final double renderY = mc.getRenderManager().renderPosY;
            final double renderZ = mc.getRenderManager().renderPosZ;
            ScaledResolution res = ScaledResolution.get();
            final double factor = res.getScaleFactor();
            final float partialTicks = event.partialTicks;

            final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderX;
            final double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks) - renderY;
            final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderZ;
            final double width = (entity.width + 0.2) / 2;
            final double height = entity.height + (entity.isSneaking() ? -0.3D : 0.2D) + 0.05;
            final AxisAlignedBB aabb = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
            final List<Vector3d> vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));

            Vector4d position = null;
            for (Vector3d vector : vectors) {
                vector = project(factor, vector.getX(), vector.getY(), vector.getZ());

                if (vector != null && vector.getZ() >= 0.0D && vector.getZ() < 1.0D) {
                    if (position == null) {
                        position = new Vector4d(vector.getX(), vector.getY(), vector.getZ(), 0.0D);
                    }

                    position = new Vector4d(Math.min(vector.getX(), position.x), Math.min(vector.getY(), position.y), Math.max(vector.getX(), position.z), Math.max(vector.getY(), position.w));
                }
            }

            return position;
        }

        private Vector3d project(final double factor, final double x, final double y, final double z) {
            if (GLU.gluProject((float) x, (float) y, (float) z, ActiveRenderInfo.MODELVIEW, ActiveRenderInfo.PROJECTION, ActiveRenderInfo.VIEWPORT, ActiveRenderInfo.OBJECTCOORDS)) {
                return new Vector3d((ActiveRenderInfo.OBJECTCOORDS.get(0) / factor), ((Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS.get(1)) / factor), ActiveRenderInfo.OBJECTCOORDS.get(2));
            }

            return null;
        }

        private int getHealthColor(double percent) {
            if (percent <= 0.6 && percent > 0.3) {
                return new Color(253, 173, 0).getRGB();
            } else if (percent <= 0.3) {
                return Color.RED.getRGB();
            } else {
                return new Color(57, 199, 56).getRGB();
            }
        }
    }
}
