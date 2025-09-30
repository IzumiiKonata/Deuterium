package tritium.event.events.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.WorldClient;
import tritium.event.eventapi.Event;

@RequiredArgsConstructor
public class WorldChangedEvent extends Event {
    @Getter
    private final WorldClient world;
}
