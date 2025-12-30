package tritium.bridge.management;

import lombok.Getter;
import org.lwjgl.opengl.GL11;
import today.opai.api.interfaces.render.GLStateManager;

/**
 * @author IzumiiKonata
 * Date: 2025/1/27 13:12
 */
public class GlStateManagerImpl implements GLStateManager {

    @Getter
    private static final GlStateManagerImpl instance = new GlStateManagerImpl();

    @Override
    public void alphaFunc(int func, float ref) {
        net.minecraft.client.renderer.GlStateManager.alphaFunc(func, ref);
    }

    @Override
    public void pushAttrib() {
        net.minecraft.client.renderer.GlStateManager.pushAttrib();
    }

    @Override
    public void popAttrib() {
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    @Override
    public void disableAlpha() {
        net.minecraft.client.renderer.GlStateManager.disableAlpha();
    }

    @Override
    public void enableAlpha() {
        net.minecraft.client.renderer.GlStateManager.enableAlpha();
    }

    @Override
    public void enableLighting() {
        net.minecraft.client.renderer.GlStateManager.enableLighting();
    }

    @Override
    public void disableLighting() {
        net.minecraft.client.renderer.GlStateManager.disableLighting();
    }

    @Override
    public void enableLight(int light) {
        net.minecraft.client.renderer.GlStateManager.enableLight(GL11.GL_LIGHT0 + light);
    }

    @Override
    public void disableLight(int light) {
        net.minecraft.client.renderer.GlStateManager.disableLight(GL11.GL_LIGHT0 + light);
    }

    @Override
    public void enableColorMaterial() {
        net.minecraft.client.renderer.GlStateManager.enableColorMaterial();
    }

    @Override
    public void disableColorMaterial() {
        net.minecraft.client.renderer.GlStateManager.disableColorMaterial();
    }

    @Override
    public void colorMaterial(int face, int mode) {
        net.minecraft.client.renderer.GlStateManager.colorMaterial(face, mode);
    }

    @Override
    public void disableDepth() {
        net.minecraft.client.renderer.GlStateManager.disableDepth();
    }

    @Override
    public void enableDepth() {
        net.minecraft.client.renderer.GlStateManager.enableDepth();
    }

    @Override
    public void depthFunc(int depthFunc) {
        net.minecraft.client.renderer.GlStateManager.depthFunc(depthFunc);
    }

    @Override
    public void depthMask(boolean flagIn) {
        net.minecraft.client.renderer.GlStateManager.depthMask(flagIn);
    }

    @Override
    public void disableBlend() {
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    @Override
    public void enableBlend() {
        net.minecraft.client.renderer.GlStateManager.enableBlend();
    }

    @Override
    public void blendFunc(int srcFactor, int dstFactor) {
        net.minecraft.client.renderer.GlStateManager.blendFunc(srcFactor, dstFactor);
    }

    @Override
    public void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
    }

    @Override
    public void enableFog() {
        net.minecraft.client.renderer.GlStateManager.enableFog();
    }

    @Override
    public void disableFog() {
        net.minecraft.client.renderer.GlStateManager.disableFog();
    }

    @Override
    public void setFog(int param) {
        net.minecraft.client.renderer.GlStateManager.setFog(param);
    }

    @Override
    public void setFogDensity(float param) {
        net.minecraft.client.renderer.GlStateManager.setFogDensity(param);
    }

    @Override
    public void setFogStart(float param) {
        net.minecraft.client.renderer.GlStateManager.setFogStart(param);
    }

    @Override
    public void setFogEnd(float param) {
        net.minecraft.client.renderer.GlStateManager.setFogEnd(param);
    }

    @Override
    public void enableCull() {
        net.minecraft.client.renderer.GlStateManager.enableCull();
    }

    @Override
    public void disableCull() {
        net.minecraft.client.renderer.GlStateManager.disableCull();
    }

    @Override
    public void cullFace(int mode) {
        net.minecraft.client.renderer.GlStateManager.cullFace(mode);
    }

    @Override
    public void enablePolygonOffset() {
        net.minecraft.client.renderer.GlStateManager.enablePolygonOffset();
    }

    @Override
    public void disablePolygonOffset() {
        net.minecraft.client.renderer.GlStateManager.disablePolygonOffset();
    }

    @Override
    public void doPolygonOffset(float factor, float units) {
        net.minecraft.client.renderer.GlStateManager.doPolygonOffset(factor, units);
    }

    @Override
    public void enableColorLogic() {
        net.minecraft.client.renderer.GlStateManager.enableColorLogic();
    }

    @Override
    public void disableColorLogic() {
        net.minecraft.client.renderer.GlStateManager.disableColorLogic();
    }

    @Override
    public void colorLogicOp(int opcode) {
        net.minecraft.client.renderer.GlStateManager.colorLogicOp(opcode);
    }

    @Override
    public void setActiveTexture(int texture) {
        net.minecraft.client.renderer.GlStateManager.setActiveTexture(texture);
    }

    @Override
    public void enableTexture2D() {
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
    }

    @Override
    public void disableTexture2D() {
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
    }

    @Override
    public int generateTexture() {
        return net.minecraft.client.renderer.GlStateManager.generateTexture();
    }

    @Override
    public void deleteTexture(int texture) {
        net.minecraft.client.renderer.GlStateManager.deleteTexture(texture);
    }

    @Override
    public void bindTexture(int texture) {
        net.minecraft.client.renderer.GlStateManager.bindTexture(texture);
    }

    @Override
    public void enableNormalize() {
        net.minecraft.client.renderer.GlStateManager.enableNormalize();
    }

    @Override
    public void disableNormalize() {
        net.minecraft.client.renderer.GlStateManager.disableNormalize();
    }

    @Override
    public void shadeModel(int mode) {
        net.minecraft.client.renderer.GlStateManager.shadeModel(mode);
    }

    @Override
    public void enableRescaleNormal() {
        net.minecraft.client.renderer.GlStateManager.enableRescaleNormal();
    }

    @Override
    public void disableRescaleNormal() {
        net.minecraft.client.renderer.GlStateManager.disableRescaleNormal();
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        net.minecraft.client.renderer.GlStateManager.viewport(x, y, width, height);
    }

    @Override
    public void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        net.minecraft.client.renderer.GlStateManager.colorMask(red, green, blue, alpha);
    }

