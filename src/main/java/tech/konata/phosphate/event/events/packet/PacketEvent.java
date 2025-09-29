package tech.konata.phosphate.event.events.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import tech.konata.phosphate.event.eventapi.EventCancellable;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 9:15
 */
@AllArgsConstructor
public class PacketEvent extends EventCancellable {
    @Getter
    @Setter
    private Packet<?> packet;
}
