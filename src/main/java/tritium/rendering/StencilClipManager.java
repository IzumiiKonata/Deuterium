package tritium.rendering;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import java.util.Stack;

public class StencilClipManager {
    private static class StencilState {
        int stencilValue;
        boolean colorMask;
        boolean depthMask;
        
        StencilState(int value, boolean color, boolean depth) {
            this.stencilValue = value;
            this.colorMask = color;
            this.depthMask = depth;
        }
    }
    
    private static Stack<StencilState> stencilStack = new Stack<>();
    private static int currentStencilValue = 0;
    private static boolean stencilEnabled = false;
    
    // 初始化模板测试
    public static void initialize() {
        Stencil.checkSetupFBO(Minecraft.getMinecraft().getFramebuffer());
        GL11.glClearStencil(0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        stencilEnabled = true;
        currentStencilValue = 0;
    }
    
    // 开始裁剪
    public static void beginClip(Runnable drawClipShape) {
        if (!stencilEnabled) {
            initialize();
        }
        
        // save states
        boolean colorMask = GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        stencilStack.push(new StencilState(currentStencilValue, colorMask, depthMask));
        
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        
        if (currentStencilValue == 0) {
            // always pass
            GL11.glStencilFunc(GL11.GL_ALWAYS, currentStencilValue + 1, 0xFF);
        } else {
            GL11.glStencilFunc(GL11.GL_EQUAL, currentStencilValue, 0xFF);
        }
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
        
        drawClipShape.run();
        
        // 更新模板值
        currentStencilValue++;
        
        // 恢复颜色写入
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(depthMask);
        GL11.glStencilFunc(GL11.GL_EQUAL, currentStencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }
    
    public static void endClip() {
        if (stencilStack.isEmpty()) {
            return;
        }
        
        StencilState state = stencilStack.pop();
        currentStencilValue = state.stencilValue;
        
        GL11.glColorMask(state.colorMask, state.colorMask, state.colorMask, state.colorMask);
        GL11.glDepthMask(state.depthMask);
        
        if (currentStencilValue > 0) {
            GL11.glStencilFunc(GL11.GL_EQUAL, currentStencilValue, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        } else {
            disable();
        }
    }
    
    public static void disable() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        stencilEnabled = false;
        currentStencilValue = 0;
        stencilStack.clear();
    }
    
    // 清除模板缓冲区
    public static void clear() {
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        currentStencilValue = 0;
        stencilStack.clear();
    }
}