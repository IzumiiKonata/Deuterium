package tritium.event.events.player;

import lombok.*;
import tritium.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * @since 2024/1/20
 */
@Setter
@Getter
@AllArgsConstructor
public class JumpEvent extends EventCancellable {

    private float rotationYaw;
}
