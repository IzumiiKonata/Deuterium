package tritium.event.events.game;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 3:17 PM
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatEvent extends EventCancellable {

    private static final ChatEvent INSTANCE = new ChatEvent("");

    @Getter
    @Setter
    private String msg;

    public static ChatEvent of(String msg) {
        INSTANCE.msg = msg;
        return INSTANCE;
    }

}
