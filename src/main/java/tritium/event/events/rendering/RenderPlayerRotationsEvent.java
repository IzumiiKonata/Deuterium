package tritium.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.Event;

@Setter
@Getter
@AllArgsConstructor
public class RenderPlayerRotationsEvent extends Event {

    public float rotationYaw, rotationPitch;
}
