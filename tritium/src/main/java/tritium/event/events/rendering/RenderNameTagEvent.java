package tritium.event.events.rendering;

import lombok.Getter;
import tritium.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:16
 */
public class RenderNameTagEvent extends EventCancellable {

    @Getter
    private static final RenderNameTagEvent INSTANCE = new RenderNameTagEvent();

}
