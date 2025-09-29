package tech.konata.phosphate.event.events.player;

import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * Date: 2025/1/29 22:38
 */
@Getter
@Setter
public class SafeWalkEvent extends Event {

    private boolean safeWalk = false;

}
