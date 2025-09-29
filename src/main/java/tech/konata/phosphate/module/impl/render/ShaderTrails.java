package tech.konata.phosphate.module.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL13;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.Render2DEvent;
import tech.konata.phosphate.event.events.rendering.Render3DEvent;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderProgram;
import tech.konata.phosphate.rendering.shader.uniform.Uniform1i;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.NumberSetting;
import tech.konata.phosphate.utils.player.PlayerUtils;
import tech.konata.phosphate.utils.timing.Timer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author IzumiiKonata
 * Date: 2025/2/8 12:43
 */
public class ShaderTrails extends Module {

    public ShaderTrails() {
        super("ShaderTrails", Category.RENDER);
    }

    public NumberSetting<Float> alpha = new NumberSetting<>("Alpha", 1.0f, 0.0f, 1.0f, 0.05f);
    public NumberSetting<Integer> length = new NumberSetting<>("Length", 30, 2, 100, 1);
    public NumberSetting<Double> size = new NumberSetting<>("Size", 1.0, 0.05, 2.0, 0.05);
    public final BooleanSetting inFirstPerson = new BooleanSetting("First Person", false);
    public BooleanSetting tex = new BooleanSetting("Texture", true);

    List<Tuple<Vec3, Float>> positions = new ArrayList<>();
    Timer addTimer = new Timer(), removeTimer = new Timer();
    Framebuffer fb = null;

    private ShaderProgram sc = new ShaderProgram("sc.frag", "vertex.vsh");
    private Uniform1i textureIn = new Uniform1i(sc, "textureIn");
    private Uniform1i stencilTex = new Uniform1i(sc, "stencilTex");

    @Override
    public void onEnable() {
        sc = new ShaderProgram("sc.frag", "vertex.vsh");
        textureIn = new Uniform1i(sc, "textureIn");
        stencilTex = new Uniform1i(sc, "stencilTex");
    }

