package tech.konata.phosphate.module.impl.render;

import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Location;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.player.AttackEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2024/11/3 10:37
 */
public class BloodParticles extends Module {

    public BloodParticles() {
        super("BloodParticles", Category.RENDER);
    }

    public final BooleanSetting sound = new BooleanSetting("sound", true);
    public final NumberSetting<Integer> multiplier = new NumberSetting<>("Multiplier", 2, 1, 10, 1);

    @Handler
    public void onAttack(AttackEvent event) {

        Entity entity = event.getAttackedEntity();

        if (entity instanceof EntityLivingBase) {
            for (int i = 0; i < multiplier.getValue() * 2; i++) {
                mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, entity.posX, entity.posY + entity.height - 0.75, entity.posZ, 0, 0, 0, Block.getStateId(Blocks.redstone_block.getDefaultState()));
            }

            if (sound.getValue()) {
                mc.getSoundHandler().playSound(new PositionedSoundRecord(Location.of("dig.stone"), 4.0F, 1.2F, ((float) entity.posX), ((float) entity.posY), ((float) entity.posZ)));
            }
        }

    }

}
