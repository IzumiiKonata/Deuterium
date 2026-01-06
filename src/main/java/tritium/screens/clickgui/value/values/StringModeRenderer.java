package tritium.screens.clickgui.value.values;

import org.lwjgl.input.Mouse;
import tritium.management.FontManager;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.settings.StringModeSetting;

import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 18:35
 */
public class StringModeRenderer extends AbstractWidget<StringModeRenderer> {

    private final StringModeSetting setting;

    public StringModeRenderer(StringModeSetting setting) {
        this.setting = setting;

        CFontRenderer pf14 = FontManager.pf14;
        double height = pf14.getHeight() + 4;
        this.setBounds(142, height);

        LabelWidget label = new LabelWidget(() -> setting.getName().get(), pf14);
        label
            .setPosition(0, height * .5 - pf14.getHeight() * .5)
            .setBeforeRenderCallback(() -> {
                label.setColor(ClickGui.getColor(20));
            });
        this.addChild(label);

        RectWidget button = new RectWidget() {

            boolean open = false;
            boolean clickThisFrame = false;
            double width, height;

            {
                this.setOnClickCallback((relativeX, relativeY, mouseButton) -> {
                    if (mouseButton == 0) {

                        if (open)
                            clickThisFrame = true;

                        open = !open;
                    }
                    return true;
                });
            }

            @Override
            public void onRender(double mouseX, double mouseY) {
                double targetW = (open ? this.getMaxEntryWidth() : pf14.getWidth(setting.getValue())) + 4;
                double targetH = this.getEntryHeight() * (this.open ? (setting.getModes().size() + 1) : 1);

                width = Interpolations.interpBezier(width, targetW, 0.2);
                height = Interpolations.interpBezier(height, targetH, 0.2);

                StringModeRenderer.this.setHeight(height);

                this.setBounds(width, height);
                this.setPosition(StringModeRenderer.this.getWidth() - this.getWidth(), 0);
                this.setColor(this.isHovering() ? ClickGui.getColor(24) : ClickGui.getColor(23));

                super.onRender(mouseX, mouseY);
                FontManager.pf14.drawCenteredString(setting.getValue(), this.getX() + this.getWidth() * .5, this.getY() + this.getEntryHeight() * .5 - pf14.getHeight() * .5, RenderSystem.reAlpha(ClickGui.getColor(20), this.getAlpha()));

                StencilClipManager.beginClip(() -> {
                    super.onRender(mouseX, mouseY);
                });

                boolean shouldRender = open || Math.abs(width - targetW) > .5 || Math.abs(height - targetH) > .5;

                if (shouldRender) {

                    List<String> constants = setting.getModes();
                    for (int i = 0; i < constants.size(); i++) {
                        String mode = constants.get(i);

                        boolean hovered = this.isHovered(mouseX, mouseY, this.getX(), this.getY() + (i + 1) * this.getEntryHeight(), this.getMaxEntryWidth() + 4, this.getEntryHeight());
                        Rect.draw(this.getX(), this.getY() + (i + 1) * this.getEntryHeight(), this.getMaxEntryWidth() + 4, this.getEntryHeight(), hovered ? ClickGui.getColor(24) : ClickGui.getColor(23));
                        FontManager.pf14.drawCenteredString(mode, this.getX() + (this.getMaxEntryWidth() + 4) * .5, this.getY() + (i + 1) * this.getEntryHeight() + this.getEntryHeight() * .5 - pf14.getHeight() * .5, RenderSystem.reAlpha(ClickGui.getColor(20), this.getAlpha()));

                        if (hovered && Mouse.isButtonDown(0) && clickThisFrame) {
                            clickThisFrame = false;
                            setting.setValue(mode);
                            open = false;
                        }
                    }
                }

                StencilClipManager.endClip();
            }

            private double getMaxEntryWidth() {
                double maxWidth = 0;

                for (String mode : setting.getModes()) {
                    double width = pf14.getWidth(mode);
                    if (width > maxWidth) {
                        maxWidth = width;
                    }
                }

                return maxWidth;
            }

            private double getEntryHeight() {
                return pf14.getHeight() + 4;
            }
        };

        button.setShouldOverrideMouseCursor(true);

        this.addChild(button);
    }

    @Override
    public double getHeight() {

        this.setHidden(!setting.shouldRender());

        if (!setting.shouldRender())
            return 0;

        return super.getHeight();
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

    }
}
