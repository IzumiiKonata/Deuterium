package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import today.opai.api.interfaces.game.network.server.SPacket0BAnimation;

import java.io.IOException;

public class S0BPacketAnimation implements Packet<INetHandlerPlayClient>, SPacket0BAnimation {
    private int entityId;
    private short type;

    public S0BPacketAnimation() {
    }

    public S0BPacketAnimation(Entity ent, int animationType) {
        this.entityId = ent.getEntityId();
        this.type = (short) animationType;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.entityId = buf.readVarIntFromBuffer();
        this.type = buf.readUnsignedByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(this.entityId);
        buf.writeByte(this.type);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleAnimation(this);
    }

    public int getEntityID() {
        return this.entityId;
    }

    public short getAnimationType() {
        return this.type;
    }

    @Override
    public byte getAnimation() {
        return (byte) this.getAnimationType();
    }

    @Override
    public int getEntityId() {
        return this.getEntityID();
    }

    @Override
    public void setAnimation(byte animation) {
        this.type = animation;
    }
}
