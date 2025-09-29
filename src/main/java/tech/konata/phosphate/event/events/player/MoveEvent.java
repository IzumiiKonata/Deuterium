package tech.konata.phosphate.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.Event;

@Setter
@Getter
@AllArgsConstructor
public class MoveEvent extends Event {
    public double x, y, z;
}
