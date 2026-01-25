package tritium.module.impl.render.blockanimations;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.BlockAnimationEvent;
import tritium.module.impl.render.BlockAnimations;
import tritium.module.submodule.SubModule;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:53 PM
 */
public class Lunar extends SubModule<BlockAnimations> {

    public Lunar() {
        super("Lunar");
    }

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        this.getModule().transformFirstPersonItem(0.2f, event.swingProgress);
        this.getModule().doBlockTransformations();
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
    }

    ;

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    /**
     * Translate and rotate the render for holding a block
     */
    private void doBlockTransformations() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

}
