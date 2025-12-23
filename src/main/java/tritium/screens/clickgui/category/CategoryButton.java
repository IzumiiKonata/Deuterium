package tritium.screens.clickgui.category;

import lombok.Getter;
import lombok.Setter;
import tritium.management.FontManager;
import tritium.rendering.RGBA;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.screens.ClickGui;

import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 15:39
 */
public class CategoryButton extends AbstractWidget<CategoryButton> {

    private String icon;
    private Supplier<String> textSupplier;

    @Getter
    @Setter
    private boolean selected;

    double scale = .0;
    float selectIndicatorAlpha = 0f;
    float hoverIndicatorAlpha = 0f;
    Runnable onClick;

    public CategoryButton(String icon, Supplier<String> textSupplier, double x, double y, Runnable onClick) {
        this.setBounds(x, y, 40, 40);
        this.icon = icon;
        this.textSupplier = textSupplier;
        this.onClick = onClick;

        this.setShouldSetMouseCursor(true);

        this.setOnClickCallback((relativeX, relativeY, mouseButton) -> {

            if (mouseButton == 0) {
                ClickGui.getInstance().getCategoriesWindow().getCategoryButtons().forEach(b -> {
                    if (this != b)
                        b.setSelected(false);
                });

                this.setSelected(true);
                this.onClick.run();
            }

            return true;
        });
    }

    @Override
    public void onRender(double mouseX, double mouseY) {

        this.scale = Interpolations.interpBezier(this.scale, selected ? 1 : 0, 0.4);
        this.selectIndicatorAlpha = Interpolations.interpBezier(this.selectIndicatorAlpha, selected ? 1 : 0, 0.3f);

        double radius = 6;

        this.roundedRect(
                this.getX() + this.getWidth() * .5 - this.getWidth() * .5 * scale,
                this.getY() + this.getHeight() * .5 - this.getHeight() * .5 * scale,
                this.getWidth() * scale,
                this.getHeight() * scale,
                radius * scale,
                RenderSystem.reAlpha(ClickGui.getColor(7), Math.min(this.selectIndicatorAlpha, this.getAlpha()))
        );

        this.hoverIndicatorAlpha = Interpolations.interpBezier(this.hoverIndicatorAlpha, CategoryButton.this.isHovering() ? .15f : 0, 0.2f);

        this.roundedRect(
                this.getX(), this.getY(),
                this.getWidth(), this.getHeight(),
                radius,
                RGBA.color(255, 255, 255, (int) (Math.min(this.hoverIndicatorAlpha, this.getAlpha()) * 255))
        );

        FontManager.tritium42.drawCenteredString(this.icon, this.getX() + this.getWidth() * .5, this.getY() + this.getHeight() * .2, RenderSystem.reAlpha(this.isSelected() ? ClickGui.getColor(5) : ClickGui.getColor(4), this.getAlpha()));

        FontManager.pf14.drawCenteredString(this.textSupplier.get(), this.getX() + this.getWidth() * .5, this.getY() + this.getHeight() * .75, RenderSystem.reAlpha(this.isSelected() ? ClickGui.getColor(5) : ClickGui.getColor(4), this.getAlpha()));

    }
}
