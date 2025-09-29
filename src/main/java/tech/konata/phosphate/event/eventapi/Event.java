package tech.konata.phosphate.event.eventapi;

import lombok.Getter;
import lombok.Setter;

/**
 * @author IzumiiKonata
 * @since 3/25/2023 11:03 PM
 */
public class Event {
    @Getter
    @Setter
    boolean responded = false;

    @Getter
    @Setter
    boolean parallel = false;
}
