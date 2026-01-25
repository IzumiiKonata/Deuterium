package tritium.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.Event;

@Getter
@Setter
@AllArgsConstructor
public class MovementInputEvent extends Event {

    private float forward, strafe;

}
