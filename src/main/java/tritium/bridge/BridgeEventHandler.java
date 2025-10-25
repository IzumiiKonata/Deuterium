package tritium.bridge;

import lombok.Getter;
import net.minecraft.network.play.server.S02PacketChat;
import today.opai.api.enums.EnumEventStage;
import today.opai.api.enums.EnumNotificationType;
import today.opai.api.events.*;
import today.opai.api.interfaces.EventHandler;
import today.opai.api.interfaces.modules.PresetModule;
import tritium.event.eventapi.Handler;
import tritium.event.eventapi.State;
import tritium.event.events.game.ChatEvent;
import tritium.event.events.game.GameLoopEvent;
import tritium.event.events.game.KeyPressedEvent;
import tritium.event.events.packet.ReceivePacketEvent;
import tritium.event.events.player.*;
import tritium.event.events.rendering.RenderNameTagEvent;
import tritium.event.events.world.TickEvent;
import tritium.event.events.world.WorldChangedEvent;
import tritium.management.EventManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 21:11
 */
public class BridgeEventHandler {

    @Getter
    private static final Map<EventHandler, BridgeEventHandler> handlers = new ConcurrentHashMap<>();

    public static void register(EventHandler handler) {
        if (!handlers.containsKey(handler)) {
            BridgeEventHandler bridgeHandler = new BridgeEventHandler(handler);
            handlers.put(handler, bridgeHandler);

            EventManager.register(bridgeHandler);
        }
    }

    public static void unregister(EventHandler handler) {
        BridgeEventHandler bridgeHandler = handlers.get(handler);
        if (bridgeHandler != null) {
            EventManager.unregister(bridgeHandler);
            handlers.remove(handler);
        }
    }

    private final EventHandler handler;

    public BridgeEventHandler(EventHandler handler) {
        this.handler = handler;
    }

    @Handler
    public void onTick(TickEvent event) {
        
        if (!event.isPre())
            return;
        
        handler.onTick();
    }

    @Handler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!event.isPre())
            return;

        handler.onPlayerUpdate();
    }

    @Handler
    public void onRenderNameTags(RenderNameTagEvent event) {

        EventRenderNameTag evt = new EventRenderNameTag();
        handler.onRenderNameTags(evt);

        if (evt.isCancelled())
            event.setCancelled();

    }

    public void onNotification(EnumNotificationType notificationType, String title, String content, long duration) {

    }

    @Handler
    public void onMotionUpdate(UpdateEvent event) {

        EventMotionUpdate evt = new EventMotionUpdate(event.getPosX(), event.getPosY(), event.getPosZ(), event.getRotationYaw(), event.getRotationPitch(), event.isOnGround(), event.isPre() ? EnumEventStage.PRE : EnumEventStage.POST);

        handler.onMotionUpdate(evt);

        event.setPosX(evt.getX());
        event.setPosY(evt.getY());
        event.setPosZ(evt.getZ());
        event.setRotationYaw(evt.getYaw());
        event.setRotationPitch(evt.getPitch());
        event.setOnGround(evt.isGround());

    }

    /**
     * Called on each loop iteration.
     */
    @Handler
    public void onLoop(GameLoopEvent event) {
        handler.onLoop();
    }

    /**
     * Called on loading world
     */
    @Handler
    public void onLoadWorld(WorldChangedEvent event) {
        handler.onLoadWorld();
    }

    public void onModuleToggle(PresetModule module, boolean state) {
        handler.onModuleToggle(module, state);
    }

    /**
     * Called when a player strafe.
     * Cancel this event for prevent move.
     *
     * @param event The strafe event.
     */
    public void onStrafe(EventStrafe event) {
    }

    /**
     * Called when player input movement.
     *
     * @param event The move event.
     */
    @Handler
    public void onMoveInput(MovementInputEvent event) {

        EventMoveInput evt = new EventMoveInput(event.getForward(), event.getStrafe());
        handler.onMoveInput(evt);

        event.setForward(evt.getForward());
        event.setStrafe(evt.getStrafe());

    }

    /**
     * Called when a player moves.
     *
     * @param event The move event.
     */
    @Handler
    public void onMove(MoveEvent event) {
        EventMove evt = new EventMove(event.getX(), event.getY(), event.getZ());
        handler.onMove(evt);
        event.setX(evt.getX());
        event.setY(evt.getY());
        event.setZ(evt.getZ());
    }

    /**
     * Called when a key is pressed.
     * Also, available in any container gui (e.g. inventory, chest).
     *
     * @param keyCode The code of the key that was pressed.
     */
    @Handler
    public void onKey(KeyPressedEvent event) {

        handler.onKey(event.getKeyCode());

    }

    /**
     * Called when a slowdown by using items.
     * Cancel this event for no slowdown effect.
     *
     * @param event The slowdown event.
     */
    @Handler
    public void onSlowdown(SlowDownEvent event) {

        EventSlowdown evt = new EventSlowdown();

        handler.onSlowdown(evt);

        if (evt.isCancelled())
            event.setCancelled();

    }

    /**
     * Called when a jump occurs.
     * Cancel this event for prevent jump.
     *
     * @param event The jump event.
     */
    @Handler
    public void onJump(JumpEvent event) {

        EventJump evt = new EventJump();
        handler.onJump(evt);

        if (evt.isCancelled())
            event.setCancelled();

    }

    /**
     * Called when a chat message is received.
     * Cancel this event for prevent chat message appears in chat.
     *
     * @param event The chat received event.
     */
    @Handler
    public void onChat(ReceivePacketEvent event) {

        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();

            if (packet.isChat()) {
                EventChatReceived evt = new EventChatReceived(packet.getChatComponent().getUnformattedText());

                handler.onChat(evt);
            }

        }

    }

    /**
     * Called when sending a chat message.
     * Cancel this event for prevent chat message send.
     *
     * @param event The chat send event.
     */
    @Handler
    public void onChat(ChatEvent event) {

        EventChatSend evt = new EventChatSend(event.getMsg());

        handler.onChat(evt);
        event.setMsg(evt.getMessage());

        if (evt.isCancelled())
            event.setCancelled();

    }

    /**
     * Called when a packet is sent.
     * Cancel this event for block packet sending.
     * Supported packets see {@link today.opai.api.interfaces.game.network.client}
     *
     * @param event The packet send event.
     */
    public void onPacketSend(EventPacketSend event) {
    }

    /**
     * Called when a packet is received.
     * Cancel this event for block packet receiving.
     * Supported packets see {@link today.opai.api.interfaces.game.network.server}
     *
     * @param event The packet receive event.
     */
    public void onPacketReceive(EventPacketReceive event) {}

    /**
     * Called when rendering in 2D.
     *
     * @param event The 2D render event.
     */
    public void onRender2D(EventRender2D event) {}

    /**
     * Called when rendering in 3D.
     *
     * @param event The 3D render event.
     */
    public void onRender3D(EventRender3D event) {}

}
