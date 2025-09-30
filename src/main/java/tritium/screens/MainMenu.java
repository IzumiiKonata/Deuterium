package tritium.screens;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.Tuple;
import tritium.Tritium;
import tritium.interfaces.SharedConstants;
import tritium.management.*;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.clickable.impl.ClickableIcon;
import tritium.rendering.entities.clickable.impl.FlatMainMenuButton;
import tritium.rendering.entities.impl.Rect;
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.loading.LoadingRenderer;
import tritium.rendering.loading.LoadingScreenRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shaders;
import tritium.settings.ClientSettings;
import tritium.utils.i18n.Localizable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2025/09/30
 */
public class MainMenu extends BaseScreen {

    @Getter
    private static final MainMenu instance = new MainMenu();

    Framebuffer fbConverge = null;
    List<FlatMainMenuButton> buttons = new ArrayList<>();

    ClickableIcon themeButton;

    public MainMenu() {

    }

    @Override
    public void initGui() {
        // create buttons
        List<Tuple<Localizable, Runnable>> tuples = Arrays.asList(
            Tuple.of(
                    Localizable.of("mainmenu.singleplayer"), () -> {
                        SharedConstants.mc.displayGuiScreen(new GuiSelectWorld(MainMenu.this));
                    }
            ),
            Tuple.of(
                    Localizable.of("mainmenu.multiplayer"), () -> {
                        SharedConstants.mc.displayGuiScreen(new GuiMultiplayer(MainMenu.this));
                    }
            ),
            Tuple.of(
                    Localizable.of("mainmenu.altmanager"), () -> {

                    }
            ),
            Tuple.of(
                    Localizable.of("mainmenu.settings"), () -> {
                        SharedConstants.mc.displayGuiScreen(new GuiOptions(MainMenu.this, SharedConstants.mc.gameSettings));
                    }
            )
        );

        double buttonWidth = 80;
        double buttonHeight = 30;

        double offsetX = RenderSystem.getWidth() / 2.0d - (buttonWidth * tuples.size()) / 2.0d;

        this.buttons.clear();

        for (Tuple<Localizable, Runnable> tuple : tuples) {
            buttons.add(
                new FlatMainMenuButton(
                    offsetX, RenderSystem.getHeight() * .5,
                    buttonWidth, buttonHeight,
                    tuple.getFirst(),
                    this.getColor(ColorType.BUTTON),
                    (x, y, i) -> tuple.getSecond().run()
                )
            );

            offsetX += buttonWidth;
        }

        this.themeButton = new ClickableIcon(
                this.getThemeIcon(),
                FontManager.tritium42,
                8, 8,
                32, 32
                , () -> {
                    ClientSettings.THEME.setValue(ClientSettings.THEME.getValue() == ThemeManager.Theme.Dark ? ThemeManager.Theme.Light : ThemeManager.Theme.Dark);

                    this.buttons.forEach(b -> b.setBackgroundColor(this.getColor(ColorType.BUTTON)));
                    this.refreshDeconvergeThisFrame = true;

                    this.themeButton.setIcon(this.getThemeIcon());
                }, () -> {

                }, () -> {

                }, () -> {

                }, () -> {

                }
         );
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), this.getColor(ColorType.BACKGROUND));

        this.renderDeconverge();

        this.buttons.forEach(button -> button.draw(mouseX, mouseY));

        Tritium.getInstance().getThemeManager().interp();

        this.themeButton.draw(mouseX, mouseY);

        // render loading screen outro
        LoadingScreenRenderer renderer = LoadingRenderer.loadingScreenRenderer;
        renderer.alpha = Interpolations.interpBezier(renderer.alpha, 0, .1f);
        renderer.render(RenderSystem.getWidth(), RenderSystem.getHeight());
    }

    private boolean refreshDeconvergeThisFrame = false;

    private void renderDeconverge() {
        int prevWidth = fbConverge != null ? fbConverge.framebufferWidth : 0;
        int prevHeight = fbConverge != null ? fbConverge.framebufferHeight : 0;
        fbConverge = RenderSystem.createFrameBuffer(fbConverge);

        boolean shouldUpdate = refreshDeconvergeThisFrame || prevWidth != fbConverge.framebufferWidth || prevHeight != fbConverge.framebufferHeight;

        if (shouldUpdate) {
            fbConverge.setFramebufferColor(this.getColor(ColorType.BACKGROUND), 0.0F);
            fbConverge.bindFramebuffer(true);
            fbConverge.framebufferClearNoBinding();

            CFontRenderer titleFr = FontManager.arial60bold;
            boolean bl = titleFr._drawCenteredString("Tritium", RenderSystem.getWidth() * .5, RenderSystem.getHeight() / 3.0d, this.getColor(ColorType.TEXT));

            if (!bl)
                refreshDeconvergeThisFrame = true;

            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
        }

        GlStateManager.bindTexture(fbConverge.framebufferTexture);
        Shaders.DECONVERGE.render();
    }

    // colors
    private int getColor(ColorType type) {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();

        switch (theme) {
            case Dark:
                switch (type) {
                    case BACKGROUND:
                        return RenderSystem.hexColor(32, 32, 43);
                    case BUTTON:
                        return RenderSystem.hexColor(32, 32, 32);
                    case TEXT:
                        return RenderSystem.hexColor(255, 255, 255);
                }
                break;
            case Light:
                switch (type) {
                    case BACKGROUND:
                        return RenderSystem.hexColor(235, 235, 235);
                    case BUTTON:
                        return RenderSystem.hexColor(246, 246, 246);
                    case TEXT:
                        return RenderSystem.hexColor(0, 0, 0);
                }
                break;
        }

        return 0;
    }

    public enum ColorType {
        BACKGROUND,
        BUTTON,
        TEXT
    }

    public String getThemeIcon() {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();

        return theme == ThemeManager.Theme.Dark ? "d" : "c";
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.buttons.forEach(button -> button.mouseClicked(mouseX, mouseY, mouseButton));
    }
}
