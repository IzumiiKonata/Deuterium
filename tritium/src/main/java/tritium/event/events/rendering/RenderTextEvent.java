package tritium.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.EventCancellable;

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
