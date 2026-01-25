package net.minecraft.network.play.server;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;
import today.opai.api.interfaces.game.network.server.SPacket02Chat;

import java.io.IOException;

public class S02PacketChat implements Packet<INetHandlerPlayClient>, SPacket02Chat {
    private IChatComponent chatComponent;
    private byte type;

    public S02PacketChat() {
    }

    public S02PacketChat(IChatComponent component) {
        this(component, (byte) 1);
    }

    public S02PacketChat(IChatComponent message, byte typeIn) {
        this.chatComponent = message;
        this.type = typeIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.chatComponent = buf.readChatComponent();
        this.type = buf.readByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeChatComponent(this.chatComponent);
        buf.writeByte(this.type);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleChat(this);
    }

    public IChatComponent getChatComponent() {
        return this.chatComponent;
    }

    public boolean isChat() {
        return this.type == 1 || this.type == 2;
    }

    /**
     * Returns the id of the area to display the text, 2 for above the action bar, anything else currently for the chat
     * window
     */
    public byte getType() {
        return this.type;
    }

    @Override
    public String getMessage() {
        return IChatComponent.Serializer.componentToJson(this.chatComponent);
    }

    @Override
    public void setType(byte b) {
        this.type = b;
    }

    @Override
    public void setMessage(String message) {
        this.chatComponent = IChatComponent.Serializer.jsonToComponent(message);
    }
}
