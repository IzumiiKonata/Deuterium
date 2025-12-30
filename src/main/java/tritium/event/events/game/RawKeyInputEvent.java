package tritium.event.events.game;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RawKeyInputEvent extends Event {

    public static final RawKeyInputEvent INSTANCE = new RawKeyInputEvent(-1);

    @Getter
    private int keyCode;

    public static RawKeyInputEvent of(int keyCode) {
        INSTANCE.keyCode = keyCode;
        return INSTANCE;
    }

}