package tritium.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Location;
import org.lwjgl.opengl.GL11;
import tritium.management.ModuleManager;

public class RenderWings
        extends ModelBase {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Location location = Location.of("tritium/textures/wings.png");
    private final ModelRenderer wing;
    private final ModelRenderer wingTip;

    public RenderWings() {
        this.setTextureOffset("wing.bone", 0, 0);
        this.setTextureOffset("wing.skin", -10, 8);
        this.setTextureOffset("wingtip.bone", 0, 5);
        this.setTextureOffset("wingtip.skin", -10, 18);
        this.wing = new ModelRenderer(this, "wing");
        this.wing.setTextureSize(30, 30);
        this.wing.setRotationPoint(-2.0f, 0.0f, 0.0f);
        this.wing.addBox("bone", -10.0f, -1.0f, -1.0f, 10, 2, 2);
        this.wing.addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10);
        this.wingTip = new ModelRenderer(this, "wingtip");
        this.wingTip.setTextureSize(30, 30);
        this.wingTip.setRotationPoint(-10.0f, 0.0f, 0.0f);
        this.wingTip.addBox("bone", -10.0f, -0.5f, -0.5f, 10, 1, 1);
        this.wingTip.addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10);
        this.wing.addChild(this.wingTip);
    }

    public void renderWings(EntityPlayer player, float partialTicks) {
        if (this.mc.gameSettings.thirdPersonView == 0 && !ModuleManager.wings.firstPerson.getValue()) {
            return;
        }
        double scale = ModuleManager.wings.scale.getValue();
        double rotate = this.interpolate(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
        GL11.glPushMatrix();
        GL11.glScaled(-scale, -scale, scale);
        GL11.glRotated(180.0 + rotate, 0.0, 1.0, 0.0);
        GL11.glTranslated(0.0, -1.25 / scale, 0.0);
        GL11.glTranslated(0.0, 0.0, 0.2 / scale);
        if (player.isSneaking()) {
            GL11.glTranslated(0.0, 0.125 / scale, 0.0);
        }
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(this.location);
        GlStateManager.enableCull();
        for (int j = 0; j < 2; ++j) {
            float f11 = (float) (System.currentTimeMillis() % 1000L) / 1000.0f * (float) Math.PI * 2.0f;
            this.wing.rotateAngleX = (float) Math.toRadians(-80.0) - (float) Math.cos(f11) * 0.2f;
            this.wing.rotateAngleY = (float) Math.toRadians(20.0) + (float) Math.sin(f11) * 0.4f;
            this.wing.rotateAngleZ = (float) Math.toRadians(20.0);
            this.wingTip.rotateAngleZ = -((float) (Math.sin(f11 + 2.0f) + 0.5)) * 0.75f;
            this.wing.render(0.0625f);
            GL11.glScalef(-1.0f, 1.0f, 1.0f);
            if (j != 0) continue;
            GlStateManager.cullFace(GL11.GL_FRONT);
        }
        GlStateManager.cullFace(GL11.GL_BACK);
        GlStateManager.disableCull();
        GL11.glColor3f(255.0f, 255.0f, 255.0f);
        GL11.glPopMatrix();
    }

    private float interpolate(float yaw1, float yaw2, float percent) {
        float f = (yaw1 + (yaw2 - yaw1) * percent) % 360.0f;
        if (f < 0.0f) {
            f += 360.0f;
        }
        return f;
    }
}
