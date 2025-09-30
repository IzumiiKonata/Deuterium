package tritium.event.events.input;

import lombok.AllArgsConstructor;
import tritium.event.eventapi.Event;

@AllArgsConstructor
public class MouseXYChangeEvent extends Event {
    public double deltaX, deltaY;
}
