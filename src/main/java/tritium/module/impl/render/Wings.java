package tritium.module.impl.render;

import tritium.event.eventapi.Handler;
import tritium.event.events.rendering.Render3DEvent;
import tritium.module.Module;
import tritium.rendering.RenderWings;
import tritium.settings.BooleanSetting;
import tritium.settings.NumberSetting;

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

