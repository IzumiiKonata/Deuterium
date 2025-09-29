package tech.konata.phosphate.event.events.rendering;

import lombok.AllArgsConstructor;
import tech.konata.phosphate.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * @since 2024/11/3 11:16
 */
@AllArgsConstructor
public class FovModifierEvent extends Event {

    public float fovModifier;

}
