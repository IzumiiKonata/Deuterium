package tritium.event.events.game;

import lombok.Getter;
import tritium.event.eventapi.Event;

@Getter

public class RawKeyInputEvent extends Event {

    private final int keyCode;

    public RawKeyInputEvent(int keyCode) {
        this.keyCode = keyCode;
    }

}