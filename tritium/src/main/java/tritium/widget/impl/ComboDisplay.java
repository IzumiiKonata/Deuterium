package tritium.widget.impl;

import net.minecraft.network.play.server.S19PacketEntityStatus;
import tritium.event.eventapi.Handler;
import tritium.event.events.packet.ReceivePacketEvent;
import tritium.event.events.player.AttackEvent;
import tritium.utils.timing.Timer;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 20:21
 */
public class ComboDisplay extends SimpleTextWidget {

    public ComboDisplay() {
        super("ComboDisplay");
    }

    int combos = 0;
    int target = -1;
    final Timer timer = new Timer();

    @Override
    public String getText() {
        String text = combos + " Combo";

        if (combos > 1) {
            text += "s";
        }

        if (timer.isDelayed(2000)) {
            combos = 0;
        }

        return text;
    }

    @Handler
    public void attack(AttackEvent event) {
        target = event.getAttackedEntity().getEntityId();
    }

    @Handler
    public void onStatusUpdate(ReceivePacketEvent event) {

        if (event.getPacket() instanceof S19PacketEntityStatus packet) {
            if (packet.getOpCode() == 2) {
                if (packet.entityId == target) {
                    combos += 1;
                    target = -1;
                    timer.reset();
                } else if (packet.entityId == mc.thePlayer.getEntityId()) {
                    combos = 0;
                }
            }
        }

    }

}
