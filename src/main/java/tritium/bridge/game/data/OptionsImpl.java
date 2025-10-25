package tritium.bridge.game.data;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import today.opai.api.enums.EnumKeybind;
import today.opai.api.interfaces.game.Options;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:31
 */
public class OptionsImpl implements Options {

    @Getter
    private static final OptionsImpl instance = new OptionsImpl();

    @Override
    public float getFovSetting() {
        return Minecraft.getMinecraft().gameSettings.fovSetting;
    }

    @Override
    public int getThirdPersonViewState() {
        return Minecraft.getMinecraft().gameSettings.thirdPersonView;
    }

    @Override
    public void setThirdPersonView(int personView) {
        Minecraft.getMinecraft().gameSettings.thirdPersonView = personView;
    }

    @Override
    public boolean isPressed(EnumKeybind key) {

        if (key == null)
            return false;

        return this.getKeyBinding(key).isPressed();
    }

    @Override
    public void setPressed(EnumKeybind key, boolean pressed) {
        if (key == null)
            return;

        this.getKeyBinding(key).pressed = pressed;
    }

    private KeyBinding getKeyBinding(EnumKeybind key) {

        switch (key) {
            case FORWARD:
                return Minecraft.getMinecraft().gameSettings.keyBindForward;
            case LEFT:
                return Minecraft.getMinecraft().gameSettings.keyBindLeft;
            case RIGHT:
                return Minecraft.getMinecraft().gameSettings.keyBindRight;
            case BACK:
                return Minecraft.getMinecraft().gameSettings.keyBindBack;
            case JUMP:
                return Minecraft.getMinecraft().gameSettings.keyBindJump;
            case SNEAK:
                return Minecraft.getMinecraft().gameSettings.keyBindSneak;
            case SPRINT:
                return Minecraft.getMinecraft().gameSettings.keyBindSprint;
            case USE_ITEM:
                return Minecraft.getMinecraft().gameSettings.keyBindUseItem;
            case ATTACK:
                return Minecraft.getMinecraft().gameSettings.keyBindAttack;
            case PLAYER_LIST:
                return Minecraft.getMinecraft().gameSettings.keyBindPlayerList;
            default:
                throw new IllegalArgumentException("Invalid key");
        }
    }

    @Override
    public float getMouseSensitivity() {
        return Minecraft.getMinecraft().gameSettings.mouseSensitivity;
    }

    @Override
    public void setMouseSensitivity(float sensitivity) {
        Minecraft.getMinecraft().gameSettings.mouseSensitivity = sensitivity;
    }

    @Override
    public boolean isViewBobbing() {
        return Minecraft.getMinecraft().gameSettings.viewBobbing;
    }

    @Override
    public void setViewBobbing(boolean state) {
        Minecraft.getMinecraft().gameSettings.viewBobbing = state;
    }

    @Override
    public void setTimerSpeed(float speed) {
        Minecraft.getMinecraft().timer.timerSpeed = speed;
    }

    @Override
    public float getTimerSpeed() {
        return Minecraft.getMinecraft().timer.timerSpeed;
    }

    @Override
    public void setForceUnicodeFont(boolean forceUnicodeFont) {
        Minecraft.getMinecraft().gameSettings.forceUnicodeFont = forceUnicodeFont;
    }

    @Override
    public boolean isForceUnicodeFont() {
        return Minecraft.getMinecraft().gameSettings.forceUnicodeFont;
    }

    @Override
    public int getGuiScale() {
        return Minecraft.getMinecraft().gameSettings.guiScale;
    }

    @Override
    public void setGuiScale(int scale) {
        Minecraft.getMinecraft().gameSettings.guiScale = scale;
    }

    @Override
    public void setFovSetting(float fovSetting) {
        Minecraft.getMinecraft().gameSettings.fovSetting = fovSetting;
    }
}
