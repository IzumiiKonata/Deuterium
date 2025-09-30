package tritium.rendering.shader.impl;

import net.minecraft.client.Minecraft;
import tritium.rendering.shader.Shader;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.ShaderRenderType;
import tritium.rendering.shader.Shaders;
import tritium.rendering.shader.uniform.Uniform1i;
import tritium.rendering.shader.uniform.Uniform2f;
import tritium.rendering.shader.uniform.Uniform3f;
import tritium.utils.math.RandomUtils;

import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 09:57
 */
public class Deconverge extends Shader {

    private final ShaderProgram deconvergeProgram = new ShaderProgram("deconverge.frag", "vertex.vsh");

    Uniform1i textureIn = new Uniform1i(deconvergeProgram, "textureIn");
    Uniform2f texelSize = new Uniform2f(deconvergeProgram, "texelSize");

    Uniform3f convX = new Uniform3f(deconvergeProgram, "convX");
    Uniform3f convY = new Uniform3f(deconvergeProgram, "convY");
    Uniform3f radConvX = new Uniform3f(deconvergeProgram, "radConvX");
    Uniform3f radConvY = new Uniform3f(deconvergeProgram, "radConvY");

    public void render() {
        this.deconvergeProgram.start();
        textureIn.setValue(0);
        texelSize.setValue(1.0F / Minecraft.getMinecraft().displayWidth, 1.0F / Minecraft.getMinecraft().displayHeight);

        this.setConvergeX(RandomUtils.nextFloat(-3, 3), RandomUtils.nextFloat(-3, 3),
                RandomUtils.nextFloat(-3, 3));

        this.setConvergeY(RandomUtils.nextFloat(-3, 3), RandomUtils.nextFloat(-3, 3),
                RandomUtils.nextFloat(-3, 3));

        ShaderProgram.drawQuad();
        ShaderProgram.stop();
    }

    public void setConvergeX(float red, float green, float blue) {
        convX.setValue(red, green, blue);
    }

    public void setConvergeY(float red, float green, float blue) {
        convY.setValue(red, green, blue);
    }

    @Override
    public void run(ShaderRenderType type, List<Runnable> runnable) {

    }

    @Override
    public void runNoCaching(ShaderRenderType type, List<Runnable> runnable) {

    }

    @Override
    public void update() {

    }
}
