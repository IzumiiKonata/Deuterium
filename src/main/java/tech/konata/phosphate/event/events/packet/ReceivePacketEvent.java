package tech.konata.phosphate.event.events.packet;

import net.minecraft.network.Packet;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 9:16
 */
public class ReceivePacketEvent extends PacketEvent {
    public ReceivePacketEvent(Packet<?> packet) {
        super(packet);
    }
}
