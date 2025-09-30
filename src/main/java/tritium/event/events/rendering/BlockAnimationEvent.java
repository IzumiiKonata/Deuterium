package tritium.event.events.rendering;

import lombok.AllArgsConstructor;
import tritium.event.eventapi.EventCancellable;

@AllArgsConstructor
public class BlockAnimationEvent extends EventCancellable {
    public float equipProgress, swingProgress, pitch, yaw;
}
