package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.Location;

import java.util.Map;

public class RenderHorse extends RenderLiving<EntityHorse> {
    private static final Map<String, Location> field_110852_a = Maps.newHashMap();
    private static final Location whiteHorseTextures = Location.of("textures/entity/horse/horse_white.png");
    private static final Location muleTextures = Location.of("textures/entity/horse/mule.png");
    private static final Location donkeyTextures = Location.of("textures/entity/horse/donkey.png");
    private static final Location zombieHorseTextures = Location.of("textures/entity/horse/horse_zombie.png");
    private static final Location skeletonHorseTextures = Location.of("textures/entity/horse/horse_skeleton.png");

    public RenderHorse(RenderManager rendermanagerIn, ModelHorse model, float shadowSizeIn) {
        super(rendermanagerIn, model, shadowSizeIn);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityHorse entitylivingbaseIn, float partialTickTime) {
        float f = 1.0F;
        int i = entitylivingbaseIn.getHorseType();

        if (i == 1) {
            f *= 0.87F;
        } else if (i == 2) {
            f *= 0.92F;
        }

        GlStateManager.scale(f, f, f);
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(EntityHorse entity) {
        if (!entity.hasDifferentHorseType()) {
            switch (entity.getHorseType()) {
                case 0:
                default:
                    return whiteHorseTextures;

                case 1:
                    return donkeyTextures;

                case 2:
                    return muleTextures;

                case 3:
                    return zombieHorseTextures;

                case 4:
                    return skeletonHorseTextures;
            }
        } else {
            return this.getHorseTexture(entity);
        }
    }

    private Location getHorseTexture(EntityHorse horse) {
        String s = horse.getHorseTexture();

//        System.out.println(s);
//        System.out.println(Arrays.toString(horse.getVariantTexturePaths()));

        if (!horse.func_175507_cI()) {
            return null;
        } else {
            Location resourcelocation = field_110852_a.get(s);

            if (resourcelocation == null) {
                resourcelocation = Location.of(s);
                Minecraft.getMinecraft().getTextureManager().loadTexture(resourcelocation, new LayeredTexture(horse.getVariantTexturePaths()));
                field_110852_a.put(s, resourcelocation);
            }

            return resourcelocation;
        }
    }
}
