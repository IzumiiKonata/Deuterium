package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerSheepWool;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.Location;

public class RenderSheep extends RenderLiving<EntitySheep> {
    private static final Location shearedSheepTextures = Location.of("textures/entity/sheep/sheep.png");

    public RenderSheep(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
        this.addLayer(new LayerSheepWool(this));
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(EntitySheep entity) {
        return shearedSheepTextures;
    }
}
