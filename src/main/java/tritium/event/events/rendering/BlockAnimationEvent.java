package tritium.event.events.rendering;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import tritium.event.eventapi.EventCancellable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockAnimationEvent extends EventCancellable {

    private static final BlockAnimationEvent INSTANCE = new BlockAnimationEvent(0, 0, 0, 0);

    public float equipProgress, swingProgress, pitch, yaw;

    public static BlockAnimationEvent of(float equipProgress, float swingProgress, float pitch, float yaw) {
        INSTANCE.equipProgress = equipProgress;
        INSTANCE.swingProgress = swingProgress;
        INSTANCE.pitch = pitch;
        INSTANCE.yaw = yaw;
        return INSTANCE;
    }
}
