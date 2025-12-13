package net.minecraft.network.play.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;


import java.io.IOException;

public class S12PacketEntityVelocity implements Packet<INetHandlerPlayClient> {
    private int entityID;
    public double motionX;
    public double motionY;
    public double motionZ;

    public S12PacketEntityVelocity() {
    }

    public S12PacketEntityVelocity(Entity entityIn) {
        this(entityIn.getEntityId(), entityIn.motionX, entityIn.motionY, entityIn.motionZ);
    }

    public S12PacketEntityVelocity(int entityIDIn, double motionXIn, double motionYIn, double motionZIn) {
        this.entityID = entityIDIn;
        double d0 = 3.9D;

        if (motionXIn < -d0) {
            motionXIn = -d0;
        }

        if (motionYIn < -d0) {
            motionYIn = -d0;
        }

        if (motionZIn < -d0) {
            motionZIn = -d0;
        }

        if (motionXIn > d0) {
            motionXIn = d0;
        }

        if (motionYIn > d0) {
            motionYIn = d0;
        }

        if (motionZIn > d0) {
            motionZIn = d0;
        }

        this.motionX = (int) (motionXIn * 8000.0D);
        this.motionY = (int) (motionYIn * 8000.0D);
        this.motionZ = (int) (motionZIn * 8000.0D);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityID = buf.readVarIntFromBuffer();
        this.motionX = buf.readShort();
        this.motionY = buf.readShort();
        this.motionZ = buf.readShort();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entityID);
        buf.writeShort((int) this.motionX);
        buf.writeShort((int) this.motionY);
        buf.writeShort((int) this.motionZ);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityVelocity(this);
    }

    public int getEntityID() {
        return this.entityID;
    }

    public double getMotionX() {
        return this.motionX;
    }

    public double getMotionY() {
        return this.motionY;
    }

    public double getMotionZ() {
        return this.motionZ;
    }

    public int getEntityId() {
        return this.entityID;
    }

    public boolean isCurrentEntity() {
        return Minecraft.getMinecraft().thePlayer.getEntityId() == this.entityID;
    }

    public double getX() {
        return this.motionX;
    }
    public double getY() {
        return this.motionY;
    }
    public double getZ() {
        return this.motionZ;
    }

    public void setX(double x) {
        this.motionX = x;
    }
    public void setY(double y) {
        this.motionY = y;
    }
    public void setZ(double z) {
        this.motionZ = z;
    }

}
