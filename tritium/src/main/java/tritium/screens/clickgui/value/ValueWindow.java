package tritium.screens.clickgui.value;

import tritium.management.FontManager;
import tritium.module.Module;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.category.CategoriesWindow;
import tritium.screens.clickgui.module.ModuleListWindow;
import tritium.screens.clickgui.value.values.*;
import tritium.settings.*;

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

        back
            .setPosition(4, 2)
            .setShouldOverrideMouseCursor(true)
            .setColor(ClickGui.getColor(17))
            .setBeforeRenderCallback(() -> {
                back.setColor(back.isHovering() ? ClickGui.getColor(18) : ClickGui.getColor(17));
            })
            .setOnClickCallback((mouseX, mouseY, mouseButton) -> {
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

        settingsPanel
            .setMargin(4)
            .setSpacing(4)
            .setBounds(settingsPanel.getRelativeX(), settingsPanel.getRelativeY() + 16, settingsPanel.getWidth(), settingsPanel.getHeight() - 16);
    }

    @Override
    public void render(double mouseX, double mouseY) {

        if (ClickGui.getInstance().getCategoriesWindow().getSelectedCategoryIndex() >= 2) {
            return;
        }

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
                if (setting instanceof NumberSetting)
                    this.settingsPanel.addChild(new NumberRenderer((NumberSetting<?>) setting));
                if (setting instanceof ModeSetting)
                    this.settingsPanel.addChild(new ModeRenderer((ModeSetting<?>) setting));
                if (setting instanceof StringModeSetting)
                    this.settingsPanel.addChild(new StringModeRenderer((StringModeSetting) setting));
                if (setting instanceof ColorSetting)
                    this.settingsPanel.addChild(new ColorRenderer((ColorSetting) setting));
                if (setting instanceof LabelSetting)
                    this.settingsPanel.addChild(new LabelRenderer((LabelSetting) setting));
                if (setting instanceof StringSetting)
                    this.settingsPanel.addChild(new StringRenderer((StringSetting) setting));
                if (setting instanceof BindSetting)
                    this.settingsPanel.addChild(new BindRenderer((BindSetting) setting));
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
    public boolean keyTyped(char typedChar, int keyCode) {
        return this.baseRect.onKeyTypedReceived(typedChar, keyCode);
    }

    @Override
    public void setAlpha(float alpha) {
        this.baseRect.setAlpha(alpha);
    }
}
