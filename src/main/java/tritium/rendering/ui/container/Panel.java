package tritium.rendering.ui.container;

import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.settings.ClientSettings;

/**
 * @author IzumiiKonata
 * Date: 2025/7/8 21:44
 */
public class Panel extends AbstractWidget<Panel> {
    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        // 什么也不干, 这个只是一个隐形的容器, 用于将组件分组。
    }

    @Override
    protected void renderDebugLayout() {

    }
}
