package tritium.screens;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Tuple;
import tritium.Tritium;
import tritium.interfaces.SharedConstants;
import tritium.management.*;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.clickable.impl.ClickableIcon;
import tritium.rendering.entities.clickable.impl.FlatMainMenuButton;
import tritium.rendering.Rect;
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.loading.LoadingRenderer;
import tritium.rendering.loading.LoadingScreenRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shaders;
import tritium.screens.altmanager.AltScreen;
import tritium.screens.ncm.NCMScreen;
import tritium.settings.ClientSettings;
import tritium.utils.i18n.Localizable;
import tritium.utils.other.info.UpdateChecker;
import tritium.utils.other.info.Version;

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
        FontManager.waitUntilAllLoaded();
    }

    @Override
    public void initGui() {
        // create buttons
        List<Tuple<Localizable, Runnable>> tuples = new ArrayList<>(Arrays.asList(
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
                            SharedConstants.mc.displayGuiScreen(AltScreen.getInstance());
                        }
                ),
                Tuple.of(
                        Localizable.of("mainmenu.settings"), () -> {
                            SharedConstants.mc.displayGuiScreen(new GuiOptions(MainMenu.this, SharedConstants.mc.gameSettings));
                        }
                )
        ));

        if (Tritium.getVersion().getReleaseType() == Version.ReleaseType.Dev) {
            tuples.add(
                Tuple.of(
                    Localizable.ofUntranslatable("打开音乐播放器"), () -> {
                        SharedConstants.mc.displayGuiScreen(NCMScreen.getInstance());
                    }
                )
            );
        }

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
//                    this.refreshDeconvergeThisFrame = true;

                    this.themeButton.setIcon(this.getThemeIcon());
                }, () -> {

                }, () -> {

                }, () -> {

                }, () -> {

                }
         );

        startTime = System.currentTimeMillis();
        alphas = new float[0];
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), this.getColor(ColorType.BACKGROUND));

        this.renderDeconverge();

        this.buttons.forEach(button -> button.draw(mouseX, mouseY));

        Tritium.getInstance().getThemeManager().interp();

        this.themeButton.draw(mouseX, mouseY);

        this.renderInfos();

        // render loading screen outro
        LoadingScreenRenderer renderer = LoadingRenderer.loadingScreenRenderer;
        renderer.alpha = Interpolations.interpBezier(renderer.alpha, 0, .1f);
        renderer.render(RenderSystem.getWidth(), RenderSystem.getHeight());
    }

    private EnumChatFormatting mapCheckResultToColor(UpdateChecker.UpdateCheckResult result) {
        return switch (result) {
            case UP_TO_DATE -> EnumChatFormatting.GREEN;
            case OUTDATED_NEW_RELEASE, OUTDATED_NEW_COMMIT -> EnumChatFormatting.YELLOW;
            case ERROR -> EnumChatFormatting.RED;
            case CHECKING -> EnumChatFormatting.GRAY;
        };
    }

    private void renderInfos() {

        List<String> infos = Arrays.asList(
                "https://github.com/IzumiiKonata/Deuterium",
                String.format(
                        "Tritium-X %s %s%s",
                        Tritium.getVersion(),
                        mapCheckResultToColor(UpdateChecker.getUpdateCheckResult()),
                        UpdateChecker.getUpdateCheckResult().getLocalizable().get()
                )
        );

        CFontRenderer fr = FontManager.pf14;
        double yOffset = RenderSystem.getHeight() - fr.getHeight() - 4;

        for (String info : infos) {
            fr.drawCenteredString(info, RenderSystem.getWidth() * .5, yOffset, this.getColor(ColorType.TEXT));
            yOffset -= fr.getHeight() + 4;
        }

    }

//    private boolean refreshDeconvergeThisFrame = false;

    private void renderDeconverge() {
        fbConverge = RenderSystem.createFrameBuffer(fbConverge);

        boolean dev = Tritium.getVersion().getReleaseType() == Version.ReleaseType.Dev;

        fbConverge.setFramebufferColor(this.getColor(ColorType.BACKGROUND), 0.0F);
        fbConverge.bindFramebuffer(true);
        fbConverge.framebufferClearNoBinding();

        if (dev) {
            this.renderDevDeconverge();
        }

        CFontRenderer titleFr = FontManager.arial60bold;
        titleFr.drawCenteredString("Tritium", RenderSystem.getWidth() * .5, RenderSystem.getHeight() / 3.0d, this.getColor(ColorType.TEXT));

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

        GlStateManager.bindTexture(fbConverge.framebufferTexture);
        Shaders.DECONVERGE.render();
    }

    long startTime = System.currentTimeMillis();
    float[] alphas = new float[0];

    private void renderDevDeconverge() {

        CFontRenderer fr = FontManager.pf65bold;

        boolean obfuscatedDev = Tritium.getInstance().isObfuscated();
        String str = obfuscatedDev ? "你他妈在干什么" : "所有人操大逼";

        char[] charArray = str.toCharArray();

        if (alphas.length != str.length()) {
            alphas = new float[str.length()];
        }

        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];

            long time = System.currentTimeMillis() - startTime;

            double angX = time * .125 - (i * 30);
            double angY = time * .25 - (360.0f / charArray.length * i);

            double x = angX <= 0 ? 0 : Math.sin(-Math.toRadians(angX % 360.0f)) * 128;
            double y = angY <= 0 ? 0 : Math.sin(-Math.toRadians(angY % 360.0f)) * 36;

            float alpha = alphas[i];
            alphas[i] = Interpolations.interpBezier(alpha, angX > 0 && angY > 0 ? 1 : 0, .1f);

            fr.drawString(String.valueOf(c), RenderSystem.getWidth() * .5 + x, RenderSystem.getHeight() / 1.375d + y, reAlpha(this.getColor(ColorType.TEXT), alpha));
        }

    }

    // colors
    public int getColor(ColorType type) {
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
