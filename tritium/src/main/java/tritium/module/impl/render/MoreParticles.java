package tritium.module.impl.render;

import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.EnumParticleTypes;
import tritium.event.eventapi.Handler;
import tritium.event.events.packet.SendPacketEvent;
import tritium.module.Module;
import tritium.settings.BooleanSetting;
import tritium.settings.NumberSetting;

public class MoreParticles extends Module {
    public static NumberSetting<Integer> crackSize = new NumberSetting<>("Crack Size", 2, 0, 10, 1);
    public static BooleanSetting crit = new BooleanSetting("Crit Particle", true);
    public static BooleanSetting normal = new BooleanSetting("Normal Particle", true);

    @Handler
    public void onSendPacket(SendPacketEvent e) {
        if (e.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) e.getPacket()).getPacketAction() == C02PacketUseEntity.Action.ATTACK) {
            for (int index = 0; index < crackSize.getValue(); ++index) {
                if (crit.getValue()) {
                    mc.effectRenderer.emitParticleAtEntity(((C02PacketUseEntity) e.getPacket()).getEntityFromWorld(mc.theWorld), EnumParticleTypes.CRIT);
                }
                if (!normal.getValue()) {
                    continue;
                }
                mc.effectRenderer.emitParticleAtEntity(((C02PacketUseEntity) e.getPacket()).getEntityFromWorld(mc.theWorld), EnumParticleTypes.CRIT_MAGIC);
            }
        }
    }

    public MoreParticles() {
        super("More Particles", Category.RENDER);
    }
}
