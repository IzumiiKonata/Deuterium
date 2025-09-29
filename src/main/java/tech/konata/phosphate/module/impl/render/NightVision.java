package tech.konata.phosphate.module.impl.render;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.world.TickEvent;
import tech.konata.phosphate.module.Module;

public class NightVision extends Module {

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!mc.thePlayer.isPotionActive(Potion.nightVision.id)) {
            PotionEffect effect = new PotionEffect(Potion.nightVision.id, 1000000, 255, false, false);
            effect.setPotionDurationMax(true);
            mc.thePlayer.addPotionEffect(effect);
        }
    }

    ;

    public NightVision() {
        super("Night Vision", Category.RENDER);
    }
}
