package org.lwjglx.opengl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public class GLContext {

    private static GLCapabilities contextCapabilities;

    public static GLCapabilities getCapabilities() {

        if (contextCapabilities == null) {
            contextCapabilities = GL.getCapabilities();
        }

        return contextCapabilities;
    }
}
