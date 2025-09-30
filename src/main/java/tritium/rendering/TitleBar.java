package tritium.rendering;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Location;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import tritium.Tritium;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.ShaderRenderType;
import tritium.rendering.shader.Shaders;
import tritium.utils.other.math.MathUtils;
import tritium.utils.timing.Timer;

import java.awt.*;
import java.util.Collections;

public class TitleBar implements SharedRenderingConstants {

    @Getter
    private static final TitleBar instance = new TitleBar();

    private final Minecraft mc = Minecraft.getMinecraft();

    public TitleBar() {

    }

    public static boolean usingTitleBar() {
        return !Tritium.POJAVE && (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_DECORATED) == GLFW.GLFW_FALSE);
    }

    public static double getTitlebarHeight() {
        return TitleBar.usingTitleBar() && !Minecraft.getMinecraft().isFullScreen() ? 14 : 0;
    }

    public void render(double mouseX, double mouseY) {

        if (!TitleBar.usingTitleBar() || mc.isFullScreen())
            return;

        mouseX *= RenderSystem.getScaleFactor();
        mouseY *= RenderSystem.getScaleFactor();

        double titleBarHeight = 14;

        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(ShaderRenderType.OVERLAY, Collections.singletonList(() -> {
            Rect.draw(0, 0, RenderSystem.getWidth(), titleBarHeight, -1, Rect.RectType.EXPAND);
        }));

        Rect.draw(0, 0, RenderSystem.getWidth(), titleBarHeight, hexColor(12, 12, 12, 60), Rect.RectType.EXPAND);
        TexturedShadow.drawBottomShadow(0, titleBarHeight, RenderSystem.getWidth(), 0.6f, 8);

        Image.draw(Location.of("icons/icon_16x16.png"), 4, 3, 8, 8, Image.Type.Normal);

        CFontRenderer fontRenderer = FontManager.pf12;

        fontRenderer.drawString(Display.getTitle(), 16, titleBarHeight * 0.5 - fontRenderer.getHeight() * 0.5 + 1, RenderSystem.hexColor(255, 255, 255, 200));

        this.renderButtons(mouseX, mouseY);

        Point p = MouseInfo.getPointerInfo().getLocation();

        this.moveWindow(p.getX(), p.getY());

        if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            GLFW.glfwSetWindowPos(Display.getWindow(), (int) 0, (int) 0);
            GLFW.glfwSetWindowSize(Display.getWindow(), 1660, 900);
        }
    }

    float closeButtonAlpha;
    float hideButtonAlpha;
    float windowFullButtonAlpha;
    boolean prevMouse = false;

    private void renderButtons(double mouseX, double mouseY) {

        closeButtonAlpha = MathUtils.clamp(closeButtonAlpha, 0.1f, 0.5f);
        windowFullButtonAlpha = MathUtils.clamp(windowFullButtonAlpha, 0.1f, 0.5f);
        hideButtonAlpha = MathUtils.clamp(hideButtonAlpha, 0.1f, 0.5f);

        Location circle = Location.of(Tritium.NAME + "/textures/titlebar/circle.png");

        RenderSystem.color(RenderSystem.reAlpha(-1, closeButtonAlpha));
        Image.draw(circle, RenderSystem.getWidth() - 10, 4.5, 4.5, 4.5, Image.Type.NoColor);

        RenderSystem.color(RenderSystem.reAlpha(-1, windowFullButtonAlpha));
        Image.draw(circle, RenderSystem.getWidth() - 10 - 7.5, 4.5, 4.5, 4.5, Image.Type.NoColor);

        RenderSystem.color(RenderSystem.reAlpha(-1, hideButtonAlpha));
        Image.draw(circle, RenderSystem.getWidth() - 10 - 15, 4.5, 4.5, 4.5, Image.Type.NoColor);

        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() - 10, 4.5, 4.5, 4.5)) {//Close button
            closeButtonAlpha = Interpolations.interpLinear(closeButtonAlpha, 0.5f, 0.2f);
            if (Mouse.isButtonDown(0)) {
                prevMouse = true;
            }
            if (!Mouse.isButtonDown(0) && prevMouse) {
                mc.shutdown();
            }
        } else {
            closeButtonAlpha = Interpolations.interpLinear(closeButtonAlpha, 0.1f, 0.2f);
        }
        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() - 10 - 7.5, 4.5, 4.5, 4.5)) {//Window full button
            windowFullButtonAlpha = Interpolations.interpLinear(windowFullButtonAlpha, 0.5f, 0.2f);
            if (Mouse.isButtonDown(0)) {
                prevMouse = true;
            }
            if (!Mouse.isButtonDown(0) && prevMouse) {
//                GLFW.glfwSetWindowMonitor(Display.getWindow(), 0L, 0, 0, GLFWVidMode.WIDTH, GLFWVidMode.HEIGHT, -1);
//                Display.getWindow().toggleFullscreen();
                if (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_FALSE) {
                    GLFW.glfwMaximizeWindow(Display.getWindow());
                } else {
                    GLFW.glfwRestoreWindow(Display.getWindow());
                }
            }
        } else {
            windowFullButtonAlpha = Interpolations.interpLinear(windowFullButtonAlpha, 0.1f, 0.2f);
        }
        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() - 10 - 15, 4.5, 4.5, 4.5)) {//Hide button
            hideButtonAlpha = Interpolations.interpLinear(hideButtonAlpha, 0.5f, 0.2f);
            if (Mouse.isButtonDown(0)) {
                prevMouse = true;
            }
            if (!Mouse.isButtonDown(0) && prevMouse) {
//                GLFW.glfwIconifyWindow(Display.getWindow());

                if (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
                    GLFW.glfwRestoreWindow(Display.getWindow());
                } else {
                    GLFW.glfwIconifyWindow(Display.getWindow());
                }

            }
        } else {
            hideButtonAlpha = Interpolations.interpLinear(hideButtonAlpha, 0.1f, 0.2f);
        }

        if (!Mouse.isButtonDown(0) && prevMouse)
            prevMouse = false;
    }

    boolean doubleClicked = false, doubleClickCheck = false;
    Timer doubleClickTimer = new Timer();
    double moveX = 0, moveY = 0;
    double moveXW = 0, moveYW = 0;

    boolean grabbingTitleBar = false;
    boolean grabbingCorner = false;

    private void moveWindow(double mouseX, double mouseY) {
        if (!Mouse.isButtonDown(0))
            doubleClicked = false;


        int displayX = Display.getX();
        int displayY = Display.getY();

        mouseX -= displayX;
        mouseY -= displayY;

        if ((RenderSystem.isHovered(mouseX, mouseY, 0, 0, RenderSystem.getWidth() * 2, getTitlebarHeight() * 2) || grabbingTitleBar) && Mouse.isButtonDown(0) && !Mouse.isGrabbed()) {

            if (!grabbingTitleBar)
                grabbingTitleBar = true;

            if (!doubleClicked) {
                doubleClicked = true;
                if (doubleClickTimer.isDelayed(500)) {
                    doubleClickTimer.reset();
                    doubleClickCheck = true;
                } else {
                    if (doubleClickCheck) {
                        if (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_FALSE) {
                            GLFW.glfwMaximizeWindow(Display.getWindow());
                            doubleClickCheck = false;
                            return;
                        } else {
                            GLFW.glfwRestoreWindow(Display.getWindow());
                        }
                        doubleClickCheck = false;
                    }
                }
            }

//            System.out.println(moveX + ", " + moveY);

            if (moveX == 0 && moveY == 0) {
                moveX = mouseX;
                moveY = mouseY;
            } else {
                double posX = displayX + mouseX - moveX;
                double posY = displayY + mouseY - moveY;
                GLFW.glfwSetWindowPos(Display.getWindow(), (int) posX, (int) posY);
            }

        } else if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }

        if (!Mouse.isButtonDown(0) && grabbingTitleBar) {
            grabbingTitleBar = false;
        }

        double cornerSize = 16;
//        Rect.draw(RenderSystem.getWidth() - cornerSize, RenderSystem.getHeight() - cornerSize, cornerSize, cornerSize, -1, Rect.RectType.EXPAND);

        if ((RenderSystem.isHovered(mouseX, mouseY, (RenderSystem.getWidth() - cornerSize) * 2, (RenderSystem.getHeight() - cornerSize) * 2, cornerSize * 2, cornerSize * 2) || grabbingCorner) && Mouse.isButtonDown(0) && !Mouse.isGrabbed()) {

            if (!grabbingCorner)
                grabbingCorner = true;

//            System.out.println(moveX + ", " + moveY);

            if (moveXW == 0 && moveYW == 0) {
                moveXW = mouseX - Display.getWidth();
                moveYW = mouseY - Display.getHeight();
            } else {
                double width = mouseX - moveXW;
                double height = mouseY - moveYW;

                System.out.println(width + "," + height);

                GLFW.glfwSetWindowSize(Display.getWindow(), (int) width, (int) height);
            }

        } else if (moveXW != 0 || moveYW != 0) {
            moveXW = 0;
            moveYW = 0;
        }

        if (!Mouse.isButtonDown(0) && grabbingCorner) {
            grabbingCorner = false;
        }
    }

}
