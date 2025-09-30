package tritium.module.impl.render.blockanimations;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import tritium.module.impl.render.BlockAnimations;
import tritium.module.submodule.SubModule;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.BlockAnimationEvent;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:53 PM
 */
public class DickPunch extends SubModule<BlockAnimations> {

    public DickPunch() {
        super("DickPunch");
    }

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        this.transformFirstPersonItem(event.swingProgress);
//        this.getModule().doBlockTransformations();
    }

    ;

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    private void transformFirstPersonItem(float swingProgress) {
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        GlStateManager.translate(0.0F, -0.52F, -0.71999997F + f * -0.5);
        GlStateManager.rotate(160.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-15.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-100.0F, -1.0F, 0.0F, 1.0F);
        GlStateManager.rotate(10.0F, 0.0F, 1.0F, 1.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }


}
