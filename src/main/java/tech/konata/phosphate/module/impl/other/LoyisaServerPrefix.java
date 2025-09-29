package tech.konata.phosphate.module.impl.other;

import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.packet.ReceivePacketEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class LoyisaServerPrefix extends Module {

    public boolean registered = false;

    @Handler
    public void onRec(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S01PacketJoinGame) {
            registered = false;

            if (mc.isIntegratedServerRunning())
                return;

//            System.out.println("S01PacketJoinGame, " + mc.getCurrentServerData().serverIP);
            if (mc.getCurrentServerData().serverIP.contains("loyisa") || mc.getCurrentServerData().serverIP.contains(":1337")) {
                MultiThreadingUtil.runAsync(() -> {

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    this.registerChannel();

                });
            }
        }
        if (event.getPacket() instanceof S3FPacketCustomPayload) {
            S3FPacketCustomPayload packet = (S3FPacketCustomPayload) event.getPacket();
//            System.out.println("CustomPayload");

//            System.out.println("packet.getChannelName() = " + packet.getChannelName());

            if (packet.getChannelName().equals("Loyisa|Prefix")) {

                byte[] array = packet.getBufferData().array();

                String content = new String(array, StandardCharsets.UTF_8);

                if (content.equals("LOCK AND LOADED!")) {
                    registered = true;

//                    System.out.println(Arrays.toString(array));
//                    System.out.println(new String(array));
//                System.out.println("packet.getBufferData().readStringFromBuffer(32767) = " + packet.getBufferData().readStringFromBuffer(32767));

                    this.sendPacket(true);
                }
            }

        }
    }

    ;

    public LoyisaServerPrefix() {
        super("LoyisaServerPrefix", Category.OTHER);
        super.setEnabled(true);
        super.setShouldRender(() -> false);
    }

    @Override
    public void onEnable() {
        if (mc.getCurrentServerData() == null)
            return;

        if (mc.getCurrentServerData().serverIP.contains("loyisa") || mc.getCurrentServerData().serverIP.equalsIgnoreCase("mc.remiaft.com:1337")) {

            if (!registered) {
                this.registerChannel();
            } else {
                this.sendPacket(true);

            }

        }
    }

    @Override
    public void onDisable() {

        if (mc.thePlayer == null || mc.theWorld == null || mc.getCurrentServerData() == null)
            return;

        if (mc.getCurrentServerData().serverIP.contains("loyisa") || mc.getCurrentServerData().serverIP.equalsIgnoreCase("mc.remiaft.com:1337")) {
            this.sendPacket(false);
        }
    }

    // register this channel first
    public void registerChannel() {
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeBytes("Loyisa|Prefix".getBytes(StandardCharsets.UTF_8));
        C17PacketCustomPayload payload = new C17PacketCustomPayload("REGISTER", packetbuffer);
        mc.thePlayer.sendQueue.addToSendQueue(payload);
    }

    /**
     * after you get a custompayload with channel name "Loyisa|Prefix" with content "LOCK AND LOADED!"
     * send the packet
     */
    @SneakyThrows
    public void sendPacket(boolean add) {
        // Client name|Username|Action|timestamp|MD5(ClientName+UserName+Action+timestamp+key)
        String clientName = "Konata";
        String username = mc.getSession().getUsername();
        String actionToSend = add ? "ADD" : "REMOVE";
        long timestamp = System.currentTimeMillis();
        String key = "atanoK_";

        MessageDigest md51 = MessageDigest.getInstance("MD5");
        byte[] digest = md51.digest((clientName + username + actionToSend + timestamp + key).getBytes(StandardCharsets.UTF_8));

        String md5 = encodeHexString(digest);
        // append them with "|"
        PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
        packetbuffer.writeString(">" + clientName + "|" + username + "|" + actionToSend + "|" + timestamp + "|" + md5 + "<");
        C17PacketCustomPayload payload = new C17PacketCustomPayload("Loyisa|Prefix", packetbuffer);
        mc.thePlayer.sendQueue.addToSendQueue(payload);
    }

    public String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    public char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public char[] encodeHex(byte[] data, boolean toLowerCase) {

        char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for (int j = 0; i < l; ++i) {
            out[j++] = toDigits[(240 & data[i]) >>> 4];
            out[j++] = toDigits[15 & data[i]];
        }

        return out;
    }
}
