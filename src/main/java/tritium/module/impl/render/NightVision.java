package tritium.module.impl.render;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import tritium.event.eventapi.Handler;
import tritium.event.events.world.TickEvent;
import tritium.module.Module;

public class NightVision extends Module {

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!mc.thePlayer.isPotionActive(Potion.nightVision.id)) {
            PotionEffect effect = new PotionEffect(Potion.nightVision.id, 1000000, 1, false, false);
            effect.setPotionDurationMax(true);
            mc.thePlayer.addPotionEffect(effect);
        }
    }

    ;

    public NightVision() {
        super("Night Vision", Category.RENDER);
    }
}
