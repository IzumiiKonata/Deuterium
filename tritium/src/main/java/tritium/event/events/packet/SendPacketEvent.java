package tritium.event.events.packet;

import net.minecraft.network.Packet;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 9:15
 */
public class SendPacketEvent extends PacketEvent {

    public SendPacketEvent(Packet<?> packet) {
        super(packet);
    }

}
