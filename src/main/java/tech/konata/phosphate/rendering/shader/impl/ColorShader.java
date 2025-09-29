package tech.konata.phosphate.rendering.shader.impl;


import tech.konata.phosphate.rendering.shader.Shader;
import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.uniform.Uniform1i;
import tech.konata.phosphate.rendering.shader.uniform.Uniform4f;

import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/12/3 21:17
 */
public class ColorShader extends Shader {

    private final ShaderProgram ctProgram = new ShaderProgram("ct.frag", "vertex.vsh");
    private final Uniform1i textureIn = new Uniform1i(ctProgram, "texture");
    private final Uniform4f color = new Uniform4f(ctProgram, "color");

    public void render(float r, float g, float b, float a) {

        this.ctProgram.start();
        textureIn.setValue(0);
        color.setValue(r, g, b, a);

        ShaderProgram.drawQuad();
        ShaderProgram.stop();
    }

    @Override
    public void run(ShaderRenderType type, List<Runnable> runnable) {
        // does nothing
    }

    @Override
    public void runNoCaching(ShaderRenderType type, List<Runnable> runnable) {

    }

    @Override
    public void update() {

    }

}
