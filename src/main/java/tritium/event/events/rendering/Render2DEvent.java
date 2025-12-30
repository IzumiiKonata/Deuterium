package tritium.event.events.rendering;

import lombok.Getter;
import tritium.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class Render2DEvent extends Event {

    @Getter
    private static final Render2DEvent INSTANCE = new Render2DEvent();

}
