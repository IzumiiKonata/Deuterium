package tritium.event.events.player;

import lombok.Getter;
import tritium.event.eventapi.Event;

public class RespawnEvent extends Event {

    @Getter
    private static final RespawnEvent INSTANCE = new RespawnEvent();

}
