package tritium.event.events.game;

import lombok.Getter;
import tritium.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * @since 5/1/2023 5:50 PM
 */
@Getter
public class KeyPressedEvent extends Event {

    private final int keyCode;

    public KeyPressedEvent(int keyCode) {
        this.keyCode = keyCode;
    }

}
