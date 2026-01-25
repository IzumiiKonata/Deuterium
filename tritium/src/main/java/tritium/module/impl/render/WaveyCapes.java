package tritium.module.impl.render;

import tritium.module.Module;
import tritium.rendering.waveycapes.CapeMovement;
import tritium.rendering.waveycapes.CapeStyle;
import tritium.rendering.waveycapes.WindMode;
import tritium.settings.BooleanSetting;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;

public class WaveyCapes extends Module {
    public ModeSetting<WindMode> windMode = new ModeSetting<>("Wind Mode", WindMode.NONE);
    public ModeSetting<CapeStyle> capeStyle = new ModeSetting<>("Cape Mode", CapeStyle.SMOOTH);
    public ModeSetting<CapeMovement> capeMovement = new ModeSetting<>("Cape Mode", CapeMovement.BASIC_SIMULATION);
    public NumberSetting<Integer> gravity = new NumberSetting<>("Gravity", 25, 0, 40, 1);
    public NumberSetting<Integer> heightMultiplier = new NumberSetting<>("Height Multiplier", 6, 0, 10, 1);
    public BooleanSetting onlyLocalPlayer = new BooleanSetting("Only Local Player", true);

    public WaveyCapes() {
        super("Wavey Capes", Category.RENDER);
    }
}
