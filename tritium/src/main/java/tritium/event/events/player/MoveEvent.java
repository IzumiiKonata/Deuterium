package tritium.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.Event;

@Setter
@Getter
@AllArgsConstructor
public class MoveEvent extends Event {
    public double x, y, z;
}
