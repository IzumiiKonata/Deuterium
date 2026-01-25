package tritium.module.impl.render;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import tritium.event.eventapi.Handler;
import tritium.event.events.world.TickEvent;
import tritium.module.Module;
import tritium.settings.BooleanSetting;
import tritium.settings.ModeSetting;

public class Perspective extends Module {
    public static boolean perspectiveToggled;
    private static float cameraYaw;
    private static float cameraPitch;
    private static int previousPerspective;

    static {
        perspectiveToggled = false;
        cameraYaw = 0.0f;
        cameraPitch = 0.0f;
        previousPerspective = 0;
    }

    ModeSetting<Mode> click = new ModeSetting<>("Mode", Mode.Hold);
    static final BooleanSetting invertMouse = new BooleanSetting("Invert Mouse", false);
    static final BooleanSetting autoSwitchThirdperson = new BooleanSetting("Auto Switch Thirdperson", true);

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            if (click.getValue() == Mode.Hold) {

                if (mc.currentScreen == null) {
                    Keyboard.enableRepeatEvents(false);
                }

                int keyBind = super.getKeyBind();
                if (keyBind > 0) {
                    if (!Keyboard.isKeyDown(keyBind)) {
                        super.setEnabled(false);
                    }
                } else {
                    if (!Mouse.isButtonDown(keyBind + 100))
                        super.setEnabled(false);
                }
            }
        }
    }

    ;

    public Perspective() {
        super("Perspective", Category.RENDER);
        super.setKeyBind(Keyboard.KEY_LMENU);
    }

    public static float getCameraYaw() {
        return perspectiveToggled ? cameraYaw : mc.getRenderViewEntity().rotationYaw;
    }

    public static float getCameraPitch() {
        return perspectiveToggled ? invertMouse.getValue() ? -cameraPitch : cameraPitch : mc.getRenderViewEntity().rotationPitch;
    }

    public static float getCameraPrevYaw() {
        return perspectiveToggled ? cameraYaw : mc.getRenderViewEntity().prevRotationYaw;
    }

    public static float getCameraPrevPitch() {
        return perspectiveToggled ? invertMouse.getValue() ? -cameraPitch : cameraPitch : mc.getRenderViewEntity().prevRotationPitch;
    }

    public static boolean overrideMouse(double dX, double dY) {
        if ((mc.inGameHasFocus && Display.isActive()) /*|| ((mc.currentScreen == null || mc.currentScreen instanceof GuiIngameMenu) && EventBus.canReceive(MouseXYChangeEvent.class))*/) {
            if (!perspectiveToggled) {
                return true;
            }
            float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            float f2 = f1 * f1 * f1 * 8.0f;
            float f3 = (float) (dX * f2);
            float f4 = (float) (dY * f2);
            cameraYaw += f3 * 0.15f;
            cameraPitch += f4 * 0.15f;
            if (cameraPitch > 90.0f) {
                cameraPitch = 90.0f;
            }
            if (cameraPitch < -90.0f) {
                cameraPitch = -90.0f;
            }
        }
        return false;
    }

    @Override
    public void onEnable() {

        if (mc.thePlayer != null) {
            perspectiveToggled = true;
            cameraYaw = mc.thePlayer.rotationYaw;
            cameraPitch = invertMouse.getValue() ? -mc.thePlayer.rotationPitch : mc.thePlayer.rotationPitch;
            if (autoSwitchThirdperson.getValue()) {
                previousPerspective = mc.gameSettings.thirdPersonView;
                mc.gameSettings.thirdPersonView = 1;
            }
        }

    }

    @Override
    public void onDisable() {
        perspectiveToggled = false;
        if (autoSwitchThirdperson.getValue()) {
            mc.gameSettings.thirdPersonView = previousPerspective;
        }
    }

    enum Mode {
        Click, Hold
    }
}
