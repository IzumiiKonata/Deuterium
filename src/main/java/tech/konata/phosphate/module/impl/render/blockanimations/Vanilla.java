package tech.konata.phosphate.module.impl.render.blockanimations;

import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.BlockAnimationEvent;
import tech.konata.phosphate.module.impl.render.BlockAnimations;
import tech.konata.phosphate.module.submodule.SubModule;

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
