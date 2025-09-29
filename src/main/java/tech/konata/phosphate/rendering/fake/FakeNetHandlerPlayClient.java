package tech.konata.phosphate.rendering.fake;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.EnumPacketDirection;

import java.util.UUID;

public class FakeNetHandlerPlayClient extends NetHandlerPlayClient {
    private NetworkPlayerInfo playerInfo;

    public FakeNetHandlerPlayClient(final Minecraft mcIn) {
        super(mcIn, mcIn.currentScreen, new FakeNetworkManager(EnumPacketDirection.CLIENTBOUND), mcIn.getSession().getProfile());
        this.playerInfo = new NetworkPlayerInfo(mcIn.getSession().getProfile());
    }

    public NetworkPlayerInfo getPlayerInfo(final UUID uniqueId) {
        return this.playerInfo;
    }

    public NetworkPlayerInfo getPlayerInfo(final String name) {
        return this.playerInfo;
    }
}
