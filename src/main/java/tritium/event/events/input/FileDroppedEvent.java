package tritium.event.events.input;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/10/7 22:14
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDroppedEvent extends Event {

    private static final FileDroppedEvent INSTANCE = new FileDroppedEvent(null);

    @Getter
    private List<String> names;

    public static FileDroppedEvent of(List<String> names) {
        INSTANCE.names = names;
        return INSTANCE;
    }

}
