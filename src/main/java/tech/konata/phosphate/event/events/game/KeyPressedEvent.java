package tech.konata.phosphate.event.events.game;

import lombok.Getter;
import tech.konata.phosphate.event.eventapi.Event;

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
