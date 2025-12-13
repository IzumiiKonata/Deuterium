package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;

public class RenderPlayer extends RendererLivingEntity<AbstractClientPlayer> {

    public RenderPlayer(RenderManager renderManager) {
        this(renderManager, false);
    }

    public RenderPlayer(RenderManager renderManager, boolean useSmallArms) {
        super(renderManager, new ModelPlayer(0.0F, useSmallArms), 0.5F);
        /**
         * this field is used to indicate the 3-pixel wide arms
         */
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
    }

    public ModelPlayer getMainModel() {
        return (ModelPlayer) super.getMainModel();
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!entity.isUser() || this.renderManager.livingPlayer == entity) {
            double d0 = y;

            if (entity.isSneaking() && !(entity instanceof EntityPlayerSP)) {
                d0 = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            super.doRender(entity, x, d0, z, entityYaw, partialTicks);
        }
    }

    @Override
    public void renderName(AbstractClientPlayer entity, double x, double y, double z) {
        if (this.canRenderName(entity)) {
            double d0 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
            String str = entity.getDisplayName().getFormattedText();

            //            else {
//                PlayerAdapter instance = PlayerAdapter.getInstance(entity);
//                phosphate = instance.isUsingPhosphate();
//            }

            if (d0 <= 4096)
            {
                FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                float f = 1.6F;
                float f1 = 0.016666668F * f;
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)x + 0.0F, (float)y + entity.height + 0.5F, (float)z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-f1, -f1, f1);
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                int i = 0;

                if (str.equals("deadmau5"))
                {
                    i = -10;
                }

                int j = fontrenderer.getStringWidth(str) / 2;

                GlStateManager.enableTexture2D();

                fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);

                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }

//            if (d0 <= 4096) {
//                FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
//                float f = 1.6F;
//                float f1 = 0.016666668F * f;
//                GlStateManager.pushMatrix();
//                GlStateManager.translate((float) x + 0.0F, (float) y + entity.height + 0.5F, (float) z);
//                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
//                if (!PaperDoll.isRendering) {
//                    int thirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
//                    int k = thirdPersonView == 2 ? -180 : 0;
//                    int l = thirdPersonView == 2 ? -1 : 1;
//
//                    GlStateManager.rotate(/*-this.renderManager.playerViewY*/ -Perspective.getCameraYaw() - k, 0.0F, 1.0F, 0.0F);
//                    GlStateManager.rotate(/*this.renderManager.playerViewX*/Perspective.getCameraPitch() * l, 1.0F, 0.0F, 0.0F);
//                } else {
//                    GlStateManager.rotate(this.renderManager.playerViewY * (this.renderManager.livingPlayer.isSneaking() ? -1 : 1), 0.0F, 1.0F, 0.0F);
//                }
//                GlStateManager.scale(-f1, -f1, f1);
//                GlStateManager.disableLighting();
//                GlStateManager.depthMask(false);
//                GlStateManager.disableDepth();
//                GlStateManager.enableBlend();
//                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//                Tessellator tessellator = Tessellator.getInstance();
//                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
//                int i = 0;
//
//                if (str.equals("deadmau5")) {
//                    i = -10;
//                }
//
//                double j = fontrenderer.getStringWidth(str) / 2.0;
//
//                GlStateManager.enableDepth();
//                GlStateManager.depthMask(true);
//
//                GlStateManager.disableTexture2D();
//                Rect.draw(-j - 1, -1 + i, j * 2 + 1, 9, RenderSystem.hexColor(0, 0, 0, 64), Rect.RectType.EXPAND);
//                GlStateManager.enableTexture2D();
//
//                if (phosphate) {
//                    GlStateManager.translate(0, 0, -1);
//                    SVGImage.draw(Location.of(Phosphate.NAME + "/textures/icon_white.svg"), -fontrenderer.getStringWidth(str) / 2.0 + 1, i, 8, 8);
//                    fontrenderer.drawStringSpecial(str, -fontrenderer.getStringWidth(str) / 2.0f + 11, i, 553648127);
//                } else {
//                    fontrenderer.drawStringSpecial(str, -fontrenderer.getStringWidth(str) / 2.0f, i, 553648127);
//                }
//
//
//                GlStateManager.enableLighting();
//                GlStateManager.disableBlend();
//                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                GlStateManager.popMatrix();
//            }
        }
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
        ModelPlayer modelplayer = this.getMainModel();

        if (clientPlayer.isSpectator()) {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = clientPlayer.isSneaking();

            if (itemstack == null) {
                modelplayer.heldItemRight = 0;
            } else {
                if (Minecraft.getMinecraft().getItemRenderer().renderBothHands) {
                    modelplayer.heldItemRight = 0;
                } else {
                    modelplayer.heldItemRight = 1;
                }

                if (clientPlayer.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemstack.getItemUseAction();

                    if (enumaction == EnumAction.BLOCK) {

                        if (Minecraft.getMinecraft().getItemRenderer().renderBothHands) {
                            modelplayer.heldItemRight = 0;
                        } else {
                            modelplayer.heldItemRight = 3;
                        }

                    } else if (enumaction == EnumAction.BOW) {
                        modelplayer.aimedBow = true;
                    }
                }
            }
        }
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(AbstractClientPlayer entity) {
//        System.out.println(entity.getLocationSkin());
        return entity.getLocationSkin();
    }

    public void transformHeldFull3DItemLayer() {
        GlStateManager.translate(0.0F, 0.1875F, 0.0F);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime) {
        float f = 0.9375F;
        GlStateManager.scale(f, f, f);
    }

    protected void renderOffsetLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
        if (p_177069_10_ < 100.0D) {
            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null) {
                Score score = scoreboard.getValueFromObjective(entityIn.getName(), scoreobjective);
                this.renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
                y += (float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * p_177069_9_;
            }
        }

        super.renderOffsetLivingLabel(entityIn, x, y, z, str, p_177069_9_, p_177069_10_);
    }

    public void renderRightArm(AbstractClientPlayer clientPlayer) {
        float f = 1.0F;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.swingProgress = 0.0F;
        modelplayer.isSneak = false;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.renderRightArm();
    }

    public void renderLeftArm(AbstractClientPlayer clientPlayer) {
        float f = 1.0F;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.isSneak = false;
        modelplayer.swingProgress = 0.0F;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.renderLeftArm();
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z) {
        if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping()) {
            super.renderLivingAt(entityLivingBaseIn, x + (double) entityLivingBaseIn.renderOffsetX, y + (double) entityLivingBaseIn.renderOffsetY, z + (double) entityLivingBaseIn.renderOffsetZ);
        } else {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    protected void rotateCorpse(AbstractClientPlayer bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        if (bat.isEntityAlive() && bat.isPlayerSleeping()) {
            GlStateManager.rotate(bat.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        } else {
            super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
        }
    }
}
