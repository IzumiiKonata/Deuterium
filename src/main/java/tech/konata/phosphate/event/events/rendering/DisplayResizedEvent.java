package tech.konata.phosphate.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.konata.phosphate.event.eventapi.Event;

@Getter
@AllArgsConstructor
public class DisplayResizedEvent extends Event {
    private final int beforeWidth, beforeHeight, nowWidth, nowHeight;
}
