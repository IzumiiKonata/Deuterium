package tritium.utils.cursor;

import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;

/**
 * @author IzumiiKonata
 * Date: 2025/1/12 19:39
 */
@UtilityClass
public class CursorUtils {

    public static final long ARROW = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
    public static final long TEXT = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    public static final long NOT_ALLOWED = GLFW.glfwCreateStandardCursor(GLFW.GLFW_NOT_ALLOWED_CURSOR);
    public static final long RESIZE_EW = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_EW_CURSOR);
    public static final long RESIZE_NS = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NS_CURSOR);
    public static final long RESIZE_NWSE = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR);
    public static final long RESIZE_NESW = GLFW.glfwCreateStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR);

    private static long curCursor = ARROW;

    public void setCursor(long cursor) {

        if (curCursor != cursor) {
            GLFW.glfwSetCursor(Display.getWindow(), cursor);
            curCursor = cursor;
        }

    }

}
