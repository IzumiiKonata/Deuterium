package tritium.module.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import tritium.event.eventapi.Handler;
import tritium.event.events.player.UpdateEvent;
import tritium.event.events.rendering.Render3DEvent;
import tritium.event.events.world.WorldChangedEvent;
import tritium.module.Module;
import tritium.rendering.HSBColor;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.settings.ColorSetting;
import tritium.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 6/29/2023 5:11 PM
 */
public class BreadCrumbs extends Module {

    public BooleanSetting fadeOut = new BooleanSetting("Fade Out", false);
    public BooleanSetting autoRemove = new BooleanSetting("Auto Remove", false);
    public NumberSetting<Integer> removeIndex = new NumberSetting<>("Remove Distance", 100, 1, 400, 1, autoRemove::getValue);
    public NumberSetting<Double> lineWidth = new NumberSetting<>("Line Width", 2.0, 1.0, 8.0, 0.1);

    public ColorSetting crumbColor = new ColorSetting("Crumb Color", new HSBColor(255, 255, 255, 255));
    List<Vec3> positions = new ArrayList<>();

    @Handler
    public void onMove(UpdateEvent event) {

        if (!event.isPre())
            return;

        double limit = 0.00001;

        if (mc.thePlayer.getSpeed() >= limit) {
            this.positions.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
        }
    }

    @Handler
    public void onWorldChanged(WorldChangedEvent event) {
        this.positions.clear();
    }

    @Handler
    public void onRender3D(Render3DEvent event) {

        Vec3 playerPos = new Vec3(mc.thePlayer.lastTickPosX - (mc.thePlayer.lastTickPosX - mc.thePlayer.posX) * mc.timer.renderPartialTicks, mc.thePlayer.lastTickPosY - (mc.thePlayer.lastTickPosY - mc.thePlayer.posY) * mc.timer.renderPartialTicks, mc.thePlayer.lastTickPosZ - (mc.thePlayer.lastTickPosZ - mc.thePlayer.posZ) * mc.timer.renderPartialTicks);
//        GL11.glBlendFunc(770, 771);
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glEnable(GL11.GL_LINE_SMOOTH);
//        GL11.glLineWidth(lineWidth.getFloatValue());
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GL11.glDepthMask(false);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.disableTexture2D();

        boolean depthState = GlStateManager.depthState.depthTest.currentState;

        if (depthState)
            GlStateManager.disableDepth();

        boolean maskState = GlStateManager.depthState.maskEnabled;

        if (maskState)
            GlStateManager.depthMask(false);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(lineWidth.getFloatValue());

        RenderSystem.color(RenderSystem.reAlpha(this.crumbColor.getRGB(), 0.5f));
        GL11.glBegin(GL11.GL_LINE_STRIP);

        int count = 0;

        if (autoRemove.getValue()) {
            while (this.positions.size() > removeIndex.getValue()) {
                this.positions.removeFirst();
            }
        }

        for (Vec3 pos : this.positions) {
            Vec3 renderPos = this.getRenderPosition(pos);
            double distance = pos.distanceTo(playerPos);

            float alpha = !this.fadeOut.getValue() ? 0.6000000238418579f : (float) (1.0 - Math.min(1.0, distance / 20.0));
            RenderSystem.color(RenderSystem.reAlpha(this.crumbColor.getRGB(count), alpha));
            GL11.glVertex3d(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord);

            count++;

        }

        Vec3 endPos = this.getRenderPosition(playerPos);
        GL11.glVertex3d(endPos.xCoord, endPos.yCoord, endPos.zCoord);
        GL11.glEnd();

        if (depthState)
            GlStateManager.enableDepth();

        if (maskState)
            GlStateManager.depthMask(true);

//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
//        GL11.glDepthMask(true);
//        GL11.glDisable(GL11.GL_BLEND);
//        GlStateManager.color(1, 1, 1, 1);
    }

    public BreadCrumbs() {
        super("Bread Crumbs", Category.RENDER);
    }

    @Override
    public void onEnable() {
        this.positions.clear();
    }

    public Vec3 getRenderPosition(Vec3 from) {
        return from.add(new Vec3(-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, -mc.getRenderManager().renderPosZ));
    }
}
