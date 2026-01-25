package tritium.event.events.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.EventState;

/**
 * @author IzumiiKonata
 * @since 4/15/2023 7:40 PM
 */
@Getter
@AllArgsConstructor
public class TickEvent extends EventState {

    private int elapsedTicks;

}

