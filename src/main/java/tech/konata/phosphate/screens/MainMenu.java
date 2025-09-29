package tech.konata.phosphate.screens;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import tech.konata.phosphate.management.*;
import tech.konata.phosphate.rendering.entities.impl.Rect;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public class MainMenu extends BaseScreen {

    @Getter
    private static final MainMenu instance = new MainMenu();

    public MainMenu() {

    }

    @Override
    @SneakyThrows
    public void initGui() {
        FontManager.waitIfNotLoaded();
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        Rect.draw(100, 100, 200, 100, 0xff0090ff, Rect.RectType.EXPAND);
    }
}
