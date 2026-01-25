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
public class Remix extends SubModule<BlockAnimations> {

    public Remix() {
        super("Remix");
    }

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        this.getModule().transformFirstPersonItem(event.equipProgress, 0.83f);
        this.getModule().doBlockTransformations();
        float remix = MathHelper.sin(MathHelper.sqrt_float(event.swingProgress) * 3.83f);
        GlStateManager.translate(-0.5f, 0.2f, 0.2f);
        GlStateManager.rotate(-remix * 0.0f, 0.0f, 0.0f, 0.0f);
        GlStateManager.rotate(-remix * 43.0f, 58.0f, 23.0f, 45.0f);
    }

    ;


}
