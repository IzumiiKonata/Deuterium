package tech.konata.phosphate.event.events.player;

import lombok.AllArgsConstructor;
import tech.konata.phosphate.event.eventapi.Event;

@AllArgsConstructor
public class MoveEntityRotationEvent extends Event {
    public float rotationYaw;
}
