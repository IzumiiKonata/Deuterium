package tritium.module.impl.render;

import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.HurtCamEvent;
import tritium.module.Module;

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
