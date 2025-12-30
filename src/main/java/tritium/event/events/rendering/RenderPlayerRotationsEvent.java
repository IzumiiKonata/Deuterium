package tritium.event.events.rendering;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.Event;

@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenderPlayerRotationsEvent extends Event {

    private static final RenderPlayerRotationsEvent INSTANCE = new RenderPlayerRotationsEvent(0, 0);

    public float rotationYaw, rotationPitch;

    public static RenderPlayerRotationsEvent of(float rotationYaw, float rotationPitch) {
        INSTANCE.rotationYaw = rotationYaw;
        INSTANCE.rotationPitch = rotationPitch;
        return INSTANCE;
    }
}
