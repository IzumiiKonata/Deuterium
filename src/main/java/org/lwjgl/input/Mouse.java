package org.lwjgl.input;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.LWJGLException;
import org.lwjglx.input.Cursor;
import org.lwjgl.opengl.Display;
import tritium.rendering.loading.LoadingRenderer;

public class Mouse {

    @Getter
    private static boolean grabbed = false;


    private static double latestX = 0;
    private static double latestY = 0;

    private static double x = 0;
    private static double y = 0;

    private static int dwheel = 0;

    private static int ignoreNextDelta = 0;
    private static int ignoreNextMove = 0;

    public static int getX() {
        return (int) x;
    }

    public static int getY() {
        return (int) y;
    }

    public static void addMoveEvent(double mouseX, double mouseY) {
        if (ignoreNextMove > 0) {
            ignoreNextMove--;
            return;
        }

        if (ignoreNextDelta > 0) {
            ignoreNextDelta--;
            x = latestX;
            y = latestY;
        }

        if (LoadingRenderer.hide && Minecraft.getMinecraft() != null && Minecraft.getMinecraft().entityRenderer != null) {
            Minecraft.getMinecraft().entityRenderer.onMouseEvent(mouseX - latestX, Display.getHeight() - mouseY - latestY);
        }

        latestX = mouseX;
        latestY = Display.getHeight() - mouseY;

    }

    public static void addButtonEvent(int button, boolean pressed) {

        Minecraft.getMinecraft().onMousePressed(button, pressed);

    }


    public static void addWheelEvent(double delta) {

        Minecraft.getMinecraft().onDWheel((int) delta);

        dwheel += (int) delta;

    }

    public static void poll() {

        if (!grabbed) {
            if (latestX < 0) latestX = 0;
            if (latestY < 0) latestY = 0;
            if (latestX > Display.getWidth() - 1) latestX = Display.getWidth() - 1;
            if (latestY > Display.getHeight() - 1) latestY = Display.getHeight() - 1;
        }

        x = latestX;
        y = latestY;
    }

    public static void setGrabbed(boolean grab) {
        if (grabbed == grab) {
            return;
        }
        GLFW.glfwSetInputMode(
                Display.getWindow(),
                GLFW.GLFW_CURSOR,
                grab ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        grabbed = grab;
        if (!grab) {
            // The old cursor position is sent instead of the new one in the events following mouse ungrab.
            ignoreNextMove += 2;
            setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            // Movement events are not properly sent when toggling mouse grab mode.
            // Trick the game into getting the correct mouse position if no new events appear.
            latestX = Display.getWidth() / 2.0;
            latestY = Display.getHeight() / 2.0;
            x = latestX;
            y = latestY;

        } else {
            ignoreNextDelta++; // Prevent camera rapidly rotating when closing GUIs.
        }
    }

    public static boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Display.getWindow(), button) == GLFW.GLFW_PRESS;
    }


    public static int getDWheel() {
        int value = dwheel;
        dwheel = 0;
        return value;
    }

    public static void setCursorPosition(int new_x, int new_y) {
        if (grabbed) {
            return;
        }
        GLFW.glfwSetCursorPos(Display.getWindow(), new_x, new_y);
        addMoveEvent(new_x, new_y);
    }

    public static Cursor setNativeCursor(Cursor cursor) throws LWJGLException {
        // no-op
        return null;
    }

    public static boolean isInsideWindow() {
        return Display.isVisible();
    }

}
