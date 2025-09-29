package tech.konata.phosphate.module.impl.render.blockanimations;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import tech.konata.phosphate.module.impl.render.BlockAnimations;
import tech.konata.phosphate.module.submodule.SubModule;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.BlockAnimationEvent;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:53 PM
 */
public class Rotate extends SubModule<BlockAnimations> {

    public Rotate() {
        super("Rotate");
    }

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        this.Random(event.swingProgress);
        this.getModule().doBlockTransformations();
    }

    ;

    public final NumberSetting<Double> speed = new NumberSetting<>("Rotate Speed", 10.0, 1.0, 50.0, 1.0);


    private float ticks = 0;

    private void Random(final float ignored) {
        this.ticks += (float) (RenderSystem.getFrameDeltaTime() * 5.0f);
        GlStateManager.translate(0.7D, -0.4000000059604645D, -0.800000011920929D);
        GlStateManager.rotate(50.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(50.0F, 0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(this.ticks * 0.2F * speed.getFloatValue(), 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-25.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4D, 0.4D, 0.4D);
    }

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
