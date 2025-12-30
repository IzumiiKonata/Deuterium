package tritium.event.events.player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import tritium.event.eventapi.EventCancellable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AttackEvent extends EventCancellable {

    private static final AttackEvent INSTANCE = new AttackEvent(null);

    @Getter
    @Setter
    private Entity attackedEntity;

    public static AttackEvent of(Entity attackedEntity) {
        INSTANCE.attackedEntity = attackedEntity;
        return INSTANCE;
    }
}
