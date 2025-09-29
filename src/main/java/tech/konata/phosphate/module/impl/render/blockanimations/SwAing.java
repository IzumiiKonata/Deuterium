package tech.konata.phosphate.module.impl.render.blockanimations;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.module.impl.render.BlockAnimations;
import tech.konata.phosphate.module.submodule.SubModule;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.BlockAnimationEvent;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 9:53 PM
 */
public class SwAing extends SubModule<BlockAnimations> {

    public SwAing() {
        super("SwAing");
    }

    @Handler
    public void onEvent(BlockAnimationEvent event) {
        event.setCancelled();

        GL11.glTranslated(-0.10000000149011612, 0.15000000596046448, 0.0);
        GL11.glTranslated(0.10000000149011612, -0.20000000298023224, 0.0);
        this.avatar(event.equipProgress, event.swingProgress);
        this.getModule().doBlockTransformations();
    }

    ;

    private void avatar(final float equipProgress, final float swingProgress) {
        GlStateManager.translate(0.6f, -0.48f, -0.79999995f);
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
        final float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927f);
        final float f2 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927f);
        GlStateManager.rotate(f2 * -40.0f, 1.0f, -0.2f, 0.2f);
        GlStateManager.scale(0.4f, 0.4f, 0.4f);
    }

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
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
    private void doBlockTransformations() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }
}
