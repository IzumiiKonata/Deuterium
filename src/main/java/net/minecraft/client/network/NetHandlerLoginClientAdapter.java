package net.minecraft.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;

/**
 * @author IzumiiKonata
 * Date: 2025/11/12 21:18
 */
public class NetHandlerLoginClientAdapter extends NetHandlerLoginClient {

    public NetHandlerLoginClientAdapter(NetworkManager networkManagerIn, Minecraft mcIn, GuiScreen p_i45059_3_) {
        super(networkManagerIn, mcIn, p_i45059_3_);
    }

    @Override
    public void handleEncryptionRequest(S01PacketEncryptionRequest packetIn) {
        super.handleEncryptionRequest(packetIn);

    }

    @Override
    public void handleDisconnect(S00PacketDisconnect packetIn) {
        super.handleDisconnect(packetIn);

    }

    @Override
    public void handleLoginSuccess(S02PacketLoginSuccess packetIn) {
        super.handleLoginSuccess(packetIn);


        if (mc.theWorld != null) {
            this.mc.theWorld.sendQuittingDisconnectingPacket();
//            this.mc.playerController.setNoCheckDisconnect(true);
        }

        mc.addScheduledTask(() -> mc.loadWorld(null));

//        this.mc.loadWorld(null);
    }

    @Override
    public void handleEnableCompression(S03PacketEnableCompression packetIn) {
        super.handleEnableCompression(packetIn);

    }
}
