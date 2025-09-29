package tech.konata.phosphate.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.Event;

@Setter
@Getter
@AllArgsConstructor
public class RenderPlayerRotationsEvent extends Event {

    public float rotationYaw, rotationPitch;
}
