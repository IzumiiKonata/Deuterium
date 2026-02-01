package tritium.module.impl.render.blockanimations;

import net.minecraft.client.renderer.GlStateManager;
import tritium.module.impl.render.BlockAnimations;
import tritium.module.submodule.SubModule;
import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.BlockAnimationEvent;
import tritium.settings.NumberSetting;

public class Screw extends SubModule<BlockAnimations> {

    public Screw() {
        super("Screw");
    }

    public final NumberSetting<Double> speed = new NumberSetting<>("Screw Speed", 10.0, 1.0, 50.0, 1.0);

    int ticks = 0;

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        this.circle(event.swingProgress);
        this.getModule().doBlockTransformations();
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

    private void circle(final float swingProgress) {
        ++this.ticks;
        GlStateManager.translate(0.7f, -0.4f, -0.8f);
        GlStateManager.rotate(this.ticks * 0.2f * speed.getFloatValue(), 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(40.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(34.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(0.4, 0.4, 0.4);
    }
}
