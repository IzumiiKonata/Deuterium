package tech.konata.phosphate.module.impl.render;

import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render3DEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.RenderWings;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.NumberSetting;

public class Wings
        extends Module {
    public NumberSetting<Double> scale = new NumberSetting<>("Scale", 1.0, 0.0, 2.5, 0.1);
    public BooleanSetting firstPerson = new BooleanSetting("Render in first person", false);
    RenderWings wings = new RenderWings();

    @Handler
    public void onRender3D(Render3DEvent.OldRender3DEvent event) {
        if (!mc.thePlayer.isInvisible()) {
            this.wings.renderWings(mc.thePlayer, event.partialTicks);
        }
    }

    ;

    public Wings() {
        super("Wings", Category.RENDER);
    }
}

