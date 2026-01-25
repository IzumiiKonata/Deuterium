package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;

public class RenderRabbit extends RenderLiving<EntityRabbit> {
    private static final Location BROWN = Location.of("textures/entity/rabbit/brown.png");
    private static final Location WHITE = Location.of("textures/entity/rabbit/white.png");
    private static final Location BLACK = Location.of("textures/entity/rabbit/black.png");
    private static final Location GOLD = Location.of("textures/entity/rabbit/gold.png");
    private static final Location SALT = Location.of("textures/entity/rabbit/salt.png");
    private static final Location WHITE_SPLOTCHED = Location.of("textures/entity/rabbit/white_splotched.png");
    private static final Location TOAST = Location.of("textures/entity/rabbit/toast.png");
    private static final Location CAERBANNOG = Location.of("textures/entity/rabbit/caerbannog.png");

    public RenderRabbit(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected Location getEntityTexture(EntityRabbit entity) {
        String s = EnumChatFormatting.getTextWithoutFormattingCodes(entity.getName());

        if (s != null && s.equals("Toast")) {
            return TOAST;
        } else {
            switch (entity.getRabbitType()) {
                case 0:
                default:
                    return BROWN;

                case 1:
                    return WHITE;

                case 2:
                    return BLACK;

                case 3:
                    return WHITE_SPLOTCHED;

                case 4:
                    return GOLD;

                case 5:
                    return SALT;

                case 99:
                    return CAERBANNOG;
            }
        }
    }
}
