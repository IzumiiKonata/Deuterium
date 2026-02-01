package net.minecraft.network.play.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import today.opai.api.enums.EnumUseEntityAction;
import today.opai.api.interfaces.dataset.Vector3d;
import today.opai.api.interfaces.game.network.client.CPacket02UseEntity;
import tritium.bridge.misc.math.Vector3dImpl;

import java.io.IOException;

public class C02PacketUseEntity implements Packet<INetHandlerPlayServer>, CPacket02UseEntity {
    private int entityId;
    private C02PacketUseEntity.Action action;
    private Vec3 hitVec;

    public C02PacketUseEntity() {
    }

    public C02PacketUseEntity(Entity entity, C02PacketUseEntity.Action action) {
        this.entityId = entity.getEntityId();
        this.action = action;
    }

    public C02PacketUseEntity(int entityId, C02PacketUseEntity.Action action) {
        this.entityId = entityId;
        this.action = action;
    }

    public C02PacketUseEntity(Entity entity, Vec3 hitVec) {
        this(entity, C02PacketUseEntity.Action.INTERACT_AT);
        this.hitVec = hitVec;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.entityId = buf.readVarIntFromBuffer();
        this.action = buf.readEnumValue(Action.class);

        if (this.action == C02PacketUseEntity.Action.INTERACT_AT) {
            this.hitVec = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(this.entityId);
        buf.writeEnumValue(this.action);

        if (this.action == C02PacketUseEntity.Action.INTERACT_AT) {
            buf.writeFloat((float) this.hitVec.xCoord);
            buf.writeFloat((float) this.hitVec.yCoord);
            buf.writeFloat((float) this.hitVec.zCoord);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler) {
        handler.processUseEntity(this);
    }

    public Entity getEntityFromWorld(World worldIn) {
        return worldIn.getEntityByID(this.entityId);
    }

    public C02PacketUseEntity.Action getPacketAction() {
        return this.action;
    }

    public Vec3 getVec() {
        return this.hitVec;
    }

    public enum Action {
        INTERACT,
        ATTACK,
        INTERACT_AT;

        public EnumUseEntityAction toOpai() {
            return switch (this) {
                case INTERACT -> EnumUseEntityAction.INTERACT;
                case ATTACK -> EnumUseEntityAction.ATTACK;
                case INTERACT_AT -> EnumUseEntityAction.INTERACT_AT;
                default -> throw new IllegalArgumentException("Unknown use entity action: " + this);
            };
        }
    }

    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public EnumUseEntityAction getAction() {
        return this.getPacketAction().toOpai();
    }

    @Override
    public Vector3d getHitVec() {
        return new Vector3dImpl(this.hitVec.xCoord, this.hitVec.yCoord, this.hitVec.zCoord);
    }
}
