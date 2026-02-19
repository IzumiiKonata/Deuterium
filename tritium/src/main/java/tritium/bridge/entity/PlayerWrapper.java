package tritium.bridge.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import today.opai.api.interfaces.game.entity.Player;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:03
 */
public class PlayerWrapper<T extends EntityPlayer> extends LivingEntityWrapper<EntityPlayer> implements Player {

    public PlayerWrapper(T mcEntity) {
        super(mcEntity);
    }

    public EntityPlayer getMcEntity() {
        return (EntityPlayer) super.getMcEntity();
    }

    @Override
    public boolean canFlying() {
        return this.getMcEntity().capabilities.allowFlying;
    }

    @Override
    public boolean getFlying() {
        return this.getMcEntity().capabilities.isFlying;
    }

    @Override
    public boolean isSneaking() {
        return this.mcEntity.isSneaking();
    }

    @Override
    public void setSneaking(boolean sneaking) {
        this.mcEntity.setSneaking(sneaking);
    }

    @Override
    public boolean isSprinting() {
        return this.mcEntity.isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        this.mcEntity.setSprinting(sprinting);
    }

    @Override
    public void setItemInUseCount(int count) {
        this.getMcEntity().itemInUseCount = count;
    }

    @Override
    public int getItemInUseCount() {
        return this.getMcEntity().itemInUseCount;
    }

    @Override
    public boolean isBlocking() {
        return this.getMcEntity().isBlocking();
    }

    @Override
    public boolean isUsingItem() {
        return this.getMcEntity().isUsingItem();
    }

    @Override
    public int getFoodLevel() {
        return this.getMcEntity().getFoodStats().getFoodLevel();
    }

    @Override
    public String getProfileName() {
        return this.getMcEntity().getGameProfile().getName();
    }
}
