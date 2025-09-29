package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.util.Location;

public class RenderSilverfish extends RenderLiving<EntitySilverfish> {
    private static final Location silverfishTextures = Location.of("textures/entity/silverfish.png");

    public RenderSilverfish(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelSilverfish(), 0.3F);
    }

    protected float getDeathMaxRotation(EntitySilverfish entityLivingBaseIn) {
        return 180.0F;
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(EntitySilverfish entity) {
        return silverfishTextures;
    }
}
