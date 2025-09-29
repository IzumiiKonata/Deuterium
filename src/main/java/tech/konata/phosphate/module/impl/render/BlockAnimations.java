package tech.konata.phosphate.module.impl.render;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import tech.konata.phosphate.module.impl.render.blockanimations.*;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.world.TickEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.settings.NumberSetting;

/**
 * @author IzumiiKonata
 * @since 2023/12/31
 */
public class BlockAnimations extends Module {

    public BlockAnimations() {
        super("BlockAnimations", Category.RENDER);
        super.addSubModules(new Vanilla(), new Remix(), new Lunar(), new Screw(), new Swing(), new Swong(), new Swang(), new Swank(), new SwAing(), new Gay(), new Punch(), new Winter(), new Rotate(), new DickPunch());
    }

    public NumberSetting<Double> x = new NumberSetting<>("X", 0.0, -1.0, 1.0, 0.05);
    public NumberSetting<Double> y = new NumberSetting<>("Y", 0.15, -1.0, 1.0, 0.05);
    public NumberSetting<Double> z = new NumberSetting<>("Z", 0.0, -1.0, 1.0, 0.05);
    public BooleanSetting leftHanded = new BooleanSetting("Left-Handed", false);
    public BooleanSetting twoHanded = new BooleanSetting("Two-Handed", false);
    public ModeSetting<TwoHandedMode> twoHandedMode = new ModeSetting<>("Two Handed Mode", TwoHandedMode.Clone, () -> twoHanded.getValue());

    public enum TwoHandedMode {
        Clone,
        Static,
        StaticNoItem
    }

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    public void transformFirstPersonItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    /**
     * Translate and rotate the render for holding a block
     */
    public void doBlockTransformations() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix(this.getSubModes().getValue());
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (!event.isPre())
            return;
        if (mc.thePlayer.getItemInUseCount() > 0) {
            boolean mouseDown = mc.gameSettings.keyBindAttack.isKeyDown() && mc.gameSettings.keyBindUseItem.isKeyDown();
            if (mouseDown && !mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) {
                mc.thePlayer.swingItem();
            }
        }
    }

    ;

    public void swingItem(EntityPlayerSP entityplayersp) {
        int swingAnimationEnd = entityplayersp.isPotionActive(Potion.digSpeed)
                ? (6 - (1 + entityplayersp.getActivePotionEffect(Potion.digSpeed).getAmplifier()))
                : (entityplayersp.isPotionActive(Potion.digSlowdown)
                ? (6 + (1 + entityplayersp.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2)
                : 6);
        if (!entityplayersp.isSwingInProgress || entityplayersp.swingProgressInt >= swingAnimationEnd / 2
                || entityplayersp.swingProgressInt < 0) {
            entityplayersp.swingProgressInt = -1;
            entityplayersp.isSwingInProgress = true;
            //mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
    }

}
