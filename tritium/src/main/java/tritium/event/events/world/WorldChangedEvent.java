package tritium.event.events.world;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.WorldClient;
import tritium.event.eventapi.Event;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorldChangedEvent extends Event {

    public static final WorldChangedEvent INSTANCE = new WorldChangedEvent(null);

    @Getter
    private WorldClient world;

    public static WorldChangedEvent of(WorldClient world) {
        INSTANCE.world = world;
        return INSTANCE;
    }
}
