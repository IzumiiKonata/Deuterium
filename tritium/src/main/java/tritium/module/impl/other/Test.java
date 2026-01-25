package tritium.module.impl.other;

import org.lwjgl.input.Keyboard;
import tritium.module.Module;
import tritium.rendering.HSBColor;
import tritium.settings.*;

public class Test extends Module {

    public Test() {
        super("Test", Category.OTHER);
    }


    BooleanSetting bs = new BooleanSetting("Boolean Setting", false);
    NumberSetting<Double> ns = new NumberSetting<>("Number Setting", 10.0, 1.0, 15.0, 0.1);
    ModeSetting<Mode> ms = new ModeSetting<>("Mode Setting", Mode.Mode1);
    ColorSetting cs = new ColorSetting("Color Setting", new HSBColor(1f, 1, 1, 255));
    BindSetting bind = new BindSetting("BindSetting", Keyboard.KEY_V);
    LabelSetting lbl = new LabelSetting("LABEL FUCK YOU");
    StringSetting text = new StringSetting("Text", "Placeholder");

    public enum Mode {
        Mode1,
        Mode2,
        Mode3
    }
}
