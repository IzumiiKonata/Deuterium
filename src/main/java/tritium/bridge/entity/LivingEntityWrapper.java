package tritium.bridge.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import today.opai.api.interfaces.game.entity.LivingEntity;
import today.opai.api.interfaces.game.item.ItemStack;
import today.opai.api.interfaces.game.item.PotionEffect;

import java.util.Collections;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:44
 */
public class LivingEntityWrapper extends EntityWrapper implements LivingEntity {

    public LivingEntityWrapper(EntityLivingBase mcEntity) {
        super(mcEntity);
    }

    @Override
    public float getAbsorption() {
        return ((EntityLivingBase) mcEntity).getAbsorption();
    }

    @Override
    public float getMoveForward() {
        return ((EntityLivingBase) mcEntity).moveForward;
    }

    @Override
    public float getMoveStrafing() {
        return ((EntityLivingBase) mcEntity).moveStrafing;
    }

    @Override
    public void setMoveForward(float forward) {
        ((EntityLivingBase) mcEntity).moveForward = forward;
    }

    @Override
    public void setMoveStrafing(float strafing) {
        ((EntityLivingBase) mcEntity).moveStrafing = strafing;
    }

    @Override
    public float getHealth() {
        return ((EntityLivingBase) mcEntity).getHealth();
    }

    @Override
    public float getMaxHealth() {
        return ((EntityLivingBase) mcEntity).getMaxHealth();
    }

    @Override
    public boolean isOnLadder() {
        return ((EntityLivingBase) mcEntity).isOnLadder();
    }

    @Override
    public int getHurtTime() {
        return ((EntityLivingBase) mcEntity).hurtTime;
    }

    @Override
    public void setJumpMovementFactor(float factor) {
        ((EntityLivingBase) mcEntity).jumpMovementFactor = factor;
    }

    @Override
    public float getJumpMovementFactor() {
        return ((EntityLivingBase) mcEntity).jumpMovementFactor;
    }

    @Override
    public ItemStack getArmorSlot(int slot) {
        // TODO
        return null;
    }

    @Override
    public ItemStack getHeldItem() {
        // TODO
        return null;
    }

    @Override
    public float getEyeHeight() {
        return mcEntity.getEyeHeight();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public float getSwingProgress() {
        return ((EntityLiving) mcEntity).getSwingProgress();
    }
}
