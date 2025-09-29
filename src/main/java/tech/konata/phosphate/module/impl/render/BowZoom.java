package tech.konata.phosphate.module.impl.render;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.FovModifierEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2024/11/3 11:18
 */
public class BowZoom extends Module {

    public BowZoom() {
        super("BowZoom", Category.RENDER);
    }

    public final NumberSetting<Integer> scaleFactor = new NumberSetting<>("Factor", 5, 1, 15,1);

    @Handler
    public void onFovModifierChange(FovModifierEvent event) {
        float base = 1.0F;
        ItemStack item = mc.thePlayer.getItemInUse();
        int useDuration = mc.thePlayer.getItemInUseDuration();

        float bowFov = scaleFactor.getValue();

        if(item != null && item.getItem() == Items.bow) {
            int duration = (int) Math.min(useDuration, 20.0F);
            float modifier = this.getModifierByTick(duration);
            base-= modifier * bowFov;
            event.fovModifier = base;
        }
    }

    public float getModifierByTick(int ticks) {
        return 0.15f / 20f * ticks;
    }


}
