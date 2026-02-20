package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S2BPacketChangeGameState implements Packet<INetHandlerPlayClient> {
    public static final String[] MESSAGE_NAMES = new String[]{"tile.bed.notValid"};
    private int state;
    private float parameter;

    public S2BPacketChangeGameState() {
    }

    public S2BPacketChangeGameState(int stateIn, float param) {
        this.state = stateIn;
        this.parameter = param;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.state = buf.readUnsignedByte();
        this.parameter = buf.readFloat();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeByte(this.state);
        buf.writeFloat(this.parameter);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleChangeGameState(this);
    }

    public int getGameState() {
        return this.state;
    }

    public float getParameter() {
        return this.parameter;
    }
}
