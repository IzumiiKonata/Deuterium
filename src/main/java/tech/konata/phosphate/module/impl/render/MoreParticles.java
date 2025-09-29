package tech.konata.phosphate.module.impl.render;

import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.EnumParticleTypes;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.packet.SendPacketEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.NumberSetting;

public class MoreParticles extends Module {
    public static NumberSetting<Double> CrackSize = new NumberSetting<>("Crack Size", 2.0, 0.0, 10.0, 1.0);
    public static BooleanSetting Crit = new BooleanSetting("Crit Particle", true);
    public static BooleanSetting Normal = new BooleanSetting("Normal Particle", true);

    @Handler
    public void onSendPacket(SendPacketEvent e) {
        if (e.getPacket() instanceof C02PacketUseEntity && ((C02PacketUseEntity) e.getPacket()).getAction() == C02PacketUseEntity.Action.ATTACK) {
            for (int index = 0; index < CrackSize.getValue().intValue(); ++index) {
                if (Crit.getValue()) {
                    mc.effectRenderer.emitParticleAtEntity(((C02PacketUseEntity) e.getPacket()).getEntityFromWorld(mc.theWorld), EnumParticleTypes.CRIT);
                }
                if (!Normal.getValue()) {
                    continue;
                }
                mc.effectRenderer.emitParticleAtEntity(((C02PacketUseEntity) e.getPacket()).getEntityFromWorld(mc.theWorld), EnumParticleTypes.CRIT_MAGIC);
            }
        }
    }

    ;

    public MoreParticles() {
        super("More Particles", Category.RENDER);
    }
}
