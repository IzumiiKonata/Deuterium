package tritium.bridge.entity;

import net.minecraft.entity.EntityLivingBase;
import today.opai.api.interfaces.game.entity.LivingEntity;
import today.opai.api.interfaces.game.item.ItemStack;
import today.opai.api.interfaces.game.item.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:44
 */
public class LivingEntityWrapper<T extends EntityLivingBase> extends EntityWrapper<EntityLivingBase> implements LivingEntity {

    public LivingEntityWrapper(T mcEntity) {
        super(mcEntity);
    }

    @Override
    public float getAbsorption() {
        return mcEntity.getAbsorption();
    }

    @Override
    public float getMoveForward() {
        return mcEntity.moveForward;
    }

    @Override
    public float getMoveStrafing() {
        return mcEntity.moveStrafing;
    }

    @Override
    public void setMoveForward(float forward) {
        mcEntity.moveForward = forward;
    }

    @Override
    public void setMoveStrafing(float strafing) {
        mcEntity.moveStrafing = strafing;
    }

    @Override
    public float getHealth() {
        return mcEntity.getHealth();
    }

    @Override
    public float getMaxHealth() {
        return mcEntity.getMaxHealth();
    }

    @Override
    public boolean isOnLadder() {
        return mcEntity.isOnLadder();
    }

    @Override
    public int getHurtTime() {
        return mcEntity.hurtTime;
    }

    @Override
    public void setJumpMovementFactor(float factor) {
        mcEntity.jumpMovementFactor = factor;
    }

    @Override
    public float getJumpMovementFactor() {
        return mcEntity.jumpMovementFactor;
    }

    @Override
    public ItemStack getArmorSlot(int slot) {
        return this.mcEntity.getCurrentArmor(slot).getWrapper();
    }

    @Override
    public ItemStack getHeldItem() {
        return this.mcEntity.getHeldItem().getWrapper();
    }

    @Override
    public float getEyeHeight() {
        return mcEntity.getEyeHeight();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return this.mcEntity.getActivePotionEffects().stream().map(net.minecraft.potion.PotionEffect::getWrapper).collect(Collectors.toList());
    }

    @Override
    public float getSwingProgress() {
        return mcEntity.getSwingProgress();
    }
}
