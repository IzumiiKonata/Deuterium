package tritium.screens.clickgui.value;

import tritium.management.FontManager;
import tritium.module.Module;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.category.CategoriesWindow;
import tritium.screens.clickgui.module.ModuleListWindow;
import tritium.screens.clickgui.value.values.BooleanRenderer;
import tritium.settings.BooleanSetting;
import tritium.settings.Setting;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 17:27
 */
public class ValueWindow extends Window {

    private boolean open;
    private double stencilWidth = 0;

    RectWidget baseRect = new RectWidget();
    ScrollPanel settingsPanel;

    @Override
    public void init() {
        this.baseRect.getChildren().clear();

        this.baseRect.setBounds(150, 300);
        this.baseRect.setBeforeRenderCallback(() -> {
            CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
            ModuleListWindow moduleListWindow = ClickGui.getInstance().getModuleListWindow();
            this.baseRect.setPosition(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth() + moduleListWindow.getBaseRect().getWidth(), categoriesWindow.getTopRect().getY());
            this.baseRect.setColor(ClickGui.getColor(15));
        });

        LabelWidget back = new LabelWidget("-", FontManager.pf25bold);

        back.setColor(ClickGui.getColor(17));
        back.setPosition(4, 2);

        back.setBeforeRenderCallback(() -> {
            back.setColor(back.isHovering() ? ClickGui.getColor(18) : ClickGui.getColor(17));
        });

        back.setOnClickCallback((mouseX, mouseY, mouseButton) -> {

            if (mouseButton == 0) {
                ModuleListWindow moduleListWindow = ClickGui.getInstance().getModuleListWindow();

                moduleListWindow.setLastOnSetting(moduleListWindow.getOnSetting());
                moduleListWindow.setOnSetting(null);
            }

            return true;
        });

        this.baseRect.addChild(back);

        settingsPanel = new ScrollPanel();

        this.baseRect.addChild(settingsPanel);

        settingsPanel.setMargin(4);
        settingsPanel.setBounds(settingsPanel.getRelativeX(), settingsPanel.getRelativeY() + 16, settingsPanel.getWidth(), settingsPanel.getHeight() - 16);
        settingsPanel.setSpacing(4);
    }

    @Override
    public void render(double mouseX, double mouseY) {
        ModuleListWindow moduleList = ClickGui.getInstance().getModuleListWindow();

        open = (moduleList.getOnSetting() != null) && (moduleList.getOnSetting() == moduleList.getLastOnSetting());

        this.stencilWidth = Interpolations.interpBezier(this.stencilWidth, open ? 150 : 0, 0.3f);

        if (!open && this.stencilWidth <= 1) {
            moduleList.setLastOnSetting(moduleList.getOnSetting());
            this.settingsPanel.targetScrollOffset = 0;
            this.settingsPanel.getChildren().clear();
        }

        CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
        ModuleListWindow moduleListWindow = ClickGui.getInstance().getModuleListWindow();

        StencilClipManager.beginClip(() -> {
            Rect.draw(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth() + moduleListWindow.getBaseRect().getWidth(), this.baseRect.getY(), this.stencilWidth, this.baseRect.getHeight(), -1);
        });

        this.baseRect.renderWidget(mouseX, mouseY, this.getDWheel());

        if (moduleList.getOnSetting() != null && this.settingsPanel.getChildren().isEmpty()) {
            this.settingsPanel.targetScrollOffset = 0;

            for (Setting<?> setting : moduleList.getOnSetting().getSettings()) {
                if (setting instanceof BooleanSetting)
                    this.settingsPanel.addChild(new BooleanRenderer((BooleanSetting) setting));

            }
        }

        Module module = moduleList.getLastOnSetting();
        if (module == null) {
            StencilClipManager.endClip();
            return;
        }

        FontManager.pf16.drawString(module.getName().get(), this.baseRect.getX() + 16, this.baseRect.getY() + 7, RenderSystem.reAlpha(ClickGui.getColor(19), this.baseRect.getAlpha()));

        StencilClipManager.endClip();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.baseRect.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {

    }

    @Override
    public void setAlpha(float alpha) {
        this.baseRect.setAlpha(alpha);
    }
}
