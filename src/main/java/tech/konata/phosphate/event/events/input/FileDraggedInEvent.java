package tech.konata.phosphate.event.events.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.konata.phosphate.event.eventapi.Event;

import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/10/7 22:14
 */
@Getter
@AllArgsConstructor
public class FileDraggedInEvent extends Event {

    private final List<String> names;

}
