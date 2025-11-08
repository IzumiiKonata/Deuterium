package tritium.event.events.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:17 PM
 */
@AllArgsConstructor
public class ChatEvent extends EventCancellable {

    @Getter
    @Setter
    private String msg;

}
