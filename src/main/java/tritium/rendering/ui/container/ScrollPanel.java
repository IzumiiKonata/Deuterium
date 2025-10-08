package tritium.rendering.ui.container;

import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;

import java.util.List;

/**
 * 一个从上至下排列的可以滚动的容器
 * @author IzumiiKonata
 * Date: 2025/7/8 21:45
 */
public class ScrollPanel extends Panel {

    private double spacing = 0;
    public double actualScrollOffset = 0, targetScrollOffset = 0;

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        // 垂直滑动
        this.performScroll(mouseX, mouseY, dWheel);

        // 设置子组件的垂直位置
        this.alignChildren();
    }

    private void performScroll(double mouseX, double mouseY, int dWheel) {

        if (dWheel != 0) {

            double strength = 24;

            // hovering this panel
            if (this.testHovered(mouseX, mouseY)) {
                if (dWheel > 0)
                    this.targetScrollOffset -= strength;
                else
                    this.targetScrollOffset += strength;

            }
        }

        this.targetScrollOffset = Math.max(this.targetScrollOffset, 0);
        double childrenHeightSum = this.getChildrenHeightSum();
        if (childrenHeightSum > this.getHeight())
            this.targetScrollOffset = Math.min(this.targetScrollOffset, childrenHeightSum - this.getHeight());
        else
            this.targetScrollOffset = Math.min(this.targetScrollOffset, 0);

        this.actualScrollOffset = Interpolations.interpBezier(this.actualScrollOffset, this.targetScrollOffset, 1f);
    }

    private void alignChildren() {
        double offsetY = -this.actualScrollOffset;

        for (AbstractWidget<?> child : this.getChildren()) {

            double height = child.getHeight();

            if (child.isHidden())
                continue;

            child.setPosition(child.getRelativeX(), offsetY);
            offsetY += height + spacing;
        }
    }

    private double getChildrenHeightSum() {
        double result = 0;

        List<AbstractWidget<?>> children = this.getChildren();

        for (AbstractWidget<?> child : children) {

            double height = child.getHeight();

            if (child.isHidden())
                continue;

            result += height + this.spacing;
        }

        if (result > 0)
            result -= this.spacing;

        return result;
    }

    @Override
    public void renderWidget(double mouseX, double mouseY, int dWheel) {
        StencilClipManager.beginClip(() -> {
            Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), -1);
        });

        super.renderWidget(mouseX, mouseY, dWheel);

        StencilClipManager.endClip();
    }

    @Override
    protected boolean shouldClickChildren(double mouseX, double mouseY) {
//        System.out.println(this.testHovered(mouseX, mouseY));
        return this.testHovered(mouseX, mouseY);
    }

    public void setSpacing(double spacing) {
        this.spacing = spacing;
    }
}