    @Handler
    public void onRenderLayer(Render3DEvent event) {

        boolean bFirstPerson = mc.gameSettings.thirdPersonView == 0;

        if (bFirstPerson && !this.inFirstPerson.getValue())
            return;

        float height = mc.thePlayer.isSneaking() ? 1.2f : 1.4f;

        fb = RenderSystem.createFrameBuffer(fb);
        fb.setFramebufferColor(0, 0, 0, 0);
        fb.bindFramebuffer(false);
        fb.framebufferClearNoBinding();

        GlStateManager.pushMatrix();

        GlStateManager.translate(0, height, 0);

        GlStateManager.rotate(mc.thePlayer.rotationYaw, 0, -1, 0);
        GlStateManager.rotate(90, -1, 0, 0);
        GlStateManager.scale(-1, 1, 1);
        // shift back
        GlStateManager.translate(0, bFirstPerson ? 0.31 : 0.1, 0);
        GlStateManager.scale(-1, 1, 1);
        GlStateManager.rotate(-90, -1, 0, 0);
        GlStateManager.rotate(-mc.thePlayer.rotationYaw, 0, -1, 0);

        boolean currentState = GlStateManager.cullState.cullFace.currentState;

        if (currentState)
            GlStateManager.disableCull();

        boolean maskEnabled = GlStateManager.depthState.maskEnabled;

        if (maskEnabled)
            GlStateManager.depthMask(false);

        double size = 1;

        boolean bPlayerMoving = PlayerUtils.isMoving2();

        while ((positions.size() > length.getValue() + (mc.thePlayer.onGround ? 10 : 0)) || (!PlayerUtils.isMoving2() && removeTimer.isDelayed(10) && !positions.isEmpty())) {
            removeTimer.reset();
            positions.remove(0);
        }

        float yawOffset = bFirstPerson ? mc.thePlayer.rotationYaw : mc.thePlayer.prevRenderYawOffset + (mc.thePlayer.renderYawOffset - mc.thePlayer.prevRenderYawOffset) * event.partialTicks;

        if (addTimer.isDelayed(10) && bPlayerMoving) {
            addTimer.reset();
            positions.add(Tuple.of(new Vec3(mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks,
                    mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks,
                    mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks), yawOffset));
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GlStateManager.disableTexture2D();

        GlStateManager.color(1.0f, 1.0f, 1.0f, this.alpha.getValue());

        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();

        wr.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double rd = Math.toRadians(-yawOffset + 180);

        Vec3 startPos = this.getRenderPosition(new Vec3(mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks));

        for (int i = 0; i < this.positions.size(); i++) {
            Tuple<Vec3, Float> tuple = this.positions.get(i);
            Vec3 renderPos = this.getRenderPosition(tuple.getFirst());
            double rad = Math.toRadians(-tuple.getSecond() + 180);

            Vec3 left = renderPos.add(new Vec3(-this.size.getValue() * 0.5, 0, 0).rotateAroundPoint(new Vec3(0, 0, 0), new Vec3(0, 1, 0), rad));
            Vec3 right = left.add(new Vec3(this.size.getValue(), 0, 0).rotateAroundPoint(new Vec3(0, 0, 0), new Vec3(0, 1, 0), rad));

            float alpha = this.alpha.getValue();

//            double dist = renderPos.distanceTo(startPos);
//            double detectionRange = 2;
//            double range = 4;
//
//            if (dist > detectionRange) {
//                double exceed = dist - detectionRange;
//
//                if (exceed > range - detectionRange)
//                    exceed = range - detectionRange;
//
//                alpha *= (float) (1 - (exceed / (range - detectionRange)));
//            }

            double perc = ((double) (this.positions.size() - i)) / this.positions.size();

            double sensitivity = 0.75;
            if (perc >= sensitivity) {

                double exceed = perc - sensitivity;

                alpha *= (float) (1 - exceed / (1f - sensitivity));

            }

            wr.pos(left.xCoord, left.yCoord, left.zCoord).color(1f, 1f, 1f, alpha).endVertex();
            wr.pos(right.xCoord, right.yCoord, right.zCoord).color(1f, 1f, 1f, alpha).endVertex();

            Tuple<Vec3, Float> tupleNext;
            Vec3 renderPosNext;
            double radNext;

            if (i < this.positions.size() - 1) {
                tupleNext = this.positions.get(i + 1);
                renderPosNext = this.getRenderPosition(tupleNext.getFirst());
                radNext = Math.toRadians(-tupleNext.getSecond() + 180);
            } else {
                renderPosNext = startPos;
                radNext = rd;
            }

            Vec3 leftNext = renderPosNext.add(new Vec3(-this.size.getValue() * 0.5, 0, 0).rotateAroundPoint(new Vec3(0, 0, 0), new Vec3(0, 1, 0), radNext));
            Vec3 rightNext = leftNext.add(new Vec3(this.size.getValue(), 0, 0).rotateAroundPoint(new Vec3(0, 0, 0), new Vec3(0, 1, 0), radNext));

            wr.pos(rightNext.xCoord, rightNext.yCoord, rightNext.zCoord).color(1f, 1f, 1f, alpha).endVertex();
            wr.pos(leftNext.xCoord, leftNext.yCoord, leftNext.zCoord).color(1f, 1f, 1f, alpha).endVertex();
        }

        t.draw();

        mc.getFramebuffer().bindFramebuffer(false);

        if (maskEnabled)
            GlStateManager.depthMask(true);

        if (currentState)
            GlStateManager.enableCull();

        GlStateManager.popMatrix();

    }

    @Handler
    public void onRender2D(Render2DEvent event) {

        if (mc.gameSettings.thirdPersonView == 0 && !this.inFirstPerson.getValue())
            return;

        if (fb != null) {

            if (tex.getValue()) {
                Location sky = Location.of("Phosphate/textures/star.png");

                TextureManager tex = mc.getTextureManager();
                ITextureObject texture = tex.getTexture(sky);

                if (texture == null) {
                    tex.triggerLoad(sky);
                } else {
                    this.sc.start();

                    this.textureIn.setValue(0);
                    this.stencilTex.setValue(9);

                    GlStateManager.bindTexture(texture.getGlTextureId());

                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE9);
                    GlStateManager.bindTexture(fb.framebufferTexture);

                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
                    ShaderProgram.drawQuad();
                    ShaderProgram.stop();
                }
            } else {
                GlStateManager.enableTexture2D();

                GlStateManager.color(1, 1, 1, this.alpha.getValue());

                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

                GlStateManager.bindTexture(fb.framebufferTexture);
                ShaderProgram.drawQuad();
            }

        }

    }

    public Vec3 getRenderPosition(Vec3 from) {
        return from.add(new Vec3(-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, -mc.getRenderManager().renderPosZ));
    }

}
