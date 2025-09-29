package tech.konata.phosphate.module.impl.render;

import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.settings.ColorSetting;

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
