package tech.konata.phosphate.module.impl.render;

import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.waveycapes.CapeMovement;
import tech.konata.phosphate.rendering.waveycapes.CapeStyle;
import tech.konata.phosphate.rendering.waveycapes.WindMode;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;

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
