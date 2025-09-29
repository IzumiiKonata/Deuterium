package tech.konata.phosphate.module.impl.other;

import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.HSBColor;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.ColorSetting;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;

public class Test extends Module {

    public Test() {
        super("Test", Category.OTHER);
    }


    BooleanSetting bs = new BooleanSetting("Boolean Setting", false);
    NumberSetting<Double> ns = new NumberSetting<>("Number Setting", 10.0, 1.0, 15.0, 0.1);
    ModeSetting<Mode> ms = new ModeSetting<>("Mode Setting", Mode.Mode1);
    ColorSetting cs = new ColorSetting("Color Setting", new HSBColor(1f, 1, 1, 255));

    public enum Mode {
        Mode1,
        Mode2,
        Mode3
    }
}
