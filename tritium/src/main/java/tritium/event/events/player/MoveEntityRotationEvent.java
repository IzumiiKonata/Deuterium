package tritium.event.events.player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import tritium.event.eventapi.Event;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveEntityRotationEvent extends Event {

    private static final MoveEntityRotationEvent INSTANCE = new MoveEntityRotationEvent(0);

    public float rotationYaw;

    public static MoveEntityRotationEvent of(float rotationYaw) {
        INSTANCE.rotationYaw = rotationYaw;
        return INSTANCE;
    }
}
