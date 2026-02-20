package net.minecraft.network.play.server;

import net.minecraft.entity.DataWatcher;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;
import java.util.List;

public class S1CPacketEntityMetadata implements Packet<INetHandlerPlayClient> {
    private int entityId;
    private List<DataWatcher.WatchableObject> objects;

    public S1CPacketEntityMetadata() {
    }

    public S1CPacketEntityMetadata(int entityIdIn, DataWatcher watcher, boolean fullUpdate) {
        this.entityId = entityIdIn;

        if (fullUpdate) {
            this.objects = watcher.getAllWatched();
        } else {
            this.objects = watcher.getChanged();
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarIntFromBuffer();
        this.objects = DataWatcher.readWatchedListFromPacketBuffer(buf);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entityId);
        DataWatcher.writeWatchedListToPacketBuffer(this.objects, buf);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityMetadata(this);
    }

    public List<DataWatcher.WatchableObject> func_149376_c() {
        return this.objects;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
