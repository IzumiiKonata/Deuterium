package tritium.screens.clickgui.module;

import lombok.Getter;
import lombok.Setter;
import tritium.management.ModuleManager;
import tritium.management.WidgetsManager;
import tritium.module.Module;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.category.CategoriesWindow;
import tritium.widget.Widget;

import java.util.Comparator;

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

    private ScrollPanel scrollPanel;

    @Override
    public void init() {
        this.baseRect.getChildren().clear();

        this.baseRect.setBounds(150, 300);
        this.baseRect.setBeforeRenderCallback(() -> {
            CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
            this.baseRect.setPosition(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth(), categoriesWindow.getTopRect().getY());
            this.baseRect.setColor(ClickGui.getColor(3));
        });

        scrollPanel = new ScrollPanel();

        this.baseRect.addChild(scrollPanel);
        scrollPanel.setMargin(4);
        scrollPanel.setSpacing(4);

        this.refreshModules(0);
    }

    public void refreshModules() {

        this.scrollPanel.getChildren().clear();
        this.scrollPanel.targetScrollOffset = 0;

        int index = ClickGui.getInstance().getCategoriesWindow().getSelectedCategoryIndex();

        this.refreshModules(index);
    }

    private void refreshModules(int index) {
        // modules
        if (index == 0) {
            for (Module module : ModuleManager.getModules()) {
                scrollPanel.addChild(new ModuleRect(module));
            }
        }

        // widgets
        if (index == 1) {
            for (Widget module : WidgetsManager.getWidgets()) {
                scrollPanel.addChild(new ModuleRect(module));
            }
        }

        // sorting
        scrollPanel.getChildren().sort(Comparator.comparing(o -> ((ModuleRect) o).module.getName().get()));
    }

    @Override
    public void render(double mouseX, double mouseY) {

        if (ClickGui.getInstance().getCategoriesWindow().getSelectedCategoryIndex() >= 2) {
            return;
        }

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
