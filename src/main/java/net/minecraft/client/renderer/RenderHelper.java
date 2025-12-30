package net.minecraft.client.renderer;

import java.nio.FloatBuffer;

import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class RenderHelper {

    private static final Vec3 LIGHT0_POS = (new Vec3(0.20000000298023224D, 1.0D, -0.699999988079071D)).normalize();
    private static final Vec3 LIGHT1_POS = (new Vec3(-0.20000000298023224D, 1.0D, 0.699999988079071D)).normalize();

    private static final Vec3 LIGHT0_POS_PAPERDOLL = (new Vec3(-.5, 1.0D, -0.699999988079071D)).normalize();
    private static final Vec3 LIGHT1_POS_PAPERDOLL = (new Vec3(.5, 1.0D, 0.699999988079071D)).normalize();

    private static final FloatBuffer LIGHT0_POS_BUFFER = createBuffer(LIGHT0_POS.xCoord, LIGHT0_POS.yCoord, LIGHT0_POS.zCoord);
    private static final FloatBuffer LIGHT1_POS_BUFFER = createBuffer(LIGHT1_POS.xCoord, LIGHT1_POS.yCoord, LIGHT1_POS.zCoord);
    private static final FloatBuffer LIGHT0_POS_PAPERDOLL_BUFFER = createBuffer(LIGHT0_POS_PAPERDOLL.xCoord, LIGHT0_POS_PAPERDOLL.yCoord, LIGHT0_POS_PAPERDOLL.zCoord);
    private static final FloatBuffer LIGHT1_POS_PAPERDOLL_BUFFER = createBuffer(LIGHT1_POS_PAPERDOLL.xCoord, LIGHT1_POS_PAPERDOLL.yCoord, LIGHT1_POS_PAPERDOLL.zCoord);
    private static final FloatBuffer DIFFUSE_BUFFER = createBuffer(0.6F, 0.6F, 0.6F, 1.0F);
    private static final FloatBuffer AMBIENT_BUFFER = createBuffer(0.0F, 0.0F, 0.0F, 1.0F);
    private static final FloatBuffer SPECULAR_BUFFER = createBuffer(0.0F, 0.0F, 0.0F, 1.0F);
    private static final FloatBuffer LIGHT_MODEL_AMBIENT_BUFFER = createBuffer(0.4F, 0.4F, 0.4F, 1.0F);

    /**
     * Disables the OpenGL lighting properties enabled by enableStandardItemLighting
     */
    public static void disableStandardItemLighting() {
        GlStateManager.disableLighting();
        GlStateManager.disableLight(GL11.GL_LIGHT0);
        GlStateManager.disableLight(GL11.GL_LIGHT1);
        GlStateManager.disableColorMaterial();
    }

    /**
     * Sets the OpenGL lighting properties to the values used when rendering blocks as items
     */
    public static void enableStandardItemLighting() {
        setupLighting(LIGHT0_POS_BUFFER, LIGHT1_POS_BUFFER);
    }

    /**
     * Sets the OpenGL lighting properties for paper doll rendering
     */
    public static void enablePaperDollLighting() {
        setupLighting(LIGHT0_POS_PAPERDOLL_BUFFER, LIGHT1_POS_PAPERDOLL_BUFFER);
    }

    private static FloatBuffer createBuffer(double x, double y, double z) {
        return createBuffer((float)x, (float)y, (float)z, 0.0f);
    }

    private static FloatBuffer createBuffer(float x, float y, float z, float w) {
        FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(4);
        buffer.put(x).put(y).put(z).put(w);
        buffer.flip();
        return buffer;
    }

    static boolean lightSet = false;

    private static void setupLighting(FloatBuffer light0Pos, FloatBuffer light1Pos) {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(GL11.GL_LIGHT0);
        GlStateManager.enableLight(GL11.GL_LIGHT1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, light0Pos);
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, light1Pos);

        if (!lightSet) {
            lightSet = true;
            GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, DIFFUSE_BUFFER);
            GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, AMBIENT_BUFFER);
            GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, SPECULAR_BUFFER);

            GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, DIFFUSE_BUFFER);
            GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_AMBIENT, AMBIENT_BUFFER);
            GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_SPECULAR, SPECULAR_BUFFER);

            GlStateManager.shadeModel(GL11.GL_FLAT);
            GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, LIGHT_MODEL_AMBIENT_BUFFER);
        }
    }

    /**
     * Update and return colorBuffer with the RGBA values passed as arguments
     */
    private static FloatBuffer setColorBuffer(double p_74517_0_, double p_74517_2_, double p_74517_4_, double p_74517_6_) {
        return setColorBuffer((float) p_74517_0_, (float) p_74517_2_, (float) p_74517_4_, (float) p_74517_6_);
    }

    /**
     * Sets OpenGL lighting for rendering blocks as items inside GUI screens (such as containers).
     */
    public static void enableGUIStandardItemLighting() {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);
        enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

}
