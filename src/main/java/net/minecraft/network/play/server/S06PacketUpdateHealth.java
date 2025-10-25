package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import today.opai.api.interfaces.game.network.server.SPacket06UpdateHealth;

import java.io.IOException;

public class S06PacketUpdateHealth implements Packet<INetHandlerPlayClient>, SPacket06UpdateHealth {
    private float health;
    private int foodLevel;
    private float saturationLevel;

    public S06PacketUpdateHealth() {
    }

    public S06PacketUpdateHealth(float healthIn, int foodLevelIn, float saturationIn) {
        this.health = healthIn;
        this.foodLevel = foodLevelIn;
        this.saturationLevel = saturationIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.health = buf.readFloat();
        this.foodLevel = buf.readVarIntFromBuffer();
        this.saturationLevel = buf.readFloat();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeFloat(this.health);
        buf.writeVarIntToBuffer(this.foodLevel);
        buf.writeFloat(this.saturationLevel);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleUpdateHealth(this);
    }

    public float getHealth() {
        return this.health;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    @Override
    public float getSaturation() {
        return this.getSaturationLevel();
    }

    @Override
    public void setHealth(float health) {
        this.health = health;
    }

    @Override
    public void setSaturation(float saturation) {
        this.saturationLevel = saturation;
    }

    @Override
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }
}
