package tech.konata.phosphate.module.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.src.Config;
import net.minecraft.util.Location;
import net.minecraft.world.WorldSettings;
import net.optifine.CustomColors;
import net.optifine.CustomItems;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render2DEvent;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Optional;

/**
 * @author IzumiiKonata
 * @since 6/27/2023 7:08 PM
 */
public class HotBar extends Module {

    public double damageDealt = 0;
    public Timer damageTimer = new Timer();
    double stackX;
    double hurtAnimPerc = 1.0;
    float hurtAnimAlpha = 0;
    double lastHealth;
    DecimalFormat df = new DecimalFormat("##.##");
    private double targetHealthWidth = 0, targetHealthPercent;
    private double targetFoodWidth = 0, targetFoodPercent;
    private double targetArmorWidth = 0, targetArmorPercent;
    private double targetExpWidth = 0;
    private double targetAbspWidth = 0, targetAbspPercent;
    private double targetHealthWidthLast = 0;

    public final ModeSetting<Style> style = new ModeSetting<>("Style", Style.Style1);
    public final BooleanSetting showPercent = new BooleanSetting("Show Percent", false);

    public enum Style {
        Style1,
        Style1ButVanillaItem
    }

    public final NumberSetting<Integer> width = new NumberSetting<>("Boarder Width", 30, 24, 40, 1, () -> this.style.getValue() == Style.Style1);

    @Handler
    public void onRender2D(Render2DEvent event) {

        if (this.style.getValue() == Style.Style1 || this.style.getValue() == Style.Style1ButVanillaItem) {
            this.renderStyle1();
        }

    }

