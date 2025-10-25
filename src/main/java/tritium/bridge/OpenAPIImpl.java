package tritium.bridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.IChatComponent;
import today.opai.api.OpenAPI;
import today.opai.api.enums.EnumNotificationType;
import today.opai.api.features.ExtensionScreen;
import today.opai.api.interfaces.EventHandler;
import today.opai.api.interfaces.Registerable;
import today.opai.api.interfaces.client.HypixelAPI;
import today.opai.api.interfaces.client.IRC;
import today.opai.api.interfaces.game.Options;
import today.opai.api.interfaces.game.entity.LocalPlayer;
import today.opai.api.interfaces.game.network.PacketUtil;
import today.opai.api.interfaces.game.world.World;
import today.opai.api.interfaces.managers.GameStateManager;
import today.opai.api.interfaces.managers.ModuleManager;
import today.opai.api.interfaces.managers.RotationManager;
import today.opai.api.interfaces.managers.ValueManager;
import today.opai.api.interfaces.render.*;
import tritium.Tritium;
import tritium.bridge.game.data.OptionsImpl;
import tritium.bridge.management.GameStateManagerImpl;
import tritium.bridge.management.ModuleManagerImpl;
import tritium.bridge.management.RotationManagerImpl;
import tritium.bridge.management.ValueManagerImpl;
import tritium.event.events.game.ChatEvent;
import tritium.management.CommandManager;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:24
 */
public class OpenAPIImpl implements OpenAPI {

    @Override
    public void registerFeature(Registerable registerable) {
        
    }

    @Override
    public void registerEvent(EventHandler eventHandler) {
        BridgeEventHandler.register(eventHandler);
    }

    @Override
    public void unregisterEvent(EventHandler eventHandler) {
        BridgeEventHandler.unregister(eventHandler);
    }

    @Override
    public void addFriend(String ign) {

    }

    @Override
    public void runCommand(String clientCommand) {
        Tritium.getInstance().getCommandManager().onChat(new ChatEvent(clientCommand));
    }

    @Override
    public void loadConfig(String config) {
        
    }

    @Override
    public void printMessage(String message) {

        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(message);
        }

    }

    @Override
    public void printChatComponent(String json) {

        try {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(json));
            }
        } catch (Exception e) {
            if (Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(e.getMessage());
            }
        }

    }

    @Override
    public void displayScreen(ExtensionScreen extensionScreen) {
        
    }

    @Override
    public void popNotification(EnumNotificationType type, String title, String message, long shownTime) {
        
    }

    @Override
    public boolean isFriend(String ign) {
        return false;
    }

    @Override
    public boolean isTarget(String ign) {
        return false;
    }

    @Override
    public void addTarget(String ign) {
        
    }

    @Override
    public void removeFriend(String ign) {

    }

    @Override
    public void removeTarget(String ign) {

    }

    @Override
    public boolean isNull() {
        return Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null;
    }

    @Override
    public String getClientUsername() {
        return Minecraft.getMinecraft().getSession().getUsername();
    }

    @Override
    public String getClientVersion() {
        return Tritium.getVersion().toString();
    }

    @Override
    public int getFrameRate() {
        return Minecraft.getDebugFPS();
    }

    @Override
    public IRC getIRC() {
        return null;
    }

    @Override
    public HypixelAPI getHypixelAPI() {
        return null;
    }

    @Override
    public LocalPlayer getLocalPlayer() {
        return (LocalPlayer) Minecraft.getMinecraft().thePlayer.getWrapper();
    }

    @Override
    public World getWorld() {
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;
        return theWorld == null ? null : theWorld.getWrapper();
    }

    @Override
    public Options getOptions() {
        return OptionsImpl.getInstance();
    }

    @Override
    public ValueManager getValueManager() {
        return ValueManagerImpl.getInstance();
    }

    @Override
    public ModuleManager getModuleManager() {
        return ModuleManagerImpl.getInstance();
    }

    @Override
    public RotationManager getRotationManager() {
        return RotationManagerImpl.getInstance();
    }

    @Override
    public GameStateManager getGameStateManager() {
        return GameStateManagerImpl.getInstance();
    }

    @Override
    public RenderUtil getRenderUtil() {
        return null;
    }

    @Override
    public ShaderUtil getShaderUtil() {
        return null;
    }

    @Override
    public FontUtil getFontUtil() {
        return null;
    }

    @Override
    public GLStateManager getGLStateManager() {
        return null;
    }

    @Override
    public PacketUtil getPacketUtil() {
        return null;
    }

    @Override
    public ItemUtil getItemUtil() {
        return null;
    }

}
