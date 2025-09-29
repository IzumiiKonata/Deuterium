package tech.konata.phosphate.event.events.input;

import lombok.AllArgsConstructor;
import tech.konata.phosphate.event.eventapi.Event;

@AllArgsConstructor
public class MouseXYChangeEvent extends Event {
    public double deltaX, deltaY;
}
