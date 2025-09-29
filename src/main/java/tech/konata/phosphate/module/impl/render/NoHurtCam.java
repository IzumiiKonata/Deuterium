package tech.konata.phosphate.module.impl.render;

import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.HurtCamEvent;
import tech.konata.phosphate.module.Module;

/**
 * @author IzumiiKonata
 * @since 2024/8/25 16:28
 */
public class NoHurtCam extends Module {

    public NoHurtCam() {
        super("NoHurtCam", Category.RENDER);
    }

    @Handler
    public void onHurtCam(final HurtCamEvent event) {
        event.setCancelled();
    }

}
