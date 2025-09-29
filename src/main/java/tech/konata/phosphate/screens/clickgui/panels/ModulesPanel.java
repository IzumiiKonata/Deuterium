package tech.konata.phosphate.screens.clickgui.panels;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.module.Module;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.entities.impl.TextField;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.screens.ClickGui;
import tech.konata.phosphate.screens.clickgui.ModuleSettings;
import tech.konata.phosphate.screens.clickgui.Panel;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.widget.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModulesPanel extends Panel {

    public boolean lmbPressed, rmbPressed;

    public ModulesPanel() {
        super("Modules");
    }

    Module.Category curCategory = Module.Category.ALL;

    List<ModuleEntity> modules = new ArrayList<>();

    TextField searchBox = new TextField(0, 0, 0, 0, 0);

    @Override
    public void init() {

        for (Module module : ModuleManager.getModules()) {

            if (module.getCategory() == Module.Category.SETTING || module == GlobalSettings.dummyModule || module == GlobalSettings.dummyMusicModule)
                continue;

            modules.add(new ModuleEntity(module));
        }

        for (Widget widget : WidgetsManager.getWidgets()) {
            modules.add(new ModuleEntity(widget));
        }

        modules.sort(Comparator.comparing(m -> m.module.getName().get()));

        searchBox.setPlaceholder("Search (Ctrl + F)");

    }

    private boolean shouldShowModule(Module module) {

        if (this.searchBox.getText().isEmpty())
            return module.getShouldRender().get();

        if (!module.getShouldRender().get())
            return false;

        String text = this.searchBox.getText();

        String[] s = text.toLowerCase().split(" ");

        String lowerCase = module.getName().get().toLowerCase();

        String internalLower = module.getInternalName().toLowerCase();

        for (String split : s) {
            if (lowerCase.contains(split))
                return true;
        }

        for (String split : s) {
            if (internalLower.contains(split))
                return true;
        }

        return false;
    }

    double scroll = 0, ySmooth = 0;

    @Override
    public void onSwitchedTo() {

    }

    @Override
    public void draw(double mouseX, double mouseY, int dWheel) {

        if (ClickGui.getInstance().settingsRenderer == null) {
            double yAdd = 5;
            if (dWheel > 0)
                ySmooth -= yAdd;
            else if (dWheel < 0)
                ySmooth += yAdd;
        }

        ySmooth = Interpolations.interpBezier(ySmooth, 0, 0.08f);
        scroll = Interpolations.interpBezier(scroll, scroll + ySmooth, 1f);

        if (scroll < 0)
            scroll = Interpolations.interpBezier(scroll, 0, 0.2f);

        int spacing = 4;

        double verticalMax = (height - 48) / (spacing + 36.65);
        double totalModuleHeight = this.getTotalModuleHeight() - verticalMax * (spacing + 36.65)/* + spacing * 2*/;

        int size = searchBox.getText().isEmpty() ? modules.size() : (int) modules.stream().filter(m -> this.shouldShowModule(m.module)).count();
        if (size > verticalMax && scroll > totalModuleHeight) {
            scroll = Interpolations.interpBezier(scroll, totalModuleHeight, 0.2f);
        } else if (size < verticalMax) {
            scroll = Interpolations.interpBezier(scroll, 0, 0.2f);
        }

        FontManager.pf25bold.drawString(this.getName().get(), posX + 4, posY + 4, ThemeManager.get(ThemeManager.ThemeColor.Text));

        this.drawCategories(mouseX, mouseY);

        Stencil.write();
        this.roundedRect(posX, posY + 38 + FontManager.pf18.getHeight(), width, height - 46, 4, Color.WHITE);
        Stencil.erase();

        this.drawModules(mouseX, mouseY);

        Stencil.dispose();

        searchBox.setDrawLineUnder(false);
        searchBox.setPosition(posX + 20 + FontManager.pf25bold.getStringWidth(this.getName().get()), posY + 6);
        searchBox.setBounds(150, 10);
        if (searchBox.isFocused())
            searchBox.onTick();
        searchBox.setTextColor(ThemeManager.get(ThemeManager.ThemeColor.Text));
        searchBox.setDisabledTextColour(Color.GRAY.getRGB());

        this.roundedRect(searchBox.xPosition - 4, searchBox.yPosition - 5, searchBox.width + 8, searchBox.height + 8, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        searchBox.drawTextBox((int) mouseX, (int) mouseY);

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        if (!Mouse.isButtonDown(1) && rmbPressed)
            rmbPressed = false;

    }

    private void drawModules(double mouseX, double mouseY) {

        double startX = posX + 4, startY = posY + 42 + FontManager.pf18.getHeight() - scroll;
        double spacing = 4;

        for (ModuleEntity module : this.modules) {

            if (!shouldShowModule(module.module))
                continue;

            if (startY + module.getHeight() < posY + 20 + FontManager.pf18.getHeight()) {
                startY += spacing + module.getHeight();
                continue;
            }

            if (startY > posY + height)
                break;

            if (ClickGui.getInstance().settingsRenderer == null) {
                if (isHovered(mouseX, mouseY, startX, startY, width - 32, module.getHeight())) {

                    if (Mouse.isButtonDown(0) && !lmbPressed) {
                        lmbPressed = true;
                        module.module.toggle();
                    }

                }

                if (isHovered(mouseX, mouseY, startX + width - 32, startY, 32, module.getHeight())) {

                    if (!module.module.getSettings().isEmpty() && ((Mouse.isButtonDown(0) && !lmbPressed) || (Mouse.isButtonDown(1) && !rmbPressed))) {

                        if (Mouse.isButtonDown(0))
                            lmbPressed = true;
                        else
                            rmbPressed = true;

                        ClickGui.getInstance().settingsRenderer = new ModuleSettings(module.module);
                    }

                }

                if (isHovered(mouseX, mouseY, startX, startY, width, module.getHeight())) {
                    if (Mouse.isButtonDown(1) && !module.module.getSettings().isEmpty()) {
                        rmbPressed = true;
                        ClickGui.getInstance().settingsRenderer = new ModuleSettings(module.module);
                    }
                }
            }

            module.draw(startX, startY, width, mouseX, mouseY);

            startY += spacing + module.getHeight();
        }

    }

    private double getTotalModuleHeight() {
        double spacing = 4;
        double sum = 0;

        for (ModuleEntity module : this.modules) {
            if (!shouldShowModule(module.module))
                continue;

            sum += spacing + module.getHeight();
        }

        return sum;
    }

    private void drawModulesBloom(double mouseX, double mouseY) {

        double startX = posX + 4, startY = posY + 20 + FontManager.pf18.getHeight() - scroll;
        double spacing = 4;

        for (ModuleEntity module : this.modules) {

            if (!shouldShowModule(module.module))
                return;

            if (startY + module.getHeight() < posY + 20 + FontManager.pf18.getHeight()) {
                startY += spacing + module.getHeight();
                continue;
            }

            if (startY > posY + height)
                break;

            module.drawBloom(startX, startY, width, mouseX, mouseY);

            startY += spacing + module.getHeight();
        }

    }

    private void drawCategories(double mouseX, double mouseY) {

        CFontRenderer fr = FontManager.pf18;

        double spacing = 4, catHeight = 4 + fr.getHeight() + 4;
        double startX = posX + spacing, startY = posY + spacing + 22;

        for (Module.Category cat : Module.Category.values()) {

            if (cat == Module.Category.SETTING)
                continue;

            double catWidth = Math.max(32, 16 + fr.getStringWidth(cat.getName().get()));

            boolean isHovered = isHovered(mouseX, mouseY, startX, startY, catWidth, catHeight);

            if (ClickGui.getInstance().settingsRenderer == null && isHovered && Mouse.isButtonDown(0) && curCategory != cat && !lmbPressed) {
                lmbPressed = true;

                curCategory = cat;

                modules.clear();

                if (curCategory == Module.Category.ALL) {
                    for (Module module : ModuleManager.getModules()) {
                        modules.add(new ModuleEntity(module));
                    }
                } else if (curCategory == Module.Category.WIDGET) {
                    for (Widget module : WidgetsManager.getWidgets()) {
                        modules.add(new ModuleEntity(module));
                    }
                } else {
                    for (Module module : ModuleManager.getModulesInCategory(curCategory)) {
                        modules.add(new ModuleEntity(module));
                    }
                }


                modules.sort(Comparator.comparing(m -> m.module.getName().get()));
            }

            boolean bIsCurrent = (curCategory == cat);

            this.roundedRect(startX, startY, catWidth, catHeight, 4, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

            cat.alpha = Interpolations.interpBezier(cat.alpha, bIsCurrent ? 1 : 0, 0.2f);
            this.roundedRectAccentColor(startX, startY, catWidth, catHeight, 4, (int) (cat.alpha * 255));

            cat.hoverAlpha = Interpolations.interpBezier(cat.hoverAlpha, isHovered ? 0.1f : 0f, 0.3f);
            this.roundedRect(startX, startY, catWidth, catHeight, 4, ThemeManager.getAsColor(ThemeManager.ThemeColor.Text, (int) (cat.hoverAlpha * 255)));

            fr.drawString(cat.getName().get(), startX + catWidth * 0.5 - fr.getStringWidth(cat.getName().get()) * 0.5, startY + 4, ThemeManager.get(ThemeManager.ThemeColor.Text));

            startX += catWidth + spacing;

        }

    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {

        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.searchBox.isFocused()) {
                this.searchBox.setFocused(false);
                return true;
            }
        }

        if (searchBox.isFocused()) {
            this.searchBox.textboxKeyTyped(typedChar, keyCode);
            return true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                && keyCode == Keyboard.KEY_F
                && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
                && !(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))) {

            this.searchBox.setFocused(true);
            this.searchBox.setCursorPositionEnd();
            this.searchBox.setSelectionPos(0);
            return true;
        }

        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.searchBox.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {

        if (ClickGui.getInstance().previousPanel != null)
            return;

        this.searchBox.mouseClicked(mouseX, mouseY, button);

        if (isHovered(mouseX, mouseY, posX, posY + 38 + FontManager.pf18.getHeight(), width, height - 46)) {
            double startX = posX + 4, startY = posY + 42 + FontManager.pf18.getHeight() - scroll;
            double spacing = 4;

            for (ModuleEntity module : this.modules) {

                if (!shouldShowModule(module.module))
                    continue;

                if (startY + module.getHeight() < posY + 20 + FontManager.pf18.getHeight()) {
                    startY += spacing + module.getHeight();
                    continue;
                }

                if (startY > posY + height)
                    break;





                startY += spacing + module.getHeight();
            }
        }

    }

    private static class ModuleEntity implements SharedRenderingConstants {

        public final Module module;

        double size = 0;
        float hoveredAlpha = 0.0f;

        public ModuleEntity(Module m) {
            this.module = m;

            size = module.isEnabled() ? (this.getHeight() - 8) * 0.5 : 0;

        }

        public void draw(double x, double y, double panelWidth, double mouseX, double mouseY) {
            double width = panelWidth - 8;

            this.roundedRect(x, y, width, this.getHeight(), 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

            if (this.hoveredAlpha > 0.02f) {
                this.roundedRect(x, y, width, this.getHeight(), 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.Text, (int) (this.hoveredAlpha * 255)));
            }

            double shrink = 3;
            this.roundedRect(x + shrink, y + shrink, this.getHeight() - shrink * 2, this.getHeight() - shrink * 2, 8, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface));
            this.roundedRectAccentColor(x + shrink + (this.getHeight() - shrink * 2) * 0.5 - size * 0.5, y + shrink + (this.getHeight() - shrink * 2) * 0.5 - size * 0.5, this.size, this.size, 8 * (this.size / (this.getHeight() - 8)));

            size = Interpolations.interpBezier(size, module.isEnabled() ? (this.getHeight() - shrink * 2) : 0, 0.2);

            CFontRenderer frBig = FontManager.pf18;
            CFontRenderer frSmall = FontManager.pf16;

            String name = module.getName().get();

            if (!ClickGui.getInstance().modulesPanel.searchBox.getText().isEmpty())
                name += EnumChatFormatting.GRAY + " (" + module.getInternalName() + ")";

            frBig.drawString(name, x + 38, y + 6, ThemeManager.get(ThemeManager.ThemeColor.Text));
            frSmall.drawString(module.getDescription().get(), x + 38, y + this.getHeight() - 8 - frSmall.getHeight(), ThemeManager.get(ThemeManager.ThemeColor.Text, 160));

            if (!module.getSettings().isEmpty()) {
                SVGImage.draw(Location.of(Phosphate.NAME + "/textures/clickgui/panel/Settings.svg"), x + width - 24, y + this.getHeight() * 0.5 - 7, 16, 16, ThemeManager.get(ThemeManager.ThemeColor.Text));
            }

            boolean hovered = isHovered(mouseX, mouseY, x, y, width, this.getHeight());

            this.hoveredAlpha = Interpolations.interpBezier(hoveredAlpha, hovered ? 0.2f : 0.0f, 0.2f);



        }

        public void drawBloom(double x, double y, double panelWidth, double mouseX, double mouseY) {
            double width = panelWidth - 8;

            Color dim = ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface);

            int value = ThemeManager.getTheme() == ThemeManager.Theme.Dark ? 10 : -20;

            if (module.isEnabled()) {
                this.roundedRectAccentColor(x, y, 50, this.getHeight(), 10);
            } else {
                this.roundedRect(x, y, 50, this.getHeight(), 10, new Color(dim.getRed() + value, dim.getGreen() + value, dim.getBlue() + value));
            }

            this.roundedRect(x + width - 32, y, 32, this.getHeight(), 10, new Color(dim.getRed() + value, dim.getGreen() + value, dim.getBlue() + value));
            Rect.draw(x + 32, y, width - 48, this.getHeight(), ThemeManager.get(ThemeManager.ThemeColor.OnSurface), Rect.RectType.EXPAND);


        }

        public double getHeight() {
            return 36.65;
        }

    }
}
