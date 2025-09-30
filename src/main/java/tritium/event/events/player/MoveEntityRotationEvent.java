package tritium.event.events.player;

import lombok.AllArgsConstructor;
import tritium.event.eventapi.Event;

@AllArgsConstructor
public class MoveEntityRotationEvent extends Event {
    public float rotationYaw;
}
