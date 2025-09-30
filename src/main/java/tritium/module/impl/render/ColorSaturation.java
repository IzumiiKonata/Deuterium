package tritium.module.impl.render;

import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.Location;
import tritium.event.eventapi.Handler;
import tritium.event.events.world.TickEvent;
import tritium.module.Module;
import tritium.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2024/9/7 22:29
 */
public class ColorSaturation extends Module {

    public ColorSaturation() {
        super("ColorSaturation", Category.RENDER);
    }

    private Location phosphorBlur = Location.of("shaders/post/color_convolve.json");

    public NumberSetting<Float> amount = new NumberSetting<Float>("Amount", 1f, -1f, 5f, 0.1f) {
        @Override
        public void onValueChanged(Float last, Float now) {

            if (mc.entityRenderer != null && mc.entityRenderer.isShaderActive()) {
                for (Shader shader : mc.entityRenderer.getShaderGroup().listShaders) {
                    ShaderUniform su = shader.getShaderManager().getShaderUniform("Saturation");

                    if (su == null) {
                        continue;
                    }

                    su.set(now);
                }
            }

        }
    };

    @Override
    public void onEnable() {

        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        mc.entityRenderer.loadShader(phosphorBlur);

        for (Shader shader : mc.entityRenderer.getShaderGroup().listShaders) {
            ShaderUniform su = shader.getShaderManager().getShaderUniform("Saturation");

            if (su == null) {
                continue;
            }

            su.set(amount.getValue());
        }
    }

    @Override
    public void onDisable() {
        mc.entityRenderer.stopUseShader();
    }

    @Handler
    public void onTick(TickEvent event) {
        if (!event.isPre())
            return;

        // Only update the shader if one is active
        if (!mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.loadShader(phosphorBlur);
        }

        for (Shader shader : mc.entityRenderer.getShaderGroup().listShaders) {
            ShaderUniform su = shader.getShaderManager().getShaderUniform("Saturation");

            if (su == null) {
                continue;
            }

            su.set(amount.getValue());
        }
    }


}
