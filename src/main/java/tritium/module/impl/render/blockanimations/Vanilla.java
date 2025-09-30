package tritium.module.impl.render.blockanimations;

import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.BlockAnimationEvent;
import tritium.module.impl.render.BlockAnimations;
import tritium.module.submodule.SubModule;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:53 PM
 */
public class Vanilla extends SubModule<BlockAnimations> {

    public Vanilla() {
        super("Vanilla");
    }

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        this.getModule().transformFirstPersonItem(event.equipProgress, event.swingProgress);
        this.getModule().doBlockTransformations();
    }

    ;

}
