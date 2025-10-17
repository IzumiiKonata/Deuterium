package tritium.rendering.shader.impl;

import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.shader.uniform.Uniform1i;
import tritium.rendering.shader.uniform.Uniform4f;

/**
 * @author IzumiiKonata
 * @since 2024/12/3 21:17
 */
public class ColorShader {

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


}
