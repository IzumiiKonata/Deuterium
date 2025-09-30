package tritium.screens.clickgui.module;

import lombok.Getter;
import lombok.Setter;
import tritium.management.ModuleManager;
import tritium.module.Module;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.category.CategoriesWindow;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 16:30
 */
public class ModuleListWindow extends Window {

    @Getter
    RectWidget baseRect = new RectWidget();

    @Getter
    @Setter
    public Module onHover;
    @Getter
    @Setter
    public Module lastOnSetting;
    @Getter
    @Setter
    public Module onSetting;

    @Override
    public void init() {
        this.baseRect.getChildren().clear();

        this.baseRect.setBounds(150, 300);
        this.baseRect.setBeforeRenderCallback(() -> {
            CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
            this.baseRect.setPosition(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth(), categoriesWindow.getTopRect().getY());
            this.baseRect.setColor(ClickGui.getColor(3));
        });

        ScrollPanel scrollPanel = new ScrollPanel();

        this.baseRect.addChild(scrollPanel);
        scrollPanel.setMargin(4);
        scrollPanel.setSpacing(4);

        for (Module module : ModuleManager.getModules()) {
            scrollPanel.addChild(new ModuleRect(module));
        }

    }

    @Override
    public void render(double mouseX, double mouseY) {
        this.baseRect.renderWidget(mouseX, mouseY, this.getDWheel());
    }

    @Override
    public void setAlpha(float alpha) {
        this.baseRect.setAlpha(alpha);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.baseRect.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }
}
