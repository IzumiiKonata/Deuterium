package tritium.rendering;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import java.util.Stack;

public class StencilClipManager {
    public static class StencilState {
        int stencilValue;
        boolean colorMask;
        boolean depthMask;

        StencilState(int value, boolean color, boolean depth) {
            this.stencilValue = value;
            this.colorMask = color;
            this.depthMask = depth;
        }
    }

    public static Stack<StencilState> stencilStack = new Stack<>();
    public static int currentStencilValue = 0;

    // 初始化模板测试
    public static void initialize() {
        Stencil.checkSetupFBO(FramebufferCaching.getOverridingFramebuffer() != null ?
                FramebufferCaching.getOverridingFramebuffer() :
                Minecraft.getMinecraft().getFramebuffer());
        GL11.glClearStencil(0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
    }

    // 开始裁剪
    public static void beginClip(Runnable drawClipShape) {
        if (currentStencilValue == 0) {
            initialize();
        }

        // 保存当前状态
        boolean colorMask = GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        stencilStack.push(new StencilState(currentStencilValue, colorMask, depthMask));

        // 禁用颜色写入，准备写入模板
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        // 关键修复：始终使用 GL_EQUAL 测试当前层级，然后递增
        if (currentStencilValue == 0) {
            // 第一层：总是通过
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        } else {
            // 嵌套层：只在父层区域内写入
            GL11.glStencilFunc(GL11.GL_EQUAL, currentStencilValue, 0xFF);
        }

        // 通过测试时递增模板值
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);

        // 绘制裁剪形状
        drawClipShape.run();

        // 更新当前模板值
        currentStencilValue++;

        // 恢复颜色写入，设置渲染条件
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(depthMask);

        // 只在新的裁剪区域内渲染
        GL11.glStencilFunc(GL11.GL_EQUAL, currentStencilValue, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    public static void endClip() {
        if (stencilStack.isEmpty()) {
            System.err.println("Stencil stack underflow");
            return;
        }

        StencilState state = stencilStack.pop();
        currentStencilValue = state.stencilValue;

        // 恢复之前的状态
        GL11.glColorMask(state.colorMask, state.colorMask, state.colorMask, state.colorMask);
        GL11.glDepthMask(state.depthMask);

        if (currentStencilValue > 0) {
            // 恢复到父层的渲染条件
            GL11.glStencilFunc(GL11.GL_EQUAL, currentStencilValue, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        } else {
            // 回到最外层，禁用模板测试
            disable();
        }
    }

    public static void disable() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        currentStencilValue = 0;
    }

    // 清除模板缓冲区
    public static void clear() {
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        currentStencilValue = 0;
        stencilStack.clear();
    }
}