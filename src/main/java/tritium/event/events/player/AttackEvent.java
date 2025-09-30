package tritium.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import tritium.event.eventapi.EventCancellable;

@AllArgsConstructor
public class AttackEvent extends EventCancellable {
    @Getter
    @Setter
    private Entity attackedEntity;
}
