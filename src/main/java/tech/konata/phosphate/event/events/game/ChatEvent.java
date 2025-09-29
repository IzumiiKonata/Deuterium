package tech.konata.phosphate.event.events.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:17 PM
 */
@AllArgsConstructor
public class ChatEvent extends EventCancellable {

    @Getter
    @Setter
    private String msg;

    public ChatEvent() {
        this.setParallel(true);
    }

}
