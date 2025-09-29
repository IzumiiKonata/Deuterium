package tech.konata.phosphate.module.impl.movement;


import net.minecraft.client.settings.KeyBinding;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.world.TickEvent;
import tech.konata.phosphate.module.Module;

/**
 * @author IzumiiKonata
 * @since 4/15/2023 7:39 PM
 */
public class AutoSprint extends Module {

    public AutoSprint() {
        super("Auto Sprint", Category.MOVEMENT);
    }

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);

    }

    ;

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
    }

}
