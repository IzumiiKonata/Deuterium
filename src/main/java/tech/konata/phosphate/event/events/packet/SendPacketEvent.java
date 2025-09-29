package tech.konata.phosphate.event.events.packet;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import tech.konata.phosphate.utils.player.rotation.RotationUtils;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 9:15
 */
public class SendPacketEvent extends PacketEvent {
    public SendPacketEvent(Packet<?> packet) {
        super(packet);
        if (packet instanceof C03PacketPlayer.C05PacketPlayerLook || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            float yaw = ((C03PacketPlayer) packet).getYaw();
            float pitch = ((C03PacketPlayer) packet).getPitch();
            RotationUtils.serverRotation.setYaw(yaw);
            RotationUtils.serverRotation.setPitch(pitch);
        }
    }
}
