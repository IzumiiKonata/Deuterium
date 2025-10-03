package tritium.rendering.ui;

import net.minecraft.client.renderer.GlStateManager;
import tritium.interfaces.SharedRenderingConstants;
import tritium.rendering.rendersystem.RenderSystem;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 组件 base。
 * @author IzumiiKonata
 * Date: 2025/7/8 19:42
 */
public abstract class AbstractWidget<SELF extends AbstractWidget<SELF>> implements SharedRenderingConstants {

    public interface OnClickCallback {
        boolean onClick(double relativeX, double relativeY, int mouseButton);
    }

    public interface BeforeRenderCallback {
        void setPositions();
    }

    AbstractWidget<?> parent = null;
    List<AbstractWidget<?>> children = new ArrayList<>();

    public static class Bounds {
        /**
         * 这个组件相对于父组件的坐标的偏移。
         * 比如说, 有一个坐标为 (100, 100) 的矩形组件,
         * 然后有一个新的组件作为这个矩形组件的子组件,
         * 然后设置子组件 x, y 都为 2,
         * 那么子组件的 {@link AbstractWidget#getX()} 将返回 父组件的屏幕 X 坐标 100 加上子组件的偏移 X 坐标 2, 结果为 102. Y 轴同理。
         * 子组件的 {@link AbstractWidget#getRelativeX()} 将返回 2.
         *
         */
        private double x, y;
        private double width, height;
    }

    private final Bounds bounds = new Bounds();
    private BeforeRenderCallback beforeRenderCallback = () -> {};

    private boolean clickable = true;
    private boolean hovering = false;
    private boolean hidden = false;
    private boolean bloom = false, blur = false;

    private Color color = Color.BLACK;

    protected OnClickCallback callback = null;
    private Runnable transformations = null, onTick = null;

    // 组件的半透明度
    private float alpha = 1.0f;

    /**
     * 在此处编写组件的渲染代码。
     */
    public abstract void onRender(double mouseX, double mouseY, int dWheel);

    /**
     * 渲染这个组件以及它的子组件。
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param dWheel 滚轮
     */
    public void renderWidget(double mouseX, double mouseY, int dWheel) {

        if (this.isHidden())
            return;

        GlStateManager.pushMatrix();

        if (this.transformations != null)
            this.transformations.run();

        this.beforeRenderCallback.setPositions();

        Runnable shader = () -> {
            GlStateManager.pushMatrix();

            if (this.transformations != null)
                this.transformations.run();

            this.onRender(mouseX, mouseY, dWheel);

            GlStateManager.popMatrix();
        };

        if (this.isBloom()) {
            SharedRenderingConstants.BLOOM.add(shader);
        }

        if (this.isBlur()) {
            SharedRenderingConstants.BLUR.add(shader);
        }

        this.onRender(mouseX, mouseY, dWheel);

        boolean childHovering = false;

        for (AbstractWidget<?> child : this.getChildren()) {

            if (child.isHidden())
                continue;

            child.renderWidget(mouseX, mouseY, dWheel);

            if (child.isClickable() && child.testHovered(mouseX, mouseY)) {
                childHovering = true;
            }
        }

        GlStateManager.popMatrix();

        // 更新组件悬停状态
        // 如果所有的子组件都没有被选中, 且这个组件被选中, 才设置这个组件的 hovering 为 true.
        this.hovering = !childHovering && this.testHovered(mouseX, mouseY);
    }

    public SELF setOnTick(Runnable onTick) {
        this.onTick = onTick;
        return (SELF) this;
    }

    public void onTick() {

    }

    public void onTickReceived() {

        if (this.onTick != null) {
            this.onTick.run();
        }

        this.onTick();

        this.getChildren().forEach(AbstractWidget::onTickReceived);

    }

