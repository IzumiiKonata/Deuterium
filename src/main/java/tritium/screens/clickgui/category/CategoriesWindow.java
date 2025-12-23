package tritium.screens.clickgui.category;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import org.lwjgl.input.Mouse;
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.MoveWidgetsScreen;
import tritium.screens.clickgui.Window;
import tritium.utils.i18n.Localizable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/9/30 12:13
 */
public class CategoriesWindow extends Window {

    @Getter
    RectWidget topRect = new RectWidget();

    RectWidget bottomRect = new RectWidget();

    boolean topRectDragging = false;
    private double lastMouseX, lastMouseY;

    @Getter
    private List<CategoryButton> categoryButtons = new ArrayList<>();

    public CategoriesWindow() {
        this.layout();
    }

    @Override
    public void init() {
        this.topRectDragging = false;

//        layout();
    }

    private void layout() {
        this.topRect.setBounds(100, 100, 100, 40);

        this.topRect.setOnClickCallback((rx, ry, i) -> {
            this.topRectDragging = true;
            return true;
        });

        this.topRect.setPosition(100, 100);

        // banner
        {
            Panel bannerPanel = new Panel();

            CFontRenderer arial18bold = FontManager.arial18bold;
            bannerPanel.setBeforeRenderCallback(() -> {
                bannerPanel.setBounds(12 + arial18bold.getWidth("Tritium"), arial18bold.getHeight());
                bannerPanel.center();
            });

            LabelWidget tritiumLogo = new LabelWidget("a", FontManager.tritium24)
                    .setPosition(0, -1)
                    .setClickable(false);

            LabelWidget tritiumLabel = new LabelWidget("Tritium", FontManager.arial18bold)
                    .setPosition(12, 0)
                    .setClickable(false);

            tritiumLogo.setBeforeRenderCallback(() -> tritiumLogo.setColor(this.getColor(1)));
            tritiumLabel.setBeforeRenderCallback(() -> tritiumLabel.setColor(this.getColor(1)));

            bannerPanel.addChild(tritiumLogo, tritiumLabel);
            bannerPanel.setClickable(false);

            // DBG
            this.topRect.getChildren().clear();
            // END DBG

            this.topRect.addChild(bannerPanel);
        }

        this.bottomRect.setBeforeRenderCallback(() -> {
            this.bottomRect.setPosition(this.topRect.getX(), this.topRect.getY() + this.topRect.getHeight());
        });

        this.bottomRect.setBounds(100, 260);

        // DBG
        this.bottomRect.getChildren().clear();
        // END DBG

        // categories
        {
            List<Tuple<String, Localizable>> categories = Arrays.asList(
                    Tuple.of(
                            "e", Localizable.of("category.modules.name")
                    ),
                    Tuple.of(
                            "f", Localizable.of("category.widgets.name")
                    )/*,
                    Tuple.of(
                            "f", Localizable.of("category.music.name")
                    )*/
            );

            List<Runnable> onClick = Arrays.asList(
                    () -> {
                        ClickGui.getInstance().getModuleListWindow().refreshModules();
                    },
                    () -> {
                        ClickGui.getInstance().getModuleListWindow().refreshModules();
                    },
                    () -> {

                    }
            );

            int numbersOfCategories = categories.size();
            double spacing = 20;

            double totalHeight = numbersOfCategories * (40 + spacing) - spacing;

            Panel categoriesPanel = new Panel();

            this.bottomRect.addChild(categoriesPanel);

            categoriesPanel
                .setBounds(40, totalHeight)
                .setPosition(
                    categoriesPanel.getParentWidth() * .5 - categoriesPanel.getWidth() * .5,
                    (categoriesPanel.getParentHeight() - (14 * categories.size())) * .5 - categoriesPanel.getHeight() * .5
                );

            categoryButtons.clear();

            for (int i = 0; i < categories.size(); i++) {
                Tuple<String, Localizable> category = categories.get(i);
                CategoryButton button = new CategoryButton(category.getA(), () -> category.getB().get(), 0, i * (40 + spacing), onClick.get(i));
                categoriesPanel.addChild(button);
                categoryButtons.add(button);
            }

            this.categoryButtons.getFirst().setSelected(true);
        }

        // edit button
        {
            LabelWidget edit = new LabelWidget("b", FontManager.tritium42);
            edit
                    .setShouldSetMouseCursor(true)
                    .setColor(this.getColor(4))
                    .setBeforeRenderCallback(() -> {
                        edit.setColor(this.getColor(edit.isHovering() ? 5 : 4));
                        edit.centerHorizontally();
                        edit.setPosition(edit.getRelativeX(), edit.getParentHeight() - 24 - edit.getHeight());
                    })
                    .setOnClickCallback((rx, ry, i) -> {
                        if (i == 0) {
                            Minecraft.getMinecraft().displayGuiScreen(new MoveWidgetsScreen());
                        }
                        return true;
                    });

            Localizable lEditText = Localizable.of("clickgui.edit");

            LabelWidget editText = new LabelWidget(lEditText::get, FontManager.pf14);
            editText.setBeforeRenderCallback(() -> {
                editText.setColor(this.getColor(edit.isHovering() ? 5 : 4));
                editText.centerHorizontally();
                editText.setPosition(editText.getRelativeX(), editText.getParentHeight() - 20);
            });

            this.bottomRect.addChild(edit, editText);
        }
    }

    @Override
    public void render(double mouseX, double mouseY) {

        this.topRect.setColor(this.getColor(0));
        this.bottomRect.setColor(this.getColor(2));

        if (!Mouse.isButtonDown(0))
            this.topRectDragging = false;

        // dragging
        {
            if (this.topRectDragging) {
                double w = this.lastMouseX - this.topRect.getX();
                double h = this.lastMouseY - this.topRect.getY();

                this.setX(mouseX - w);
                this.setY(mouseY - h);
                this.topRect.setPosition(mouseX - w, mouseY - h);
            }

            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
        }

        this.topRect.renderWidget(mouseX, mouseY, this.getDWheel());
        this.bottomRect.renderWidget(mouseX, mouseY, this.getDWheel());
    }

    @Override
    public void setAlpha(float alpha) {
        this.topRect.setAlpha(alpha);
        this.bottomRect.setAlpha(alpha);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.topRect.onMouseClickReceived(mouseX, mouseY, mouseButton);
        this.bottomRect.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {

    }

    public int getSelectedCategoryIndex() {
        for (int i = 0; i < this.categoryButtons.size(); i++) {
            CategoryButton cb = this.categoryButtons.get(i);

            if (cb.isSelected())
                return i;
        }

        return -1;
    }

}
