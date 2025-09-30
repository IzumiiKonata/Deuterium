package tritium.screens;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import tritium.management.*;
import tritium.rendering.entities.impl.Rect;
import tritium.management.FontManager;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shaders;
import tritium.settings.ClientSettings;
import tritium.utils.math.RandomUtils;

/**
 * @author IzumiiKonata
 * @since 2025/09/30
 */
public class MainMenu extends BaseScreen {

    @Getter
    private static final MainMenu instance = new MainMenu();

    Framebuffer fbConverge = null;

    public MainMenu() {

    }

    @Override
    @SneakyThrows
    public void initGui() {

    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), this.getColor(ColorType.BACKGROUND));

        fbConverge = RenderSystem.createFrameBuffer(fbConverge);

        fbConverge.setFramebufferColor(1, 1, 1, 0.0F);
        fbConverge.bindFramebuffer(true);
        fbConverge.framebufferClearNoBinding();

        CFontRenderer titleFr = FontManager.arial60bold;
        titleFr.drawCenteredString("Tritium", RenderSystem.getWidth() * .5, RenderSystem.getHeight() * .5 - titleFr.getHeight() * .5, this.getColor(ColorType.TEXT));

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

        GlStateManager.bindTexture(fbConverge.framebufferTexture);
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
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
                    case TEXT:
                        return RenderSystem.hexColor(255, 255, 255);
                }
                break;
            case Light:
                switch (type) {
                    case BACKGROUND:
                        return RenderSystem.hexColor(235, 235, 235);
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
}
