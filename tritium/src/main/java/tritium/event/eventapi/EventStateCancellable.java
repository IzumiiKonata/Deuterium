package tritium.event.eventapi;

import lombok.Getter;
import lombok.Setter;

/**
 * @author IzumiiKonata
 * @since 3/26/2023 7:34 AM
 */
public class EventStateCancellable extends EventCancellable {

    @Getter
    @Setter
    State state = State.PRE;

    public boolean isPre() {
        return state == State.PRE;
    }
}
