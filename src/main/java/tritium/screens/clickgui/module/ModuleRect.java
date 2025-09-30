package tritium.screens.clickgui.module;

import tritium.management.FontManager;
import tritium.module.Module;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 16:46
 */
public class ModuleRect extends AbstractWidget<ModuleRect> {

    private Module module;

    public ModuleRect(Module module) {
        this.module = module;
        this.setBounds(142, 20);

        if (!this.module.getSettings().isEmpty()) {
            RectWidget settingsRect = new RectWidget();
            settingsRect.setBounds(132, 0, 10, this.getHeight());
            settingsRect.setColor(ClickGui.getInstance().getColor(8));
            settingsRect.setBeforeRenderCallback(() -> {
                settingsRect.setColor(settingsRect.isHovering() ? ClickGui.getInstance().getColor(12) : ClickGui.getInstance().getColor(11));
            });

            RectWidget dotCenter = new RectWidget();
            dotCenter.setBounds(1, 1);
            dotCenter.setColor(-1);
            dotCenter.setClickable(false);
            settingsRect.addChild(dotCenter);
            dotCenter.center();

            RectWidget dotTop = new RectWidget();
            dotTop.setBounds(1, 1);
            dotTop.setColor(-1);
            dotTop.setClickable(false);
            settingsRect.addChild(dotTop);
            dotTop.center();
            dotTop.setPosition(dotTop.getRelativeX(), dotTop.getRelativeY() - 3);

            RectWidget dotBottom = new RectWidget();
            dotBottom.setBounds(1, 1);
            dotBottom.setColor(-1);
            dotBottom.setClickable(false);
            settingsRect.addChild(dotBottom);
            dotBottom.center();
            dotBottom.setPosition(dotBottom.getRelativeX(), dotBottom.getRelativeY() + 3);

            this.addChild(settingsRect);
        }

        this.moduleEnableAlpha = module.isEnabled() ? 1f : 0f;

        this.setOnClickCallback((rx, ry, i) -> {
            module.toggle();
            return true;
        });
    }

    float moduleEnableAlpha;
    double nameHoverAnimation = 0;

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        this.setColor(this.isHovering() ? ClickGui.getInstance().getColor(10) : ClickGui.getInstance().getColor(8));
        Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getHexColor());

        Rect.draw(this.getX(), this.getY(), 2, this.getHeight(), RenderSystem.hexColor(223, 1, 1, (int) (this.getAlpha() * 255)));

        this.moduleEnableAlpha = Interpolations.interpBezier(this.moduleEnableAlpha, this.module.isEnabled() ? 1f : 0f, 0.2f);

        if (this.moduleEnableAlpha > .05f)
            Rect.draw(this.getX(), this.getY(), 2, this.getHeight(), RenderSystem.hexColor(1, 223, 1, (int) (Math.min(this.moduleEnableAlpha, this.getAlpha()) * 255)));

        this.nameHoverAnimation = Interpolations.interpBezier(this.nameHoverAnimation, this.isHovering() ? 4 : 0, 0.3f);

        FontManager.pf18.drawString(this.module.getName().get(), this.getX() + 8 + this.nameHoverAnimation, this.getY() + this.getHeight() * .5 - FontManager.pf18.getHeight() * .5, RenderSystem.reAlpha(ClickGui.getInstance().getColor(9), this.getAlpha()));
    }
}
