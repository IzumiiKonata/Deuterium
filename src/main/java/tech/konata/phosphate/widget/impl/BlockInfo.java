package tech.konata.phosphate.widget.impl;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.optifine.CustomItems;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.utils.timing.Timer;
import tech.konata.phosphate.widget.Widget;

import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2024/11/2 17:00
 */
public class BlockInfo extends Widget {

    public BlockInfo() {
        super("BlockInfo");
    }

    IBlockState lastHoveredBlock = null;

    boolean closing = true;

    Animation scaleAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(250));
    Timer closeTimer = new Timer();

    @Override
    public void onRender(boolean editing) {

//        scaleAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(250));

        if (editing) {
            lastHoveredBlock = Blocks.grass.getDefaultState();
            closing = false;
        } else {
            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {

                IBlockState blockState = mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos());
                Block block = blockState.getBlock();
                if (!block.equals(Blocks.portal) && !block.equals(Blocks.end_portal)) {
                    lastHoveredBlock = blockState;
                    closing = false;
                } else {
                    if (!closing) {
                        closing = true;
                        closeTimer.reset();
                    }
                }

            } else {
                if (!closing) {
                    closing = true;
                    closeTimer.reset();
                }
            }
        }

        scaleAnimation.run(closing && closeTimer.isDelayed(250) ? 0 : 1);


        if (lastHoveredBlock != null) {
            NORMAL.add(() -> {

                GlStateManager.pushMatrix();
                this.scaleAtPos(this.getX() + this.getWidth() * 0.5, this.getY() + this.getHeight() * 0.5, scaleAnimation.getValue());

                this.renderStyledBackground(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 8);

                Block block = lastHoveredBlock.getBlock();
                this.renderItem(new ItemStack(block), (int) (this.getX() + 30), (int) (this.getY() + 28), 1);

                CFontRenderer fr = FontManager.pf25bold;
                fr.drawString(block.getLocalizedName(), this.getX() + 73, this.getY() + 16, -1);

                String harvest = "";
                if(mc.thePlayer.inventory.getCurrentItem() != null && (!(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock))) {
                    harvest = "Harvest: " + (mc.thePlayer.inventory.getCurrentItem().getStrVsBlock(block) > 1 ? EnumChatFormatting.GREEN + "true" : EnumChatFormatting.RED + "false");
                }else {
                    harvest = "Harvest: " + EnumChatFormatting.RED + "false";
                }

                FontManager.pf20.drawString(harvest, this.getX() + 73, this.getY() + 20 + fr.getHeight(), -1);

                GlStateManager.popMatrix();

                double width = Math.max(160, 73 + fr.getStringWidth(block.getLocalizedName() + 16));
                this.setWidth(width);
                this.setHeight(70);
            });

        }


    }

    private void renderItem(ItemStack itemStack, double x, double y, double scale) {

        GlStateManager.pushMatrix();

        RenderHelper.enableGUIStandardItemLighting();
        this.renderItemIntoGUI(itemStack, x, y, scale);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    public void renderItemIntoGUI(ItemStack stack, double x, double y, double scale) {
        mc.getRenderItem().renderItemGui = true;
        IBakedModel ibakedmodel = mc.getRenderItem().getItemModelMesher().getItemModel(stack);
        GlStateManager.pushMatrix();
        mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
        mc.getRenderItem().textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
//        RenderSystem.linearFilter();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.setupGuiTransform(x, y, ibakedmodel.isGui3d());

        ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);

        this.renderItem(stack, ibakedmodel, scale);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();

        if (mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture) == null) {
            mc.getTextureManager().loadTickableTexture(TextureMap.locationBlocksTexture, mc.getTextureMapBlocks());
        }

        mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
        mc.getRenderItem().textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        mc.getRenderItem().renderItemGui = false;
    }

    public void setupGuiTransform(double xPosition, double yPosition, boolean isGui3d) {
        GlStateManager.translate(xPosition, yPosition, 100.0F + mc.getRenderItem().zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        if (isGui3d) {
            GlStateManager.scale(40.0F, 40.0F, 40.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.enableLighting();
        } else {
            GlStateManager.scale(64.0F, 64.0F, 64.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.disableLighting();
        }
    }

    public void renderItem(ItemStack stack, IBakedModel model, double scale) {
        if (stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F + scale, 0.5F + scale, 0.5F + scale);

            if (model.isBuiltInRenderer()) {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                TileEntityItemStackRenderer.instance.renderByItem(stack);
            } else {
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);

                if (Config.isCustomItems()) {
                    model = CustomItems.getCustomItemModel(stack, model, mc.getRenderItem().modelLocation, false);
                }

                mc.getRenderItem().renderModelHasEmissive = false;
                //mc.thePlayer.getCurrentEquippedItem();//                    current.framebufferClear();
//                    current.bindFramebuffer(true);
                mc.getRenderItem().renderModel(model, stack);
                // mc.thePlayer.getCurrentEquippedItem();//                    current.unbindFramebuffer();

                if (mc.getRenderItem().renderModelHasEmissive) {
                    float f = OpenGlHelper.lastBrightnessX;
                    float f1 = OpenGlHelper.lastBrightnessY;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, f1);
                    mc.getRenderItem().renderModelEmissive = true;
                    mc.getRenderItem().renderModel(model, stack);
                    mc.getRenderItem().renderModelEmissive = false;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
                }

                if (stack.hasEffect() && (!Config.isCustomItems() || !CustomItems.renderCustomEffect(mc.getRenderItem(), stack, model))) {
                    mc.getRenderItem().renderEffect(model);
                }
            }

            GlStateManager.popMatrix();

            mc.thePlayer.getCurrentEquippedItem();//                current.framebufferRender(mc.displayWidth, mc.displayHeight);
//                GlowShader.renderGlow(current.framebufferTexture, 5, 0.6f, Color.YELLOW.getRGB(), 1);
        }
    }

}
