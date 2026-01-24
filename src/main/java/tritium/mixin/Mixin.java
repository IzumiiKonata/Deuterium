package tritium.mixin;

import lombok.experimental.UtilityClass;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

/**
 * @author IzumiiKonata
 * Date: 2026/1/24 11:34
 */
@UtilityClass
public class Mixin {

    public static void setup() {
        MixinBootstrap.init();
//        Mixins.addConfiguration("mixins.yourmod.json");

        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        env.setSide(MixinEnvironment.Side.CLIENT);

        env.setOption(MixinEnvironment.Option.DEBUG_VERBOSE, true);
    }

}
