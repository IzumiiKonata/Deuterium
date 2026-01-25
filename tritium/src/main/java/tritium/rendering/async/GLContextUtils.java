package tritium.rendering.async;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.opengl.Display;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author IzumiiKonata
 * @since 11/25/2023
 */
public class GLContextUtils {

    public static long createContext() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        return GLFW.glfwCreateWindow(16, 16, "SubWindow", MemoryUtil.NULL, Display.getWindow());
    }

}