    private void renderStyle1() {

        double val = (width.getValue().doubleValue() - width.getMinimum()) / (width.getMaximum() - width.getMinimum());

        double w = 182 * (GlobalSettings.FIXED_SCALE.getValue() ? RenderSystem.getScaleFactor() : 1);
        double h = 22 * (GlobalSettings.FIXED_SCALE.getValue() ? RenderSystem.getScaleFactor() : 1);

        boolean nativeHotBar = this.style.getValue() == Style.Style1ButVanillaItem;

        double hotbarItemSize = width.getValue();

        double hotbarItemSpacing = 8;
        double hotbarWidth = nativeHotBar ? w : hotbarItemSpacing * 8 + hotbarItemSize * 9;

        double hotbarItemWidth = ((hotbarWidth - (hotbarItemSpacing * 8)) / 9);

        double hotbarX = RenderSystem.getWidth() / 2 - hotbarWidth / 2;
        double hotbarY = nativeHotBar ? RenderSystem.getHeight() - h : RenderSystem.getHeight() - hotbarItemSize - 4 - 4 * val;

        double offsetX = hotbarX, offsetY = hotbarY;

        if (stackX < hotbarX + hotbarItemWidth / 2)
            stackX = hotbarX + (hotbarItemWidth + hotbarItemSpacing) * mc.thePlayer.inventory.currentItem + hotbarItemWidth / 2;

        if (this.style.getValue() != Style.Style1ButVanillaItem) {

            double finalOffsetX1 = offsetX;
            NORMAL.add(() -> {
                roundedRect(finalOffsetX1, offsetY, hotbarWidth, hotbarItemSize, 6, new Color(0, 0, 0, 120));
            });

            BLUR.add(() -> {
                roundedRect(finalOffsetX1, offsetY, hotbarWidth, hotbarItemSize, 6, Color.BLACK);
            });

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[i];

                if (i == mc.thePlayer.inventory.currentItem) {
                    stackX = Interpolations.interpBezier(stackX, offsetX + hotbarItemWidth / 2, 0.4);

                    NORMAL.add(() -> {
//                        roundedOutlineAccentColor(stackX - hotbarItemSize * 0.5, offsetY, hotbarItemSize, hotbarItemSize, 8, 0.5 + val * 1, 255);
                        roundedRect(stackX - hotbarItemSize * 0.5, offsetY, hotbarItemSize, hotbarItemSize, 6, new Color(255, 255, 255, 80));
                    });

                    if (stack != null) {
                        NORMAL.add(() -> {
                            FontManager.pf16.drawCenteredString(stack.getDisplayName(), stackX, offsetY - 10 - FontManager.pf16.getHeight(), hexColor(255, 255, 255, 255));
                            String itemDesc = "";
                            Item item = stack.getItem();
                            if (item instanceof ItemSword) {
                                itemDesc = "+" + this.getSwordDamage(stack) + " " + I18n.format("attribute.name.generic.attackDamage");
                            } else {

                                CreativeTabs creativeTab = item.getCreativeTab();

                                if (creativeTab != null) {
                                    itemDesc = I18n.format("itemGroup." + creativeTab.getTabLabel());
                                }
                            }

                            FontManager.pf12.drawCenteredString(itemDesc, stackX, offsetY - 7, hexColor(255, 255, 255, 180));
                        });
                    }
                }

                if (stack != null) {
                    double finalOffsetX2 = offsetX;
                    NORMAL.add(() -> {
                        this.renderItem(stack, finalOffsetX2 + 4 + val * 8, offsetY + 4 + val * 7, 0.5f + val * 0.35f);

                        if (stack.stackSize != 1) {
                            FontManager.pf12.drawString(String.valueOf(stack.stackSize), finalOffsetX2 + hotbarItemWidth - val * 10 - (1 - val) * 4, offsetY + hotbarItemWidth - val * 10 - (1 - val) * 4, -1);
                        }
                    });

                    if (stack.isItemDamaged()) {

                        double finalOffsetX = offsetX;
                        NORMAL.add(() -> {
                            double maxDamage = stack.getMaxDamage();
                            double damageBarWidth = 16 + val * 8;
                            double damageWidth = stack.getItemDamage() / maxDamage * damageBarWidth;

                            Rect.draw(finalOffsetX + hotbarItemWidth / 2.0 - damageBarWidth / 2.0, offsetY + hotbarItemSize - 3 - val * 3, damageBarWidth, 1, RenderSystem.hexColor(0, 0, 0), Rect.RectType.EXPAND);
                            Rect.draw(finalOffsetX + hotbarItemWidth / 2.0 - damageBarWidth / 2.0, offsetY + hotbarItemSize - 3 - val * 3, damageBarWidth - damageWidth, 1, RenderSystem.hexColor(0, 255, 0), Rect.RectType.EXPAND);
                        });
                    }
                }


                offsetX += hotbarItemWidth + hotbarItemSpacing;
            }
        }

        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SURVIVAL || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE) {

            double barsY = nativeHotBar ? hotbarY : hotbarY - 14;
            this.drawHealthBar(hotbarX, barsY, hotbarWidth);
            this.drawFoodBar(hotbarX, barsY, hotbarWidth);
            if (mc.thePlayer.getTotalArmorValue() > 0)
                this.drawArmorBar(hotbarX, barsY, hotbarWidth);

            if (mc.thePlayer.getAbsorptionAmount() > 0)
                this.drawAbspBar(hotbarX, barsY, hotbarWidth);

            this.drawExpBar(hotbarX, barsY, hotbarWidth);

        }
    }

    public HotBar() {
        super("Hot Bar", Category.RENDER);
    }

    private void drawExpBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double expBarHeight = 12;

        int y = 16;

        double expBarWidth = mc.thePlayer.experience * hotbarWidth;

        targetExpWidth = Interpolations.interpBezier(targetExpWidth, expBarWidth, 0.4f);

        if (Double.isNaN(targetExpWidth))
            targetExpWidth = expBarWidth;

        BLUR.add(() -> {
            roundedRect(hotbarX, hotbarY - y, hotbarWidth, expBarHeight, 3, Color.BLACK);
        });

        NORMAL.add(() -> {

            roundedRect(hotbarX, hotbarY - y, hotbarWidth, expBarHeight, 3, new Color(0, 0, 0, 100));

            roundedRectAccentColor(hotbarX, hotbarY - y, targetExpWidth, expBarHeight, 3);

            if (mc.thePlayer.experienceLevel > 0) {
                int k1 = RenderSystem.hexColor(128, 255, 32);

                if (Config.isCustomColors()) {
                    k1 = RenderSystem.reAlpha(CustomColors.getExpBarTextColor(k1), 1.0f);
                }

                String s = String.valueOf(this.mc.thePlayer.experienceLevel);

                FontManager.pf16.drawOutlineCenteredString(s, hotbarX + hotbarWidth * 0.5, hotbarY - y + expBarHeight * 0.5 - FontManager.pf16.getHeight() * 0.5, k1, RenderSystem.hexColor(0, 0, 0));
            }
        });

    }

    private void drawHealthBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double healthBarWidth = (hotbarWidth - 8) / 2;
        double healthBarHeight = 12;
        double totalHealth = mc.thePlayer.getMaxHealth();
        double healthWidth = mc.thePlayer.getHealth() / totalHealth * healthBarWidth;

        hurtAnimPerc = Interpolations.interpBezier(hurtAnimPerc, (double) mc.thePlayer.hurtTime / (mc.thePlayer.maxHurtTime + 0.00001), 0.25);
        hurtAnimAlpha = (float) Interpolations.interpBezier(hurtAnimAlpha, hurtAnimPerc * 120 * RenderSystem.DIVIDE_BY_255, 0.4f);

        if (Double.isNaN(targetHealthWidth)) {
            targetHealthWidth = healthWidth;
        }

        if (mc.thePlayer.getHealth() != lastHealth) {
            double delta = lastHealth - mc.thePlayer.getHealth();
            if (delta >= 0)
                damageDealt = delta;
            lastHealth = mc.thePlayer.getHealth();
            damageTimer.reset();
        }

        if (damageTimer.isDelayed(1000)) {
            lastHealth = mc.thePlayer.getHealth();
            damageDealt = 0;
            damageTimer.reset();
        }

        targetHealthWidth = Interpolations.interpBezier(targetHealthWidth, healthWidth, 0.4f);
        if (targetHealthWidth <= healthWidth + 0.1)
            targetHealthWidthLast = Interpolations.interpBezier(targetHealthWidthLast, targetHealthWidth, 0.2f);

        double offsetY = hotbarY - 32;

        BLUR.add(() -> {
            roundedRect(hotbarX, offsetY, healthBarWidth, healthBarHeight, 3, Color.BLACK);
        });

        NORMAL.add(() -> {
            roundedRect(hotbarX, offsetY, healthBarWidth, healthBarHeight, 3, new Color(0, 0, 0, 100));
            roundedRect(hotbarX, offsetY, targetHealthWidthLast, healthBarHeight, 3, new Color(255, 100, 80, ((int) (hurtAnimAlpha * 255))));

            roundedRect(hotbarX, offsetY, targetHealthWidth, healthBarHeight, 3, new Color(RenderSystem.reAlpha(getHealthColor(targetHealthPercent * 0.01), 100), true));

            targetHealthPercent = Interpolations.interpBezierApprox(targetHealthPercent, ((mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) * 100), 0.15);

            String percentText = (int) targetHealthPercent + "%";

            if (hurtAnimAlpha > 3 * RenderSystem.DIVIDE_BY_255) {
                double dealtY = offsetY + healthBarHeight / 2.0 - FontManager.pf16.getHeight() / 2.0;

                FontManager.pf16.drawString("-" + df.format(damageDealt), hotbarX + 2 + targetHealthWidthLast + ((targetHealthWidth - (FontManager.pf16.getStringWidth(percentText) + 4) < 0) ? (FontManager.pf16.getStringWidth(percentText) + 2) : 0), dealtY, new Color(255, 0, 0, ((int) (hurtAnimAlpha * 255)) * 2).getRGB());
            }

            if (showPercent.getValue()) {

                double percentX;

                if (targetHealthWidth - (FontManager.pf14.getStringWidth(percentText) + healthBarHeight + 2) < 0) {
                    percentX = hotbarX + targetHealthWidth + 4;
                } else {
                    percentX = hotbarX + targetHealthWidth - FontManager.pf16.getStringWidth(percentText);
                }

                FontManager.pf14bold.drawString(percentText, percentX, offsetY + healthBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));
            }

            SVGImage.drawKeepState(Location.of(Phosphate.NAME + "/textures/hotbar/health.svg"), hotbarX + 2, offsetY + 1, healthBarHeight - 2, healthBarHeight - 1);

        });

    }

    private void drawFoodBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double foodBarWidth = (hotbarWidth - 8) / 2;
        double foodBarHeight = 12;
        double totalFood = 20;
        double foodWidth = mc.thePlayer.getFoodStats().getFoodLevel() / totalFood * foodBarWidth;

        if (Double.isNaN(targetFoodWidth)) {
            targetFoodWidth = foodWidth;
        }

        double offsetY = hotbarY - 31.5;

        targetFoodWidth = Interpolations.interpBezier(targetFoodWidth, foodWidth, 0.4f);

