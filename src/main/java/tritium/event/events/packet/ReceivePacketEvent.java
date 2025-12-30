package tritium.event.events.packet;

import net.minecraft.network.Packet;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 9:16
 */
public class ReceivePacketEvent extends PacketEvent {

    private static final ReceivePacketEvent INSTANCE = new ReceivePacketEvent(null);

    private ReceivePacketEvent(Packet<?> packet) {
        super(packet);
    }

    public static ReceivePacketEvent of(Packet<?> packet) {
        INSTANCE.setPacket(packet);
        return INSTANCE;
    }
}
