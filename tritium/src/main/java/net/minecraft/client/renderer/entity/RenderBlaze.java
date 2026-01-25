package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBlaze;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.Location;

public class RenderBlaze extends RenderLiving<EntityBlaze> {
    private static final Location blazeTextures = Location.of("textures/entity/blaze.png");

    public RenderBlaze(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBlaze(), 0.5F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(EntityBlaze entity) {
        return blazeTextures;
    }
}
