package tech.konata.phosphate.event.events.rendering;

import lombok.AllArgsConstructor;
import tech.konata.phosphate.event.eventapi.EventCancellable;

@AllArgsConstructor
public class BlockAnimationEvent extends EventCancellable {
    public float equipProgress, swingProgress, pitch, yaw;
}
