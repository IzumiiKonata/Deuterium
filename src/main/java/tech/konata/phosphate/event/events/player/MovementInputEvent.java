package tech.konata.phosphate.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.Event;

@Getter
@Setter
@AllArgsConstructor
public class MovementInputEvent extends Event {

    private float forward, strafe;

}
