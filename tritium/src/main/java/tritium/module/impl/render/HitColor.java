package tritium.module.impl.render;

import tritium.module.Module;
import tritium.rendering.HSBColor;
import tritium.settings.ColorSetting;

/**
 * @author IzumiiKonata
 * @since 2024/8/20 13:18
 */
public class HitColor extends Module {

    public HitColor() {
        super("HitColor", Category.RENDER);
    }

    public final ColorSetting color = new ColorSetting("Color", new HSBColor(255, 0, 0, 76));

}