//        BLOOM.add(() -> {
//            Rect.draw(hotbarX + foodBarWidth + 8, hotbarY - 30, foodBarWidth, foodBarHeight, hexColor(0, 0, 0, 100), Rect.RectType.EXPAND);
//        });

        BLUR.add(() -> {
            roundedRect(hotbarX + foodBarWidth + 8, offsetY, foodBarWidth, foodBarHeight, 3, Color.BLACK);
        });

        NORMAL.add(() -> {
//            Rect.draw(hotbarX + foodBarWidth + 8, offsetY, foodBarWidth, foodBarHeight, new Color(0, 0, 0, 50).getRGB(), Rect.RectType.EXPAND);

            roundedRect(hotbarX + foodBarWidth + 8, offsetY, foodBarWidth, foodBarHeight, 3, new Color(0, 0, 0, 100));

            roundedRect(hotbarX + foodBarWidth + 8, offsetY, targetFoodWidth, foodBarHeight, 3, new Color(255, 98, 0, 180));

            targetFoodPercent = Interpolations.interpBezierApprox(targetFoodPercent, ((mc.thePlayer.getFoodStats().getFoodLevel() / totalFood) * 100), 0.15);

            if (showPercent.getValue()) {

                String percentText = (int) targetFoodPercent + "%";

                double percentX;

                if (targetFoodWidth - (FontManager.pf14.getStringWidth(percentText) + foodBarHeight + 2) < 0) {
                    percentX = hotbarX + foodBarWidth + 8 + targetFoodWidth + 4;
                } else {
                    percentX = hotbarX + foodBarWidth + 8 + targetFoodWidth - 2 - FontManager.pf14.getStringWidth(percentText);
                }

                FontManager.pf14.drawString(percentText, percentX, offsetY + foodBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));
            }

            SVGImage.drawKeepState(Location.of(Phosphate.NAME + "/textures/hotbar/hunger.svg"), hotbarX + foodBarWidth + 11, offsetY + 1, foodBarHeight - 2, foodBarHeight - 2);
        });
    }

    private void drawArmorBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double armorBarWidth = (hotbarWidth - 8) / 2;
        double armorBarHeight = 12;
        double totalArmor = 20;
        double armorWidth = mc.thePlayer.getTotalArmorValue() / totalArmor * armorBarWidth;

        if (Double.isNaN(targetArmorWidth)) {
            targetArmorWidth = armorWidth;
        }

        targetArmorWidth = Interpolations.interpBezier(targetArmorWidth, armorWidth, 0.4f);

        double offsetY = mc.thePlayer.getAbsorptionAmount() > 0 ? hotbarY - 52: hotbarY - 36;

        BLUR.add(() -> {
            roundedRect(hotbarX, offsetY - armorBarHeight, armorBarWidth, armorBarHeight, 3, Color.BLACK);
        });

        NORMAL.add(() -> {
            roundedRect(hotbarX, offsetY - armorBarHeight, armorBarWidth, armorBarHeight, 3, new Color(0, 0, 0, 100));

            roundedRect(hotbarX, offsetY - armorBarHeight, targetArmorWidth, armorBarHeight, 3, new Color(0, 159, 255, 150));

            targetArmorPercent = Interpolations.interpBezierApprox(targetArmorPercent, ((mc.thePlayer.getTotalArmorValue() / totalArmor) * 100), 0.15);

            if (showPercent.getValue()) {

                String percentText = (int) targetArmorPercent + "%";

                double percentX;

                if (targetArmorWidth - (FontManager.pf14.getStringWidth(percentText) + armorBarHeight + 2) < 0) {
                    percentX = hotbarX + targetArmorWidth + 2;
                } else {
                    percentX = hotbarX + targetArmorWidth - FontManager.pf16.getStringWidth(percentText);
                }

                FontManager.pf14.drawString(percentText, percentX, offsetY - armorBarHeight + armorBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));
            }

            SVGImage.drawKeepState(Location.of(Phosphate.NAME + "/textures/hotbar/armor.svg"), hotbarX + 2, offsetY - armorBarHeight + 1.5, armorBarHeight - 2, armorBarHeight - 3);

        });
    }

    private void drawAbspBar(double hotbarX, double hotbarY, double hotbarWidth) {
        double abspBarWidth = (hotbarWidth - 8) / 2;
        double abspBarHeight = 12;
        double totalAbsp = mc.thePlayer.maxAbsp;
        double abspWidth = mc.thePlayer.getAbsorptionAmount() / totalAbsp * abspBarWidth;

        if (Double.isNaN(targetAbspWidth) || Double.isInfinite(targetAbspWidth)) {
            targetAbspWidth = abspWidth;
        }

        if (Double.isNaN(targetAbspPercent) || Double.isInfinite(targetAbspPercent)) {
            targetAbspPercent = (mc.thePlayer.getAbsorptionAmount() / totalAbsp) * 100;
        }

        targetAbspWidth = Interpolations.interpBezier(targetAbspWidth, abspWidth, 0.4f);

        double offsetY = hotbarY - 36;

        BLUR.add(() -> {
            roundedRect(hotbarX, offsetY - abspBarHeight, abspBarWidth, abspBarHeight, 3, Color.BLACK);
        });

        NORMAL.add(() -> {
            roundedRect(hotbarX, offsetY - abspBarHeight, abspBarWidth, abspBarHeight, 3, new Color(0, 0, 0, 100));

            roundedRect(hotbarX, offsetY - abspBarHeight, targetAbspWidth, abspBarHeight, 3, new Color(212, 175, 55));
            targetAbspPercent = Interpolations.interpBezierApprox(targetAbspPercent, ((mc.thePlayer.getAbsorptionAmount() / totalAbsp) * 100), 0.15);

            if (showPercent.getValue()) {

                String percentText = (int) targetAbspPercent + "%";

                double percentX;

                if (targetAbspWidth - (FontManager.pf14.getStringWidth(percentText) + 4) < 0) {
                    percentX = hotbarX + targetAbspWidth + 2;
                } else {
                    percentX = hotbarX + targetAbspWidth - FontManager.pf16.getStringWidth(percentText);
                }

                FontManager.pf14.drawString(percentText, percentX, offsetY - abspBarHeight + abspBarHeight / 2.0 - FontManager.pf14.getHeight() / 2.0, RenderSystem.reAlpha(-1, 0.8f));
            }

//            SVGImage.drawKeepState(Location.of(Phosphate.NAME + "/textures/hotbar/absorb.svg"), hotbarX + 2.5, offsetY - abspBarHeight + 1.5, abspBarHeight - 3, abspBarHeight - 3.5);

        });
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

    private double getSwordDamage(final ItemStack itemStack) {
        double damage = 0.0;
        final Optional<AttributeModifier> attributeModifier = itemStack.getAttributeModifiers().values().stream().findFirst();
        if (attributeModifier.isPresent()) {
            damage = attributeModifier.get().getAmount();
        }
        return damage + EnchantmentHelper.getModifierForCreature(itemStack, EnumCreatureAttribute.UNDEFINED);
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
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getRenderItem().setupGuiTransform(x, y, ibakedmodel.isGui3d());

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

    public void renderItem(ItemStack stack, IBakedModel model, double scale) {
        if (stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);

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
                mc.getRenderItem().renderModel(model, stack);

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

            mc.thePlayer.getCurrentEquippedItem();
        }
    }
}
