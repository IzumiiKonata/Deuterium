package tritium.event.events.game;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * @since 5/1/2023 5:50 PM
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyPressedEvent extends Event {

    private static final KeyPressedEvent INSTANCE = new KeyPressedEvent(0);

    @Getter
    private int keyCode;

    public static KeyPressedEvent of(int keyCode) {
        INSTANCE.keyCode = keyCode;
        return INSTANCE;
    }

}
