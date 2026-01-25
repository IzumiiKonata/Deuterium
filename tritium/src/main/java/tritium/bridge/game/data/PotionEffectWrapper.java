package tritium.bridge.game.data;

import today.opai.api.interfaces.game.item.PotionEffect;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:01
 */
public class PotionEffectWrapper implements PotionEffect {

    private net.minecraft.potion.PotionEffect mcPotionEffect;

    public PotionEffectWrapper(net.minecraft.potion.PotionEffect mcPotionEffect) {
        this.mcPotionEffect = mcPotionEffect;
    }

    @Override
    public int getAmplifier() {
        return this.mcPotionEffect.getAmplifier();
    }

    @Override
    public int getId() {
        return this.mcPotionEffect.getPotionID();
    }

    @Override
    public String getName() {
        return this.mcPotionEffect.getName();
    }

    @Override
    public int getDuration() {
        return this.mcPotionEffect.getDuration();
    }
}
