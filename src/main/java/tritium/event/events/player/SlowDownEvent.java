package tritium.event.events.player;

import lombok.Getter;
import tritium.event.eventapi.EventCancellable;

public class SlowDownEvent extends EventCancellable {

    private static final SlowDownEvent INSTANCE = new SlowDownEvent();

    public boolean shouldSlowDown = false;

    public static SlowDownEvent get() {
        INSTANCE.shouldSlowDown = false;
        return INSTANCE;
    }

}
