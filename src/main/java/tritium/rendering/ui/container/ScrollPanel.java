package tritium.rendering.ui.container;

import lombok.Getter;
import org.lwjgl.input.Keyboard;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
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

    @Getter
    private Alignment alignment = Alignment.VERTICAL;

    /**
     * 子组件的排列方式
     */
    public enum Alignment {
        /**
         * 仅垂直排列 (默认)
         */
        VERTICAL,
        /**
         * 仅水平排列
         */
        HORIZONTAL,
        /**
         * 垂直排列并水平填充
         */
        VERTICAL_WITH_HORIZONTAL_FILL;
    }

    public ScrollPanel setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

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

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                strength *= 2;

            // hovering this panel
            if (this.testHovered(mouseX, mouseY)) {
                if (dWheel > 0)
                    this.targetScrollOffset -= strength;
                else
                    this.targetScrollOffset += strength;

            }
        }

        this.targetScrollOffset = Math.max(this.targetScrollOffset, 0);

        if (this.alignment == Alignment.VERTICAL) {
            double childrenHeightSum = this.getChildrenHeightSum();
            if (childrenHeightSum > this.getHeight())
                this.targetScrollOffset = Math.min(this.targetScrollOffset, childrenHeightSum - this.getHeight());
            else
                this.targetScrollOffset = Math.min(this.targetScrollOffset, 0);
        } else if (this.alignment == Alignment.HORIZONTAL) {
            double childrenWidthSum = this.getChildrenWidthSum();
            if (childrenWidthSum > this.getWidth())
                this.targetScrollOffset = Math.min(this.targetScrollOffset, childrenWidthSum - this.getWidth());
            else
                this.targetScrollOffset = Math.min(this.targetScrollOffset, 0);
        } else if (this.alignment == Alignment.VERTICAL_WITH_HORIZONTAL_FILL) {
            double childrenHeightSum = this.getChildrenHeightSumHorizontalFill();
            if (childrenHeightSum > this.getHeight())
                this.targetScrollOffset = Math.min(this.targetScrollOffset, childrenHeightSum - this.getHeight());
            else
                this.targetScrollOffset = Math.min(this.targetScrollOffset, 0);
        }

        this.actualScrollOffset = Interpolations.interpBezier(this.actualScrollOffset, this.targetScrollOffset, 1f);
    }

    public void alignChildren() {
        double offsetX = 0;
        double offsetY = 0;

        if (this.alignment == Alignment.VERTICAL || this.alignment == Alignment.VERTICAL_WITH_HORIZONTAL_FILL)
            offsetY = -this.actualScrollOffset;
        else
            offsetX = -this.actualScrollOffset;

        for (AbstractWidget<?> child : this.getChildren()) {

            double width = child.getWidth();
            double height = child.getHeight();

            if (child.isHidden())
                continue;

            if (this.alignment == Alignment.VERTICAL) {
                child.setPosition(child.getRelativeX(), offsetY);
                offsetY += height + spacing;
            } else if (this.alignment == Alignment.HORIZONTAL) {
                child.setPosition(offsetX, child.getRelativeY());
                offsetX += width + spacing;
            } else if (this.alignment == Alignment.VERTICAL_WITH_HORIZONTAL_FILL) {
                if (offsetX + width > this.getWidth()) {
                    offsetX = 0;
                    offsetY += height + spacing;
                }

                child.setPosition(offsetX, offsetY);
                offsetX += width + spacing;
            }
        }
    }

    private double getChildrenHeightSum() {
        double result = 0;

        List<AbstractWidget<?>> children = this.getChildren();

        for (AbstractWidget<?> child : children) {

            double width = child.getWidth();
            double height = child.getHeight();

            if (child.isHidden())
                continue;

            result += height + this.spacing;
        }

        if (result > 0)
            result -= this.spacing;

        return result;
    }

    private double getChildrenWidthSum() {
        double result = 0;

        List<AbstractWidget<?>> children = this.getChildren();

        for (AbstractWidget<?> child : children) {

            double width = child.getWidth();
            double height = child.getHeight();

            if (child.isHidden())
                continue;

            result += width + this.spacing;
        }

        if (result > 0)
            result -= this.spacing;

        return result;
    }

    private double getChildrenHeightSumHorizontalFill() {
        double result = 0;
        double offsetX = 0;

        List<AbstractWidget<?>> children = this.getChildren();

        for (AbstractWidget<?> child : children) {

            double width = child.getWidth();
            double height = child.getHeight();

            if (child.isHidden())
                continue;

            if (offsetX == 0 && result == 0) {
                result += height + spacing;
            }

            if (offsetX + width > this.getWidth()) {
                offsetX = 0;
                result += height + spacing;
            }

            offsetX += width + spacing;
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
    protected boolean shouldRenderChildren(AbstractWidget<?> child, double mouseX, double mouseY) {

        if (this.getAlignment() == Alignment.VERTICAL || this.getAlignment() == Alignment.VERTICAL_WITH_HORIZONTAL_FILL) {
            if (child.getRelativeY() + child.getHeight() < 0)
                return false;

            if (child.getRelativeY() > this.getHeight())
                return false;
        } else if (this.getAlignment() == Alignment.HORIZONTAL) {
            if (child.getRelativeX() + child.getWidth() < 0)
                return false;

            if (child.getRelativeX() > this.getWidth())
                return false;
        }

        return true;
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
