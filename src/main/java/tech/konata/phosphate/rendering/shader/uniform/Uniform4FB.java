package tech.konata.phosphate.rendering.shader.uniform;

import org.lwjgl.opengl.GL20;
import tech.konata.phosphate.rendering.shader.ShaderProgram;

import java.nio.FloatBuffer;

/**
 * @author IzumiiKonata
 * @since 2024/10/29 21:38
 */
public class Uniform4FB {

    private final int location;
    private FloatBuffer value;

    public Uniform4FB(ShaderProgram shader, String uniformName) {
        this.location = GL20.glGetUniformLocation(shader.getProgramId(), uniformName);
    }

    public void setValue(FloatBuffer value) {

        if (this.value != value) {
            GL20.glUniform4fv(location, value);
            this.value = value;
        }

    }

}
