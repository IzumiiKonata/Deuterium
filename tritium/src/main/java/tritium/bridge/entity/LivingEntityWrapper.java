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
    public EntityLivingBase getMcEntity() {
        return super.getMcEntity();
    }

    @Override
    public float getAbsorption() {
        return this.getMcEntity().getAbsorption();
    }

    @Override
    public float getMoveForward() {
        return this.getMcEntity().moveForward;
    }

    @Override
    public float getMoveStrafing() {
        return this.getMcEntity().moveStrafing;
    }

    @Override
    public void setMoveForward(float forward) {
        this.getMcEntity().moveForward = forward;
    }

    @Override
    public void setMoveStrafing(float strafing) {
        this.getMcEntity().moveStrafing = strafing;
    }

    @Override
    public float getHealth() {
        return this.getMcEntity().getHealth();
    }

    @Override
    public float getMaxHealth() {
        return this.getMcEntity().getMaxHealth();
    }

    @Override
    public boolean isOnLadder() {
        return this.getMcEntity().isOnLadder();
    }

    @Override
    public int getHurtTime() {
        return this.getMcEntity().hurtTime;
    }

    @Override
    public void setJumpMovementFactor(float factor) {
        this.getMcEntity().jumpMovementFactor = factor;
    }

    @Override
    public float getJumpMovementFactor() {
        return this.getMcEntity().jumpMovementFactor;
    }

    @Override
    public ItemStack getArmorSlot(int slot) {
        return this.getMcEntity().getCurrentArmor(slot).getWrapper();
    }

    @Override
    public ItemStack getHeldItem() {
        return this.getMcEntity().getHeldItem().getWrapper();
    }

    @Override
    public float getEyeHeight() {
        return this.getMcEntity().getEyeHeight();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return this.getMcEntity().getActivePotionEffects().stream().map(net.minecraft.potion.PotionEffect::getWrapper).collect(Collectors.toList());
    }

    @Override
    public float getSwingProgress() {
        return this.getMcEntity().getSwingProgress();
    }
}
