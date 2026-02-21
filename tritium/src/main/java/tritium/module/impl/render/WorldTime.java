package tritium.module.impl.render;

import net.minecraft.network.play.server.S03PacketTimeUpdate;
import tritium.event.eventapi.Handler;
import tritium.event.events.packet.ReceivePacketEvent;
import tritium.event.events.player.RespawnEvent;
import tritium.event.events.rendering.Render2DEvent;
import tritium.event.events.world.WorldChangedEvent;
import tritium.module.Module;
import tritium.rendering.animation.Interpolations;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;
import tritium.utils.timing.Timer;

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
                mc.theWorld.setWorldTime((long) Interpolations.interpolate(mc.theWorld.getWorldTime(), time.getValue() * 100, 0.25f));
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
