package tritium.bridge.entity;

import net.minecraft.entity.player.EntityPlayer;
import today.opai.api.interfaces.game.entity.Player;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:03
 */
public class PlayerWrapper<T extends EntityPlayer> extends LivingEntityWrapper<T> implements Player {

    public PlayerWrapper(T mcEntity) {
        super(mcEntity);
    }

    @Override
    public boolean canFlying() {
        return ((EntityPlayer) this.mcEntity).capabilities.allowFlying;
    }

    @Override
    public boolean getFlying() {
        return ((EntityPlayer) this.mcEntity).capabilities.isFlying;
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
        ((EntityPlayer) this.mcEntity).itemInUseCount = count;
    }

    @Override
    public int getItemInUseCount() {
        return ((EntityPlayer) this.mcEntity).itemInUseCount;
    }

    @Override
    public boolean isBlocking() {
        return ((EntityPlayer) this.mcEntity).isBlocking();
    }

    @Override
    public boolean isUsingItem() {
        return ((EntityPlayer) this.mcEntity).isUsingItem();
    }

    @Override
    public int getFoodLevel() {
        return ((EntityPlayer) this.mcEntity).getFoodStats().getFoodLevel();
    }

    @Override
    public String getProfileName() {
        return ((EntityPlayer) this.mcEntity).getGameProfile().getName();
    }
}
