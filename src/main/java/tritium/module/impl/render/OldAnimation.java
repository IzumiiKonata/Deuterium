package tritium.module.impl.render;

import tritium.module.Module;
import tritium.settings.BooleanSetting;

/**
 * @author IzumiiKonata
 * @since 2024/11/30 19:43
 */
public class OldAnimation extends Module {

    public OldAnimation() {
        super("OldAnimation", Category.RENDER);
    }

    public final BooleanSetting health = new BooleanSetting("Health", false);
    public final BooleanSetting rod = new BooleanSetting("Rod", false);
    public final BooleanSetting bow = new BooleanSetting("Bow", false);
    public final BooleanSetting sneak = new BooleanSetting("Sneak", false);

}
