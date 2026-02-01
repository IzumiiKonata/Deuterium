package net.minecraft.network.play.server;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import today.opai.api.dataset.Vec3Data;
import today.opai.api.interfaces.game.network.server.SPacket23BlockChange;

import java.io.IOException;

public class S23PacketBlockChange implements Packet<INetHandlerPlayClient>, SPacket23BlockChange {
    private BlockPos blockPosition;
    private IBlockState blockState;

    public S23PacketBlockChange() {
    }

    public S23PacketBlockChange(World worldIn, BlockPos blockPositionIn) {
        this.blockPosition = blockPositionIn;
        this.blockState = worldIn.getBlockState(blockPositionIn);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.blockPosition = buf.readBlockPos();
        this.blockState = Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPosition);
        buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(this.blockState));
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleBlockChange(this);
    }

    public IBlockState getBlockState() {
        return this.blockState;
    }

    public BlockPos getBlockPosition() {
        return this.blockPosition;
    }

    @Override
    public today.opai.api.interfaces.game.world.Block getBlock() {
        return this.blockState.getBlock().getWrapper();
    }

    @Override
    public Vec3Data getPosition() {
        return new Vec3Data(this.blockPosition.getX(), this.blockPosition.getY(), this.blockPosition.getZ());
    }
}
