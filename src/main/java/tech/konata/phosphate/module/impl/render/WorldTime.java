package tech.konata.phosphate.module.impl.render;

import net.minecraft.network.play.server.S03PacketTimeUpdate;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.packet.ReceivePacketEvent;
import tech.konata.phosphate.event.events.player.RespawnEvent;
import tech.konata.phosphate.event.events.rendering.Render2DEvent;
import tech.konata.phosphate.event.events.world.WorldChangedEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.utils.timing.Timer;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 13:29
 */
public class WorldTime extends Module {

    public WorldTime() {
        super("WorldTime", Category.RENDER);
    }

    public final NumberSetting<Integer> time = new NumberSetting<>("Time", 140, 0, 240, 1);

    public final ModeSetting<Weather> weather = new ModeSetting<>("Weather", Weather.None);
    public final NumberSetting<Float> weatherStrength = new NumberSetting<>("Weather Strength", 0.0f, 0.0f, 1.0f, 0.05f, () -> this.weather.getValue() != Weather.None);

    public enum Weather {
        None,
        Rain,
        Snow,
    }

    Timer worldJoinTimer = new Timer();

    @Handler
    public void onRespawn(RespawnEvent event) {
        worldJoinTimer.reset();
    }

    @Handler
    public void onJoinWorld(WorldChangedEvent event) {
        worldJoinTimer.reset();
    }

    @Override
    public void onEnable() {
        worldJoinTimer.reset();
    }

    @Handler
    public void onUpdate(Render2DEvent event) {
        if (mc.theWorld != null) {

//            long delta = Math.abs(mc.theWorld.getWorldTime() - time.getValue() * 100);

            if (worldJoinTimer.isDelayed(500)) {
                mc.theWorld.setWorldTime((long) Interpolations.interpBezier(mc.theWorld.getWorldTime(), time.getValue() * 100, 0.25f));
            } else {
                mc.theWorld.setWorldTime(time.getValue() * 100);
            }

        }
    }

    @Handler
    public void onPacket(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCancelled();
        }
    }

}
