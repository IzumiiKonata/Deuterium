package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.Location;
import net.optifine.CustomItems;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public abstract class LayerArmorBase<T extends ModelBase> implements LayerRenderer<EntityLivingBase> {
    protected static final Location ENCHANTED_ITEM_GLINT_RES = Location.of("textures/misc/enchanted_item_glint.png");
    protected T modelLeggings;
    protected T modelArmor;
    private final RendererLivingEntity<?> renderer;
    private boolean skipRenderGlint;
    private static final Map<String, Location> ARMOR_TEXTURE_RES_MAP = Maps.newHashMap();

    public LayerArmorBase(RendererLivingEntity<?> rendererIn) {
        this.renderer = rendererIn;
        this.initArmor();
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 4);
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 3);
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 2);
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 1);
    }

    public boolean shouldCombineTextures() {
        return false;
    }

    private void renderLayer(EntityLivingBase entitylivingbaseIn, float p_177182_2_, float p_177182_3_, float partialTicks, float p_177182_5_, float p_177182_6_, float p_177182_7_, float scale, int armorSlot) {
        ItemStack itemstack = this.getCurrentArmor(entitylivingbaseIn, armorSlot);

        if (itemstack != null && itemstack.getItem() instanceof ItemArmor itemarmor) {
            T t = this.getArmorModel(armorSlot);
            t.setModelAttributes(this.renderer.getMainModel());
            t.setLivingAnimations(entitylivingbaseIn, p_177182_2_, p_177182_3_, partialTicks);

            this.setModelPartVisible(t, armorSlot);
            boolean leggings = this.isSlotForLeggings(armorSlot);

            if (!Config.isCustomItems() || !CustomItems.bindCustomArmorTexture(itemstack, leggings ? 2 : 1, null)) {
                this.renderer.bindTexture(this.getArmorResource(itemarmor, leggings));
            }

            float colorB = 1.0F;
            float colorG = 1.0F;
            float colorR = 1.0F;
            float alpha = 1.0F;

            switch (itemarmor.getArmorMaterial()) {
                case LEATHER:
                    int i = itemarmor.getColor(itemstack);
                    float f = (float) (i >> 16 & 255) / 255.0F;
                    float f1 = (float) (i >> 8 & 255) / 255.0F;
                    float f2 = (float) (i & 255) / 255.0F;
                    GlStateManager.color(colorR * f, colorG * f1, colorB * f2, alpha);
                    t.render(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, scale);

                    if (!Config.isCustomItems() || !CustomItems.bindCustomArmorTexture(itemstack, leggings ? 2 : 1, "overlay")) {
                        this.renderer.bindTexture(this.getArmorResource(itemarmor, leggings, "overlay"));
                    }

                case CHAIN:
                case IRON:
                case GOLD:
                case DIAMOND:
                    GlStateManager.color(colorR, colorG, colorB, alpha);
                    t.render(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, scale);
            }

            if (!RendererLivingEntity.SHADER_RENDERING && !this.skipRenderGlint && itemstack.isItemEnchanted() && (!Config.isCustomItems() || !CustomItems.renderCustomArmorEffect(entitylivingbaseIn, itemstack, t, p_177182_2_, p_177182_3_, partialTicks, p_177182_5_, p_177182_6_, p_177182_7_, scale))) {
                this.renderGlint(entitylivingbaseIn, t, p_177182_2_, p_177182_3_, partialTicks, p_177182_5_, p_177182_6_, p_177182_7_, scale);
            }
        }
    }

    public ItemStack getCurrentArmor(EntityLivingBase entitylivingbaseIn, int armorSlot) {
        return entitylivingbaseIn.getCurrentArmor(armorSlot - 1);
    }

    public T getArmorModel(int armorSlot) {
        return this.isSlotForLeggings(armorSlot) ? this.modelLeggings : this.modelArmor;
    }

    private boolean isSlotForLeggings(int armorSlot) {
        return armorSlot == 2;
    }

    private void renderGlint(EntityLivingBase entitylivingbaseIn, T modelbaseIn, float p_177183_3_, float p_177183_4_, float partialTicks, float p_177183_6_, float p_177183_7_, float p_177183_8_, float scale) {
        if (!Config.isShaders() || !Shaders.isShadowPass) {
            float f = (float) entitylivingbaseIn.ticksExisted + partialTicks;
            this.renderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);

            if (Config.isShaders()) {
                ShadersRender.renderEnchantedGlintBegin();
            }

            GlStateManager.enableBlend();
            GlStateManager.depthFunc(GL11.GL_EQUAL);
            GlStateManager.depthMask(false);
            float f1 = 0.5F;
            GlStateManager.color(f1, f1, f1, 1.0F);

            for (int i = 0; i < 2; ++i) {
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
                float f2 = 0.76F;
                GlStateManager.color(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                float f3 = 0.33333334F;
                GlStateManager.scale(f3, f3, f3);
                GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                modelbaseIn.render(entitylivingbaseIn, p_177183_3_, p_177183_4_, p_177183_6_, p_177183_7_, p_177183_8_, scale);
            }

            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.disableBlend();

            if (Config.isShaders()) {
                ShadersRender.renderEnchantedGlintEnd();
            }
        }
    }

    private Location getArmorResource(ItemArmor armor, boolean leggings) {
        return this.getArmorResource(armor, leggings, null);
    }

    private Location getArmorResource(ItemArmor armor, boolean forLeggings, String additionalIdentifier) {
//        String s = String.format("textures/models/armor/%s_layer_%d%s.png", armor.getArmorMaterial().getName(), forLeggings ? 2 : 1, additionalIdentifier == null ? "" : String.format("_%s", additionalIdentifier));
        String s = "textures/models/armor/" + armor.getArmorMaterial().getName() + "_layer_" + (forLeggings ? 2 : 1) + (additionalIdentifier == null ? "" : "_" + additionalIdentifier) + ".png";
        Location resourcelocation = ARMOR_TEXTURE_RES_MAP.get(s);

        if (resourcelocation == null) {
            resourcelocation = Location.of(s);
            ARMOR_TEXTURE_RES_MAP.put(s, resourcelocation);
        }

        return resourcelocation;
    }

    protected abstract void initArmor();

    protected abstract void setModelPartVisible(T model, int armorSlot);
}
