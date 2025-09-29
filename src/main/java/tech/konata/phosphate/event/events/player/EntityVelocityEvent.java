package tech.konata.phosphate.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.konata.phosphate.event.eventapi.EventState;

/**
 * @author IzumiiKonata
 * Date: 2025/2/4 18:03
 */
@AllArgsConstructor
public class EntityVelocityEvent extends EventState {

    @Getter
    private final int entityId;


}
