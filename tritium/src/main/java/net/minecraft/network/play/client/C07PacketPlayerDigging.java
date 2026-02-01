package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import today.opai.api.enums.EnumDiggingAction;
import today.opai.api.enums.EnumDirection;
import today.opai.api.interfaces.dataset.Vector3i;
import today.opai.api.interfaces.game.network.client.CPacket07Digging;
import tritium.bridge.misc.math.Vector3iImpl;

import java.io.IOException;

public class C07PacketPlayerDigging implements Packet<INetHandlerPlayServer>, CPacket07Digging {
    private BlockPos position;
    private EnumFacing facing;

    /**
     * Status of the digging (started, ongoing, broken).
     */
    private C07PacketPlayerDigging.Action status;

    public C07PacketPlayerDigging() {
    }

    public C07PacketPlayerDigging(C07PacketPlayerDigging.Action statusIn, BlockPos posIn, EnumFacing facingIn) {
        this.status = statusIn;
        this.position = posIn;
        this.facing = facingIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) {
        this.status = buf.readEnumValue(Action.class);
        this.position = buf.readBlockPos();
        this.facing = EnumFacing.getFront(buf.readUnsignedByte());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) {
        buf.writeEnumValue(this.status);
        buf.writeBlockPos(this.position);
        buf.writeByte(this.facing.getIndex());
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler) {
        handler.processPlayerDigging(this);
    }

    public BlockPos getPos() {
        return this.position;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    public C07PacketPlayerDigging.Action getStatus() {
        return this.status;
    }

    public enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM;

        public EnumDiggingAction toEnumDiggingAction() {
            return switch (this) {
                case START_DESTROY_BLOCK -> EnumDiggingAction.START_DESTROY_BLOCK;
                case ABORT_DESTROY_BLOCK -> EnumDiggingAction.ABORT_DESTROY_BLOCK;
                case STOP_DESTROY_BLOCK -> EnumDiggingAction.STOP_DESTROY_BLOCK;
                case DROP_ALL_ITEMS -> EnumDiggingAction.DROP_ALL_ITEMS;
                case DROP_ITEM -> EnumDiggingAction.DROP_ITEM;
                case RELEASE_USE_ITEM -> EnumDiggingAction.RELEASE_USE_ITEM;
            };
        }

        public static Action fromEnumDiggingAction(EnumDiggingAction action) {
            return switch (action) {
                case START_DESTROY_BLOCK -> START_DESTROY_BLOCK;
                case ABORT_DESTROY_BLOCK -> ABORT_DESTROY_BLOCK;
                case STOP_DESTROY_BLOCK -> STOP_DESTROY_BLOCK;
                case DROP_ALL_ITEMS -> DROP_ALL_ITEMS;
                case DROP_ITEM -> DROP_ITEM;
                case RELEASE_USE_ITEM -> RELEASE_USE_ITEM;
            };
        }
    }

    @Override
    public EnumDiggingAction getAction() {
        return this.status.toEnumDiggingAction();
    }

    @Override
    public Vector3i getPosition() {
        return new Vector3iImpl(this.position.getX(), this.position.getY(), this.position.getZ());
    }

    @Override
    public EnumDirection getDirection() {
        return this.facing.toEnumDirection();
    }
}
