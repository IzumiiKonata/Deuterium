package tritium.bridge.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import today.opai.api.dataset.RotationData;
import today.opai.api.enums.EnumResource;
import today.opai.api.enums.EnumShopItem;
import today.opai.api.interfaces.dataset.Vector3d;
import today.opai.api.interfaces.game.Inventory;
import today.opai.api.interfaces.game.entity.Entity;
import today.opai.api.interfaces.game.entity.LocalPlayer;
import today.opai.api.interfaces.game.entity.raytrace.RaytraceResult;
import tritium.bridge.misc.math.Vector3dImpl;
import tritium.utils.player.PlayerUtils;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:08
 */
public class LocalPlayerWrapper<T extends EntityPlayerSP> extends PlayerWrapper<EntityPlayerSP> implements LocalPlayer {

    public LocalPlayerWrapper(T player) {
        super(player);
    }

    @Override
    public EntityPlayerSP getMcEntity() {
        return (EntityPlayerSP) super.getMcEntity();
    }

    @Override
    public void attack(Entity target) {
        this.getMcEntity().attackTargetEntityWithCurrentItem(this.getMcEntity().worldObj.getEntityByID(target.getEntityId()));
    }

    @Override
    public Inventory getInventory() {
        return this.getMcEntity().inventory.getWrapper();
    }

    @Override
    public void sendChatMessage(String message) {
        this.getMcEntity().sendChatMessage(message);
    }

    @Override
    public void jump() {
        this.getMcEntity().jump();
    }

    @Override
    public void swingItem() {
        this.getMcEntity().swingItem();
    }

    @Override
    public void setSpeed(double speed) {
        this.getMcEntity().setMotion(speed);
    }

    @Override
    public int getItemSlot() {
        return this.getMcEntity().inventory.currentItem;
    }

    @Override
    public void setItemSlot(int slotId) {
        this.getMcEntity().inventory.currentItem = slotId;
    }

    @Override
    public void clickWindow(int slot, int button, int mode) {
        Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().thePlayer.inventoryContainer.windowId, slot, button, mode, Minecraft.getMinecraft().thePlayer);
    }

    @Override
    public void clickMouse() {
        Minecraft.getMinecraft().clickMouse();
    }

    @Override
    public void rightClickMouse() {
        Minecraft.getMinecraft().rightClickMouse();
    }

    @Override
    public void useItem() {
        Minecraft.getMinecraft().playerController.sendUseItem(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer.getHeldItem());
    }

    @Override
    public Vector3d getViewPosition() {
        return new Vector3dImpl(Minecraft.getMinecraft().getRenderManager().renderPosX, Minecraft.getMinecraft().getRenderManager().renderPosY, Minecraft.getMinecraft().getRenderManager().renderPosZ);
    }

    @Override
    public float getPlayerViewY() {
        return Minecraft.getMinecraft().getRenderManager().playerViewY;
    }

    @Override
    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2875D;
        if (this.getMcEntity().isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1.0D + 0.2D * (this.getMcEntity().getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        return baseSpeed;
    }

    @Override
    public boolean isBedWarsShopScreen() {
        return false;
    }

    @Override
    public int countResource(EnumResource resource) {
        return 0;
    }

    @Override
    public void purchase(EnumShopItem item) {

    }

    @Override
    public RaytraceResult raytrace(RotationData rotation, double range, float expand, boolean allowThroughWalls) {
        return PlayerUtils.rayTrace(rotation.getYaw(), rotation.getPitch(), range).getRaytraceResult();
    }
}
