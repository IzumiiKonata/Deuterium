package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S1FPacketSetExperience implements Packet<INetHandlerPlayClient> {
    private float experience;
    private int totalExperience;
    private int level;

    public S1FPacketSetExperience() {
    }

    public S1FPacketSetExperience(float exp, int totalExperienceIn, int levelIn) {
        this.experience = exp;
        this.totalExperience = totalExperienceIn;
        this.level = levelIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.experience = buf.readFloat();
        this.level = buf.readVarIntFromBuffer();
        this.totalExperience = buf.readVarIntFromBuffer();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeFloat(this.experience);
        buf.writeVarIntToBuffer(this.level);
        buf.writeVarIntToBuffer(this.totalExperience);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleSetExperience(this);
    }

    public float getExperience() {
        return this.experience;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public int getLevel() {
        return this.level;
    }
}
