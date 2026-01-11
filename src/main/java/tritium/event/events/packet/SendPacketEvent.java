package tritium.event.events.packet;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import tritium.utils.player.rotation.RotationUtils;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 9:15
 */
public class SendPacketEvent extends PacketEvent {

    public SendPacketEvent(Packet<?> packet) {
        super(packet);
    }

}
