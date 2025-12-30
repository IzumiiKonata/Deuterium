package tritium.event.events.game;

import lombok.Getter;
import tritium.event.eventapi.Event;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:23
 */
public class GameLoopEvent extends Event {

    @Getter
    private static final GameLoopEvent INSTANCE = new GameLoopEvent();

}
