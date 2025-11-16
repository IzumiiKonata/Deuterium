package tritium.screens.clickgui.module;

import tritium.management.FontManager;
import tritium.module.Module;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.settings.ClientSettings;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 16:46
 */
public class ModuleRect extends AbstractWidget<ModuleRect> {

    protected Module module;

    public ModuleRect(Module module) {
        this.module = module;
        this.setBounds(142, 20);

        if (!this.module.getSettings().isEmpty()) {
            RectWidget settingsRect = new RectWidget();
            settingsRect.setBounds(132, 0, 10, this.getHeight());
            settingsRect.setColor(ClickGui.getColor(8));
            settingsRect.setBeforeRenderCallback(() -> {
                settingsRect.setColor(settingsRect.isHovering() ? ClickGui.getColor(12) : ClickGui.getColor(11));
            });

            RectWidget dotCenter = new RectWidget();
            dotCenter.setBounds(1, 1);
            dotCenter.setColor(ClickGui.getColor(16));
            dotCenter.setClickable(false);
            settingsRect.addChild(dotCenter);
            dotCenter.center();
            dotCenter.setBeforeRenderCallback(() -> dotCenter.setColor(ClickGui.getColor(16)));

            RectWidget dotTop = new RectWidget();
            dotTop.setBounds(1, 1);
            dotTop.setColor(ClickGui.getColor(16));
            dotTop.setClickable(false);
            settingsRect.addChild(dotTop);
            dotTop.center();
            dotTop.setPosition(dotTop.getRelativeX(), dotTop.getRelativeY() - 3);
            dotTop.setBeforeRenderCallback(() -> dotTop.setColor(ClickGui.getColor(16)));

            RectWidget dotBottom = new RectWidget();
            dotBottom.setBounds(1, 1);
            dotBottom.setColor(ClickGui.getColor(16));
            dotBottom.setClickable(false);
            settingsRect.addChild(dotBottom);
            dotBottom.center();
            dotBottom.setPosition(dotBottom.getRelativeX(), dotBottom.getRelativeY() + 3);
            dotBottom.setBeforeRenderCallback(() -> dotBottom.setColor(ClickGui.getColor(16)));

            this.addChild(settingsRect);

            settingsRect.setOnClickCallback((rx, ry, i) -> {

                if (i != 0)
                    return true;

                ModuleListWindow moduleList = ClickGui.getInstance().getModuleListWindow();
                if (moduleList.getOnSetting() == this.module)
                    return true;
                if (moduleList.getLastOnSetting() == null) {
                    moduleList.setLastOnSetting(this.module);
                } else if (moduleList.getLastOnSetting() == moduleList.getOnSetting()) {
                    moduleList.setLastOnSetting(moduleList.getOnSetting());
                }

                moduleList.setOnSetting(this.module);

                return true;
            });
        }

        this.moduleEnableAlpha = module.isEnabled() ? 1f : 0f;

        this.setOnClickCallback((rx, ry, i) -> {

            if (i == 0 && this.module != ClientSettings.settingsModule)
                module.toggle();

            return true;
        });
    }

    float moduleEnableAlpha;
    double nameHoverAnimation = 0;

    @Override
    public void onRender(double mouseX, double mouseY) {
        this.setColor(this.isHovering() ? ClickGui.getColor(10) : ClickGui.getColor(8));
        Rect.draw(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getHexColor());

        if (this.module != ClientSettings.settingsModule) {
            Rect.draw(this.getX(), this.getY(), 2, this.getHeight(), RenderSystem.hexColor(223, 1, 1, (int) (this.getAlpha() * 255)));

            this.moduleEnableAlpha = Interpolations.interpBezier(this.moduleEnableAlpha, this.module.isEnabled() ? 1f : 0f, 0.2f);

            if (this.moduleEnableAlpha > .05f)
                Rect.draw(this.getX(), this.getY(), 2, this.getHeight(), RenderSystem.hexColor(1, 223, 1, (int) (Math.min(this.moduleEnableAlpha, this.getAlpha()) * 255)));
        }

        this.nameHoverAnimation = Interpolations.interpBezier(this.nameHoverAnimation, this.isHovering() ? 4 : 0, 0.3f);

        FontManager.pf18.drawString(this.module.getName().get(), this.getX() + 8 + this.nameHoverAnimation, this.getY() + this.getHeight() * .5 - FontManager.pf18.getHeight() * .5, RenderSystem.reAlpha(ClickGui.getColor(9), this.getAlpha()));
    }

    @Override
    public boolean isHidden() {
        return !this.module.getShouldRender().get();
    }
}
