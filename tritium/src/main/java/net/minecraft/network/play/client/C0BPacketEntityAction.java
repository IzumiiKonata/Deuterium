package net.minecraft.network.play.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import today.opai.api.enums.EnumEntityAction;
import today.opai.api.interfaces.game.network.client.CPacket0BEntityAction;

import java.io.IOException;

public class C0BPacketEntityAction implements Packet<INetHandlerPlayServer>, CPacket0BEntityAction {
    private int entityID;
    private C0BPacketEntityAction.Action action;
    private int auxData;

    public C0BPacketEntityAction() {
    }

    public C0BPacketEntityAction(Entity entity, C0BPacketEntityAction.Action action) {
        this(entity, action, 0);
    }

    public C0BPacketEntityAction(Entity entity, C0BPacketEntityAction.Action action, int auxData) {
        this.entityID = entity.getEntityId();
        this.action = action;
        this.auxData = auxData;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.entityID = buf.readVarIntFromBuffer();
        this.action = buf.readEnumValue(Action.class);
        this.auxData = buf.readVarIntFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(this.entityID);
        buf.writeEnumValue(this.action);
        buf.writeVarIntToBuffer(this.auxData);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler) {
        handler.processEntityAction(this);
    }

    public EnumEntityAction getAction() {
        return this.action.toOpai();
    }

    public int getAuxData() {
        return this.auxData;
    }

    public enum Action {
        START_SNEAKING,
        STOP_SNEAKING,
        STOP_SLEEPING,
        START_SPRINTING,
        STOP_SPRINTING,
        RIDING_JUMP,
        OPEN_INVENTORY;

        public static Action fromOpai(EnumEntityAction action) {
            return switch (action) {
                case START_SNEAKING -> START_SNEAKING;
                case STOP_SNEAKING -> STOP_SNEAKING;
                case START_SPRINTING -> START_SPRINTING;
                case STOP_SPRINTING -> STOP_SPRINTING;
                case RIDING_JUMP -> RIDING_JUMP;
                case OPEN_INVENTORY -> OPEN_INVENTORY;
                case STOP_SLEEPING -> STOP_SLEEPING;
                default -> throw new IllegalArgumentException();
            };
        }

        public EnumEntityAction toOpai() {
            return switch (this) {
                case START_SNEAKING -> EnumEntityAction.START_SNEAKING;
                case STOP_SNEAKING -> EnumEntityAction.STOP_SNEAKING;
                case START_SPRINTING -> EnumEntityAction.START_SPRINTING;
                case STOP_SPRINTING -> EnumEntityAction.STOP_SPRINTING;
                case RIDING_JUMP -> EnumEntityAction.RIDING_JUMP;
                case OPEN_INVENTORY -> EnumEntityAction.OPEN_INVENTORY;
                case STOP_SLEEPING -> EnumEntityAction.STOP_SLEEPING;
                default -> throw new IllegalArgumentException();
            };
        }
    }

    public Action getPacketAction() {
        return this.action;
    }

    @Override
    public void setAction(EnumEntityAction action) {
        this.action = Action.fromOpai(action);
    }
}
