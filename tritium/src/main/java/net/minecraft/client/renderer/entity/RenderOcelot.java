package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.util.Location;

public class RenderOcelot extends RenderLiving<EntityOcelot> {
    private static final Location blackOcelotTextures = Location.of("textures/entity/cat/black.png");
    private static final Location ocelotTextures = Location.of("textures/entity/cat/ocelot.png");
    private static final Location redOcelotTextures = Location.of("textures/entity/cat/red.png");
    private static final Location siameseOcelotTextures = Location.of("textures/entity/cat/siamese.png");

    public RenderOcelot(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(EntityOcelot entity) {
        return switch (entity.getTameSkin()) {
            case 1 -> blackOcelotTextures;
            case 2 -> redOcelotTextures;
            case 3 -> siameseOcelotTextures;
            default -> ocelotTextures;
        };
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityOcelot entitylivingbaseIn, float partialTickTime) {
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);

        if (entitylivingbaseIn.isTamed()) {
            GlStateManager.scale(0.8F, 0.8F, 0.8F);
        }
    }
}
