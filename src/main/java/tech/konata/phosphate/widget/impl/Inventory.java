package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.optifine.CustomItems;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.utils.player.InvUtils;
import tech.konata.phosphate.widget.Widget;

import java.awt.*;

public class Inventory extends Widget {

    public Inventory() {
        super("Inventory");
    }

    @Override
    public void onRender(boolean editing) {
        double width = 256, height = 88;

        boolean bFlagVanilla = GlobalSettings.HUD_STYLE.getValue() == GlobalSettings.HudStyle.Vanilla;

        if (!bFlagVanilla) {
            BLOOM.add(() -> {
                GlStateManager.pushMatrix();
                this.doScale();

                roundedRect(this.getX(), this.getY(), width, height, 3, new Color(0, 0, 0, 100));
                GlStateManager.popMatrix();
            });

            BLUR.add(() -> {
                GlStateManager.pushMatrix();
                this.doScale();

                roundedRect(this.getX(), this.getY(), width, height, 3, Color.WHITE);
                GlStateManager.popMatrix();
            });
        }

        NORMAL.add(() -> {

            GlStateManager.pushMatrix();
            this.doScale();

            if (bFlagVanilla) {
                Rect.draw(this.getX(), this.getY(), width, height, hexColor(0, 0, 0, 100), Rect.RectType.EXPAND);
            } else {
                roundedRect(this.getX(), this.getY(), width, height, 3, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, 100));
            }

            double offsetX = this.getX() + 4, offsetY = this.getY() + 4;
            double stackWidth = 24, stackHeight = 24;
            double spacing = 4;

            int count = 0;

            GlStateManager.enableBlend();

            for (ItemStack stack : InvUtils.getInventoryContent()) {
                GlStateManager.disableAlpha();

                if (bFlagVanilla) {
                    Rect.draw(offsetX, offsetY, stackWidth, stackHeight, hexColor(0, 0, 0, 20), Rect.RectType.EXPAND);
                } else {
                    roundedRect(offsetX, offsetY, stackWidth, stackHeight, 2, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, 50));
                }

                if (stack != null) {
                    if (stack.getItem() instanceof ItemBlock)
                        this.renderItem(stack, offsetX + 4, offsetY + 4, 0.25);
                    else
                        this.renderItem(stack, offsetX + 3.5, offsetY + 4, 0.25);

                    if (stack.stackSize != 1) {
                        FontManager.pf14.drawString(String.valueOf(stack.stackSize), offsetX + 20, offsetY + 20, -1);
                    }

                    if (stack.isItemDamaged()) {
                        double maxDamage = stack.getMaxDamage();
                        double damageBarWidth = 16;
                        double damageWidth = stack.getItemDamage() / maxDamage * damageBarWidth;

                        Rect.draw(offsetX + stackWidth / 2.0 - damageBarWidth / 2.0, offsetY + stackHeight - 1, damageBarWidth, 1.5, RenderSystem.hexColor(0, 0, 0), Rect.RectType.EXPAND);
                        Rect.draw(offsetX + stackWidth / 2.0 - damageBarWidth / 2.0, offsetY + stackHeight - 1, damageBarWidth - damageWidth, 1.5, RenderSystem.hexColor(0, 255, 0), Rect.RectType.EXPAND);
                    }
                }

                offsetX += stackWidth + spacing;
                count++;

                if (count > 8) {
                    offsetX = this.getX() + 4;
                    offsetY += stackHeight + spacing;
                    count = 0;
                }


            }

            this.setWidth(width);
            this.setHeight(height);

            GlStateManager.popMatrix();

        });
    }

    private void renderItem(ItemStack itemStack, double x, double y, double scale) {
        RenderItem ir = mc.getRenderItem();

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
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        setupGuiTransform(x, y, ibakedmodel.isGui3d());

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
//            current = RenderSystem.createFrameBuffer(current);
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
                if (stack == mc.thePlayer.getCurrentEquippedItem()) {
//                    current.framebufferClear();
//                    current.bindFramebuffer(true);
                }
                mc.getRenderItem().renderModel(model, stack);
                if (stack == mc.thePlayer.getCurrentEquippedItem()) {
//                    current.unbindFramebuffer();
                }

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

            if (stack == mc.thePlayer.getCurrentEquippedItem()) {
//                current.framebufferRender(mc.displayWidth, mc.displayHeight);
//                GlowShader.renderGlow(current.framebufferTexture, 5, 0.6f, Color.YELLOW.getRGB(), 1);
            }
        }
    }
}