    public void addChild(AbstractWidget<?>... children) {
        for (AbstractWidget<?> child : children) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    /**
     * 设置这个组件相较于父组件的留边, 会直接修改组件的 x, y 坐标以及长宽。
     * @param margin 留边大小
     */
    public SELF setMargin(double margin) {
        return this.setMargin(margin, margin, margin, margin);
    }

    /**
     * 设置这个组件相较于父组件的留边, 会直接修改组件的 x, y 坐标以及长宽。
     * @param left 左边的留边大小
     * @param top 上面的留边大小
     * @param right 右边的留边大小
     * @param bottom 下面的留边大小
     */
    public SELF setMargin(double left, double top, double right, double bottom) {
        this.getBounds().x = left;
        this.getBounds().y = top;
        this.getBounds().width = this.getParentWidth() - left - right;
        this.getBounds().height = this.getParentHeight() - top - bottom;
        return (SELF) this;
    }

    /**
     * 测试该组件有没有被鼠标指向
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @return 该组件有没有被鼠标指向
     */
    protected boolean testHovered(double mouseX, double mouseY) {
        return this.isHovered(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    /**
     * 实用方法, 检测鼠标有没有在一个矩形范围内。
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param x 矩形 X 坐标
     * @param y 矩形 Y 坐标
     * @param width 矩形宽度
     * @param height 举行长度
     * @return 是否在范围内
     */
    public boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {

        // bounds check
        if (width < 0) {
            width = -width;
            x -= width;
        }

        if (height < 0) {
            height = -height;
            y -= height;
        }

        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    private boolean iterateChildrenMouseClick(List<AbstractWidget<?>> children, double mouseX, double mouseY, int mouseButton) {
        for (AbstractWidget<?> child : children) {

            if (child.isHidden()) {
                continue;
            }

            if (child.isHovering() && child.isClickable() && child.onMouseClicked(mouseX - child.getX(), mouseY - child.getY(), mouseButton)) {
                return true;
            }

            if (!child.getChildren().isEmpty()) {
                if (this.iterateChildrenMouseClick(child.getChildren(), mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void onMouseClickReceived(double mouseX, double mouseY, int mouseButton) {

        // 先检测子组件
        // 如果子组件都没有响应点击事件, 则测试这个组件
        if (!this.iterateChildrenMouseClick(this.getChildren(), mouseX, mouseY, mouseButton)) {
            if (!this.isHidden() && this.isHovering()) {
                // 不需要在乎返回值。
                this.onMouseClicked(mouseX - this.getX(), mouseY - this.getY(), mouseButton);
            }
        }

    }

    /**
     * 当组件被点击时, 此方法被调用。
     * @param relativeX 被点击的在组件内的坐标 X
     * @param relativeY 被点击的在组件内的坐标 Y
     * @param mouseButton 鼠标按钮, 0 为鼠标左键, 1 为鼠标右键
     * @return 是否接收点击事件
     */
    public boolean onMouseClicked(double relativeX, double relativeY, int mouseButton) {
        return this.callback != null && this.isClickable() && this.callback.onClick(relativeX, relativeY, mouseButton);
    }

    public void onKeyTypedReceived(char typedChar, int keyCode) {

        if (this.isHidden())
            return;

        this.children.forEach(c -> c.onKeyTyped(typedChar, keyCode));
        this.onKeyTyped(typedChar, keyCode);
    }

    /**
     * 当接收到键盘输入时, 此方法被调用。
     * @param character 字符
     * @param keyCode 键码
     */
    public void onKeyTyped(char character, int keyCode) {

    }

    // getters and setters


    public SELF setBloom(boolean bloom) {
        this.bloom = bloom;
        return (SELF) this;
    }

    public boolean isBloom() {
        return bloom;
    }

    public SELF setBlur(boolean blur) {
        this.blur = blur;
        return (SELF) this;
    }

    public boolean isBlur() {
        return blur;
    }

    public SELF setBeforeRenderCallback(BeforeRenderCallback beforeRenderCallback) {
        this.beforeRenderCallback = beforeRenderCallback;
        return (SELF) this;
    }

    public Color getColor() {
        return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int) (this.getAlpha() * 255));
    }

    public SELF setColor(int color) {
        this.color = new Color(color);
        return (SELF) this;
    }

    public SELF setColor(Color color) {
        this.color = color;
        return (SELF) this;
    }

    public int getHexColor() {
        return RenderSystem.hexColor(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int) (this.getAlpha() * 255));
    }

    public SELF setTransformations(Runnable transformations) {
        this.transformations = transformations;
        return (SELF) this;
    }

    public double getParentX() {
        if (this.getParent() != null)
            return this.getParent().getX();

        return 0;
    }

    public double getParentY() {
        if (this.getParent() != null)
            return this.getParent().getY();

        return 0;
    }

    public double getParentWidth() {

        if (this.getParent() != null)
            return this.getParent().getWidth();

        return RenderSystem.getWidth();
    }

    public double getParentHeight() {

        if (this.getParent() != null)
            return this.getParent().getHeight();

        return RenderSystem.getHeight();
    }

    public SELF setHidden(boolean hidden) {
        this.hidden = hidden;
        return (SELF) this;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isHovering() {
        return hovering;
    }

    /**
     * 获取用于渲染的 alpha.
     */
    public float getAlpha() {
        // 与父组件的 alpha 相乘
        if (this.getParent() != null)
            return this.getParent().getAlpha() * this.alpha;

        return this.alpha;
    }

    /**
     * 仅获取这个组件的 alpha.
     */
    public float getWidgetAlpha() {
        return this.alpha;
    }

    public SELF setAlpha(float alpha) {
        this.alpha = alpha;
        this.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int) (alpha * 255));
        return (SELF) this;
    }

    public SELF setOnClickCallback(OnClickCallback callback) {
        this.callback = callback;
        return (SELF) this;
    }

    /**
     * @return 返回这个组件在屏幕上 相对于 (0, 0) 的 X 坐标。
     */
    public double getX() {

        // 如果父组件为空则直接返回 X 坐标
        if (this.getParent() == null)
            return this.getBounds().x;

        return this.getParent().getX() + this.getBounds().x;
    }

    /**
     * @return 返回这个组件在屏幕上 相对于 (0, 0) 的 Y 坐标。
     */
    public double getY() {

        // 如果父组件为空则直接返回 Y 坐标
        if (this.getParent() == null)
            return this.getBounds().y;

        return this.getParent().getY() + this.getBounds().y;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public double getRelativeX() {
        return this.getBounds().x;
    }

    public double getRelativeY() {
        return this.getBounds().y;
    }

    public double getWidth() {
        return this.getBounds().width;
    }

    public double getHeight() {
        return this.getBounds().height;
    }

    public SELF setWidth(double width) {
        this.getBounds().width = width;
        return (SELF) this;
    }

    public SELF setHeight(double height) {
        this.getBounds().height = height;
        return (SELF) this;
    }

    /**
     * 设置组件的界限。
     * @param x 相对于父组件的 X 偏移
     * @param y 相对于父组件的 Y 偏移
     * @param width 宽
     * @param height 长
     */
    public SELF setBounds(double x, double y, double width, double height) {
        this.getBounds().x = x;
        this.getBounds().y = y;
        this.getBounds().width = width;
        this.getBounds().height = height;
        return (SELF) this;
    }

    /**
     * 使组件居中。
     * @return
     */
    public SELF center() {
        return this.setPosition(this.getParentWidth() * .5 - this.getWidth() * .5, this.getParentHeight() * .5 - this.getHeight() * .5);
    }

    public SELF centerHorizontally() {
        return this.setPosition(this.getParentWidth() * .5 - this.getWidth() * .5, this.getRelativeY());
    }

    public SELF centerVertically() {
        return this.setPosition(this.getRelativeX(), this.getParentHeight() * .5 - this.getHeight() * .5);
    }

    /**
     * 设置子组件相对于父组件的坐标偏移
     * @param x X 坐标偏移
     * @param y Y 坐标偏移
     */
    public SELF setPosition(double x, double y) {
        this.getBounds().x = x;
        this.getBounds().y = y;
        return (SELF) this;
    }

    /**
     * 设置子组件渲染时的绝对坐标
     * @param x X 坐标
     * @param y Y 坐标
     */
    public SELF setPositionAbsolute(double x, double y) {
        this.getBounds().x = this.getBounds().y = 0;
        this.getBounds().x = x - this.getX();
        this.getBounds().y = y - this.getY();
        return (SELF) this;
    }

    public SELF setBounds(double width, double height) {
        this.getBounds().width = width;
        this.getBounds().height = height;
        return (SELF) this;
    }

    public boolean isClickable() {
        return clickable;
    }

    public SELF setClickable(boolean clickable) {
        this.clickable = clickable;
        return (SELF) this;
    }

    public AbstractWidget<?> getParent() {
        return parent;
    }

    public SELF setParent(AbstractWidget<?> parent) {
        this.parent = parent;
        return (SELF) this;
    }

    public List<AbstractWidget<?>> getChildren() {
        return children;
    }
}
