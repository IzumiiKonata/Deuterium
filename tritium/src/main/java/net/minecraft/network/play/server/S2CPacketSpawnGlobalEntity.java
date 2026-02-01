package net.minecraft.network.play.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.MathHelper;
import today.opai.api.interfaces.game.network.server.SPacket2CEntitySpawn;

import java.io.IOException;

public class S2CPacketSpawnGlobalEntity implements Packet<INetHandlerPlayClient>, SPacket2CEntitySpawn {
    private int entityId;
    private int x;
    private int y;
    private int z;
    private int type;

    public S2CPacketSpawnGlobalEntity() {
    }

    public S2CPacketSpawnGlobalEntity(Entity entityIn) {
        this.entityId = entityIn.getEntityId();
        this.x = MathHelper.floor_double(entityIn.posX * 32.0D);
        this.y = MathHelper.floor_double(entityIn.posY * 32.0D);
        this.z = MathHelper.floor_double(entityIn.posZ * 32.0D);

        if (entityIn instanceof EntityLightningBolt) {
            this.type = 1;
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.entityId = buf.readVarIntFromBuffer();
        this.type = buf.readByte();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(this.entityId);
        buf.writeByte(this.type);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSpawnGlobalEntity(this);
    }

    public int func_149052_c() {
        return this.entityId;
    }

    public int func_149051_d() {
        return this.x;
    }

    public int func_149050_e() {
        return this.y;
    }

    public int func_149049_f() {
        return this.z;
    }

    public int func_149053_g() {
        return this.type;
    }

    @Override
    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }
}
