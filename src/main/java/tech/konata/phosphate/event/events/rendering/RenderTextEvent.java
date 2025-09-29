package tech.konata.phosphate.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * Date: 2024/12/28 21:40
 */
@Getter
@Setter
@AllArgsConstructor
public class RenderTextEvent extends EventCancellable {

    private String text;

}
