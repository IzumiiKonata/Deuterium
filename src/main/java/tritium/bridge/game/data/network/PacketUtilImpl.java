package tritium.bridge.game.data.network;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import today.opai.api.enums.EnumDiggingAction;
import today.opai.api.enums.EnumDirection;
import today.opai.api.enums.EnumEntityAction;
import today.opai.api.interfaces.dataset.Vector2f;
import today.opai.api.interfaces.dataset.Vector3d;
import today.opai.api.interfaces.dataset.Vector3i;
import today.opai.api.interfaces.game.item.ItemStack;
import today.opai.api.interfaces.game.network.PacketUtil;
import today.opai.api.interfaces.game.network.client.*;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 18:39
 */
public class PacketUtilImpl implements PacketUtil {

    @Getter
    private static final PacketUtil instance = new PacketUtilImpl();

    @Override
    public CPacket01Chat createChat(CharSequence message) {
        return new C01PacketChatMessage(message.toString());
    }

    @Override
    public CPacket00KeepAlive createKeepAlive(int key) {
        return new C00PacketKeepAlive(key);
    }

    @Override
    public CPacket0ASwing createSwing() {
        return new C0APacketAnimation();
    }

    @Override
    public CPacket0BEntityAction createEntityAction(EnumEntityAction action) {
        return new C0BPacketEntityAction(Minecraft.getMinecraft().thePlayer, C0BPacketEntityAction.Action.fromOpai(action));
    }

    @Override
    public CPacket0DCloseWindow createCloseWindow(int windowsId) {
        return new C0DPacketCloseWindow(windowsId);
    }

    @Override
    public CPacket0FTransaction createTransaction(int windowId, short uid, boolean accepted) {
        return new C0FPacketConfirmTransaction(windowId, uid, accepted);
    }

    @Override
    public CPacket0EClickWindow createClickWindow(int windowId, int slotId, int usedButton, int mode, ItemStack clickedItem, short actionNumber) {
        return new C0EPacketClickWindow(windowId, slotId, usedButton, mode, net.minecraft.item.ItemStack.fromOpai(clickedItem), actionNumber);
    }

    @Override
    public CPacket03Player createPlayer(boolean onGround) {
        return new C03PacketPlayer(onGround);
    }

    @Override
    public CPacket04Position createPlayerPosition(Vector3d positionData, boolean onGround) {
        return new C03PacketPlayer.C04PacketPlayerPosition(positionData.getX(), positionData.getY(), positionData.getZ(), onGround);
    }

    @Override
    public CPacket04Position createPlayerPosition(double x, double y, double z, boolean onGround) {
        return new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, onGround);
    }

    @Override
    public CPacket05Rotation createPlayerRotation(Vector2f rotationData, boolean onGround) {
        return new C03PacketPlayer.C05PacketPlayerLook(rotationData.getX(), rotationData.getY(), onGround);
    }

    @Override
    public CPacket05Rotation createPlayerRotation(float yaw, float pitch, boolean onGround) {
        return new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, onGround);
    }

    @Override
    public CPacket06PositionRotation createPlayerPositionRotation(Vector3d positionData, Vector2f rotationData, boolean onGround) {
        return new C03PacketPlayer.C06PacketPlayerPosLook(positionData.getX(), positionData.getY(), positionData.getZ(), rotationData.getX(), rotationData.getY(), onGround);
    }

    @Override
    public CPacket06PositionRotation createPlayerPositionRotation(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        return new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, yaw, pitch, onGround);
    }

    @Override
    public CPacket07Digging createDigging(Vector3i position, EnumDiggingAction action, EnumDirection direction) {
        return new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.fromEnumDiggingAction(action), new BlockPos(position.getX(), position.getY(), position.getZ()), EnumFacing.fromEnumDirection(direction));
    }

    @Override
    public CPacket08Placement createUseItem(ItemStack stack) {
        return new C08PacketPlayerBlockPlacement(net.minecraft.item.ItemStack.fromOpai(stack));
    }

    @Override
    public CPacket09SlotChange createSwitchItem(int slot) {
        return new C09PacketHeldItemChange(slot);
    }
}
