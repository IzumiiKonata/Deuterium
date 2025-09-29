package tech.konata.phosphate.event.events.player;

import lombok.*;
import tech.konata.phosphate.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * @since 2024/1/20
 */
@Setter
@Getter
@AllArgsConstructor
public class JumpEvent extends Event {

    private float rotationYaw;
}
