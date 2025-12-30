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
public class FileDraggedInEvent extends Event {

    private static final FileDraggedInEvent INSTANCE = new FileDraggedInEvent(null);

    @Getter
    private List<String> names;

    public static FileDraggedInEvent of(List<String> names) {
        INSTANCE.names = names;
        return INSTANCE;
    }

}