    @Override
    public void clearDepth(double depth) {
        net.minecraft.client.renderer.GlStateManager.clearDepth(depth);
    }

    @Override
    public void clearColor(float red, float green, float blue, float alpha) {
        net.minecraft.client.renderer.GlStateManager.clearColor(red, green, blue, alpha);
    }

    @Override
    public void clear(int mask) {
        net.minecraft.client.renderer.GlStateManager.clear(mask);
    }

    @Override
    public void matrixMode(int mode) {
        net.minecraft.client.renderer.GlStateManager.matrixMode(mode);
    }

    @Override
    public void loadIdentity() {
        net.minecraft.client.renderer.GlStateManager.loadIdentity();
    }

    @Override
    public void pushMatrix() {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
    }

    @Override
    public void popMatrix() {
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    @Override
    public void ortho(double left, double right, double bottom, double top, double zNear, double zFar) {
        net.minecraft.client.renderer.GlStateManager.ortho(left, right, bottom, top, zNear, zFar);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        net.minecraft.client.renderer.GlStateManager.rotate(angle, x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        net.minecraft.client.renderer.GlStateManager.scale(x, y, z);
    }

    @Override
    public void scale(double x, double y, double z) {
        net.minecraft.client.renderer.GlStateManager.scale(x, y, z);
    }

    @Override
    public void translate(float x, float y, float z) {
        net.minecraft.client.renderer.GlStateManager.translate(x, y, z);
    }

    @Override
    public void translate(double x, double y, double z) {
        net.minecraft.client.renderer.GlStateManager.translate(x, y, z);
    }

    @Override
    public void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        net.minecraft.client.renderer.GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha);
    }

    @Override
    public void color(float colorRed, float colorGreen, float colorBlue) {
        net.minecraft.client.renderer.GlStateManager.color(colorRed, colorGreen, colorBlue);
    }

    @Override
    public void resetColor() {
        net.minecraft.client.renderer.GlStateManager.resetColor();
    }

    @Override
    public void glBegin(int p_glBegin_0_) {
        GL11.glBegin(p_glBegin_0_);
    }

    @Override
    public void glEnd() {
        GL11.glEnd();
    }

    @Override
    public void glDrawArrays(int p_glDrawArrays_0_, int p_glDrawArrays_1_, int p_glDrawArrays_2_) {
        GL11.glDrawArrays(p_glDrawArrays_0_, p_glDrawArrays_1_, p_glDrawArrays_2_);
    }

    @Override
    public void callList(int list) {
        GL11.glCallList(list);
    }

    @Override
    public void glDeleteLists(int p_glDeleteLists_0_, int p_glDeleteLists_1_) {
        GL11.glDeleteLists(p_glDeleteLists_0_, p_glDeleteLists_1_);
    }

    @Override
    public void glNewList(int p_glNewList_0_, int p_glNewList_1_) {
        GL11.glNewList(p_glNewList_0_, p_glNewList_1_);
    }

    @Override
    public void glEndList() {
        GL11.glEndList();
    }

    @Override
    public int glGetError() {
        return GL11.glGetError();
    }

}
