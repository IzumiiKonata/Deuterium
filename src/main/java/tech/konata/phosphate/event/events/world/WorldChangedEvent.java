package tech.konata.phosphate.event.events.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.WorldClient;
import tech.konata.phosphate.event.eventapi.Event;

@RequiredArgsConstructor
public class WorldChangedEvent extends Event {
    @Getter
    private final WorldClient world;
}
