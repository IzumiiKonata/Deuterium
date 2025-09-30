package tritium.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

@Getter
@AllArgsConstructor
public class DisplayResizedEvent extends Event {
    private final int beforeWidth, beforeHeight, nowWidth, nowHeight;
}
