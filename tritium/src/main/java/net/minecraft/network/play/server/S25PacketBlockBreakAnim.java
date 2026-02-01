package net.minecraft.network.play.server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import today.opai.api.interfaces.game.network.server.SPacket25BlockBreak;
import today.opai.api.interfaces.game.world.Block;

import java.io.IOException;

public class S25PacketBlockBreakAnim implements Packet<INetHandlerPlayClient>, SPacket25BlockBreak {
    private int breakerId;
    private BlockPos position;
    private int progress;

    public S25PacketBlockBreakAnim() {
    }

    public S25PacketBlockBreakAnim(int breakerId, BlockPos pos, int progress) {
        this.breakerId = breakerId;
        this.position = pos;
        this.progress = progress;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.breakerId = buf.readVarIntFromBuffer();
        this.position = buf.readBlockPos();
        this.progress = buf.readUnsignedByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeVarIntToBuffer(this.breakerId);
        buf.writeBlockPos(this.position);
        buf.writeByte(this.progress);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleBlockBreakAnim(this);
    }

    public int getBreakerId() {
        return this.breakerId;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getProgress() {
        return this.progress;
    }

    @Override
    public Block getBlock() {
        return Minecraft.getMinecraft().theWorld.getBlockState(this.position).getBlock().getWrapper();
    }

    @Override
    public int getEntityId() {
        return this.breakerId;
    }
}
