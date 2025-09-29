package tech.konata.phosphate.screens;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;
import org.lwjglx.util.glu.Project;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.dnd.DropTargetHandler;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedConstants;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.*;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.loading.LoadingRenderer;
import tech.konata.phosphate.rendering.loading.screens.GenshinImpactLoadingScreen;
import tech.konata.phosphate.rendering.loading.screens.NormalLoadingScreen;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.rendering.shader.StencilShader;
import tech.konata.phosphate.screens.alt.GuiAltManager;
import tech.konata.phosphate.screens.altmanager.AltScreen;
import tech.konata.phosphate.screens.mainmenu.MainMenuButton;
import tech.konata.phosphate.screens.mainmenu.MouseShifter;
import tech.konata.phosphate.screens.mainmenu.skybox.RenderSkybox;
import tech.konata.phosphate.screens.mainmenu.skybox.RenderSkyboxCube;
import tech.konata.phosphate.screens.multiplayer.ZephyrMultiPlayerUI;
import tech.konata.phosphate.utils.other.SplashGenerator;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2023/12/17
 */
public class MainMenu extends BaseScreen {

    @Getter
    private static final MainMenu instance = new MainMenu();

    Animation logoScaleAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(1500));
    Animation logoTranslateAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(1500));
    Animation alphaAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(1000));
    Animation maskAlphaAnim = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(2000));

    public final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(Location.of("textures/gui/title/background/" + this.randomChoose() + "/panorama"));
    private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);

    MouseShifter shifter = new MouseShifter();

    public MainMenu() {
        EventManager.register(this);
    }

    float dragOverlayAlpha = 0.0f;

    boolean shouldDisplayJVMVersionWarning = this.outdatedJVMDetected();

    public boolean outdatedJVMDetected() {

        String ver = System.getProperty("java.version");

        if (!ver.contains("_"))
            return false;

        String substring = ver.substring(ver.indexOf("_") + 1);

        String parsed = "";

        for (char c : substring.toCharArray()) {

            if (c >= '0' && c <= '9') {
                parsed += c;
            } else {
                break;
            }

        }

        return Integer.parseInt(parsed) < 320;
    }

    private String randomChoose() {

        List<String> panoramas = Arrays.asList("s1.20", "classic");

        return panoramas.get(Math.abs(new Random().nextInt()) % panoramas.size());

    }

    List<MainMenuButton> buttons = new ArrayList<>();

    @Override
    @SneakyThrows
    public void initGui() {

        FontManager.waitIfNotLoaded();

        this.buttons.clear();

        List<Tuple<Localizable, MainMenuButton.ClickHandler>> tuples = Arrays.asList(
                Tuple.of(
                        Localizable.of("mainmenu.singleplayer"), button -> {
                            SharedConstants.mc.displayGuiScreen(new GuiSelectWorld(MainMenu.this));
                        }
                ),
                Tuple.of(
                        Localizable.of("mainmenu.multiplayer"), button -> {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                                SharedConstants.mc.displayGuiScreen(new GuiMultiplayer(MainMenu.this));
                            else
                                SharedConstants.mc.displayGuiScreen(new ZephyrMultiPlayerUI(MainMenu.this));
                        }
                ),
                Tuple.of(
                        Localizable.of("mainmenu.altmanager"), button -> {
                            if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                                SharedConstants.mc.displayGuiScreen(new GuiAltManager(this));
                            } else {
                                SharedConstants.mc.displayGuiScreen(AltScreen.getInstance());
                            }
                        }
                ),
                Tuple.of(
                        Localizable.of("mainmenu.settings"), button -> {
                            SharedConstants.mc.displayGuiScreen(new GuiOptions(MainMenu.this, SharedConstants.mc.gameSettings));
                        }
                ),
                Tuple.of(
                        Localizable.of("mainmenu.quit"), button -> {
                            SharedConstants.mc.shutdown();
                        }
                )
        );

        for (Tuple<Localizable, MainMenuButton.ClickHandler> tuple : tuples) {
            this.buttons.add(new MainMenuButton(tuple.getFirst(), tuple.getSecond()));
        }

        sdfDay = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        sdfTime = new SimpleDateFormat("HH:mm:ss");
    }

    SimpleDateFormat sdfDay;
    SimpleDateFormat sdfTime;

    Location bgLocation = Location.of(Phosphate.NAME + "/textures/custombg.png");

    Runnable r = null;

    @Override
    public void onGuiClosed() {

    }

    public void renderBackground() {
        this.panorama.render(1.0F);
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {

        if (!FontManager.loaded) {
            FontManager.loadFonts();
        }

        if (r != null) {
            r.run();
            r = null;
        }

        this.renderBackground();

        Rect.draw(0, 0, this.getWidth(), this.getHeight(), this.hexColor(23, 23, 23, 46), Rect.RectType.EXPAND);

        if (LoadingRenderer.loadingScreenRenderer instanceof NormalLoadingScreen) {
            Rect.draw(0, 0, this.getWidth(), this.getHeight(), this.hexColor(23, 23, 23, (int) (255 - this.maskAlphaAnim.run(255))), Rect.RectType.EXPAND);
        }

        this.renderTexts();

        int alpha = (int) this.alphaAnimation.getValue();

        Date date = new Date();
        CFontRenderer bold = FontManager.pf60;
        bold.drawString(sdfDay.format(date), 8, 4, hexColor(233, 233, 233, alpha));
        FontManager.pf40.drawString(sdfTime.format(date), 8, 4 + bold.getHeight(), hexColor(233, 233, 233, alpha));

        if (!PRE_SHADER.isEmpty())
            PRE_SHADER.forEach(Runnable::run);

        if (!BLUR.isEmpty())
            Shaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, BLUR);

        if (!BLOOM.isEmpty())
            Shaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, BLOOM);

        SharedRenderingConstants.clearRunnables();

        if (this.logoScaleAnimation.getProgress() >= 0.5)
            this.alphaAnimation.run(255);

        if (shouldDisplayJVMVersionWarning) {
            String warning = "警告";
            CFontRenderer pf60 = FontManager.pf60;
            pf60.drawString(warning, this.getWidth() - pf60.getStringWidth(warning) - 4, 4, hexColor(255, 0, 0));

            List<String> list = Arrays.asList(
                    "您的Java运行时修订版本过低",
                    "可能会导致字体偏移",
                    "请到群公告查看解决方案"
            );

            double offsetY = 12 + pf60.getHeight();

            for (String s : list) {
                CFontRenderer fr = FontManager.pf25bold;
                fr.drawString(s, this.getWidth() - fr.getStringWidth(s) - 4, offsetY, -1);
                offsetY += fr.getHeight() + 4;
            }

        }

        this.renderIntroAndLogo();

        if (LoadingRenderer.loadingScreenRenderer instanceof GenshinImpactLoadingScreen) {
            Rect.draw(0, 0, this.getWidth(), this.getHeight(), this.hexColor(255, 255, 255, (int) (255 - this.maskAlphaAnim.run(255))), Rect.RectType.EXPAND);
        }

        this.renderButtons(mouseX, mouseY);
    }

    @Override
    public void renderLast(double mouseX, double mouseY) {
        this.dragOverlayAlpha = Interpolations.interpBezier(this.dragOverlayAlpha, DropTargetHandler.getInstance().isDragging() ? 1.0f : 0.0f, 0.15f);

        if (dragOverlayAlpha > 0.02f) {
            this.renderDragOverlay();
        }
    }

    Localizable lDragOverHere = Localizable.of("mainmenu.dragoverhere");

    private void renderDragOverlay() {
        Rect.draw(0, 0, this.getWidth(), this.getHeight(), this.hexColor(0, 0, 0, (int) (160 * this.dragOverlayAlpha)), Rect.RectType.EXPAND);

        CFontRenderer fr = FontManager.pf100;
        CFontRenderer frSmall = FontManager.pf40;

        fr.drawCenteredString(lDragOverHere.get(), this.getWidth() * 0.5, this.getHeight() * 0.5 - fr.getHeight() * 1.1, this.hexColor(255, 255, 255, (int) (255 * this.dragOverlayAlpha)));
        frSmall.drawCenteredString("(.jpg, .png, .mp4)", this.getWidth() * 0.5, this.getHeight() * 0.5 + frSmall.getHeight() * 1.1, this.hexColor(255, 255, 255, (int) (255 * this.dragOverlayAlpha)));
    }

    Framebuffer fbButtons, fbStencil;

    Timer t = new Timer();
    float theta = 0.0f;

    private void setupProjectionTransformation() {
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        Project.gluPerspective(45.0f, (float) (RenderSystem.getWidth() / RenderSystem.getHeight()), 1.0F, 3000.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(RenderSystem.getWidth() * -0.5, RenderSystem.getHeight() * -0.5, -550.0F);
    }

    private void renderButtons(double mouseX, double mouseY) {

//        this.setupProjectionTransformation();

        GlStateManager.disableCull();
        GlStateManager.pushMatrix();

        if (t.isDelayed(10)) {
            t.reset();
            theta += 5f;
        }

//        GlStateManager.translate(RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5, 0);
//
//        GlStateManager.scale(1, -1, 1);
//
//        GlStateManager.rotate(theta, 0, 1, 0);
//
//        GlStateManager.translate(-RenderSystem.getWidth() * 0.5, -RenderSystem.getHeight() * 0.5, 0);

        double width = 140, height = 20;

        double spacing = 6;

        double x = this.getWidth() * 0.5 - width * 0.5;

        double offsetY = this.getHeight() / 1.8 - (2 * (height + spacing) + height / 2.0);

        double finalOffsetY = offsetY;

//        BLUR.add(() -> {
//            this.roundedRect(x, finalOffsetY - spacing * 0.5, width, (height + spacing) * this.buttons.size(), 8, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, (int) ((this.alphaAnimation.getValue() / 255.0) * 200)));
//        });

        this.roundedRect(x, finalOffsetY - spacing * 0.5, width, (height + spacing) * this.buttons.size(), 8, new Color(0, 0, 0, (int) ((this.alphaAnimation.getValue() / 255.0) * 80)));

        fbButtons = RenderSystem.createFrameBuffer(fbButtons);
        fbStencil = RenderSystem.createFrameBuffer(fbStencil);

        fbButtons.bindFramebuffer(true);
        fbButtons.framebufferClearNoBinding();

        for (MainMenuButton button : this.buttons) {

            if (button.hoveredAlpha > 0.02f) {
                Rect.draw(x, offsetY - 3, width, height + 6, hexColor(255, 255, 255, (int) (button.hoveredAlpha * 255)), Rect.RectType.EXPAND);
            }

            offsetY += height + spacing;
        }

        fbStencil.bindFramebuffer(true);
        fbStencil.framebufferClearNoBinding();

        this.roundedRect(x, finalOffsetY - spacing * 0.5, width, (height + spacing) * this.buttons.size(), 8, Color.WHITE);

        mc.getFramebuffer().bindFramebuffer(true);
        StencilShader.render(fbButtons.framebufferTexture, fbStencil.framebufferTexture);

        offsetY = this.getHeight() / 1.8 - (2 * (height + spacing) + height / 2.0);

        for (MainMenuButton button : this.buttons) {
            CFontRenderer fr = FontManager.pf20bold;

            boolean hovered = isHovered(mouseX, mouseY, x, offsetY, width, height);

            button.hoveredAlpha = Interpolations.interpBezier(button.hoveredAlpha, hovered ? 0.4f : 0.0f, 0.2f);

            fr.drawCenteredString(button.getLabel().get(), x + width * 0.5, offsetY + height * 0.5 - fr.getHeight() * 0.5, hexColor(255, 255, 255, (int) ((this.alphaAnimation.getValue() / 255.0) * 200)));

            offsetY += height + spacing;
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

        double spacing = 6;

        double width = 140, height = 20;

        double x = this.getWidth() * 0.5 - width * 0.5;
        double offsetY = this.getHeight() / 1.8 - (2 * (height + spacing) + height / 2.0);

        if (mouseButton == 0) {

            for (MainMenuButton button : this.buttons) {

                boolean hovered = isHovered(mouseX, mouseY, x, offsetY, width, height);

                if (hovered) {
                    button.getHandler().onClick(button);
                    break;
                }

                offsetY += height + spacing;
            }

        }

    }

    private void renderIntroAndLogo() {

        this.matrix(() -> {

            int textureId = SplashGenerator.t_small.getGlTextureId();
            double y = -this.getHeight() / 4.0;

            if ((!this.logoScaleAnimation.isFinished() || !this.logoTranslateAnimation.isFinished()) && LoadingRenderer.loadingScreenRenderer instanceof NormalLoadingScreen) {

                textureId = SplashGenerator.t.getGlTextureId();

                double scale = 1 - logoScaleAnimation.run(0.4);

                double run = logoTranslateAnimation.run(-this.getHeight() / 4.0);

                this.scaleAtPos(this.getWidth() / 2.0, this.getHeight() / 2.0 + run, scale);

                y = run;
            }

            int width = SplashGenerator.logo.getWidth() / 2;
            int height = SplashGenerator.logo.getHeight() / 2;
            Image.draw(textureId, (this.getWidth() - width) / 2.0, (this.getHeight() - height) / 2.0 + y, width, height, Image.Type.Normal);

        });

    }

    private void renderTexts() {

        List<String> textsMid = Collections.singletonList(
                String.format(
                        "%s %s %s",
                        Phosphate.NAME,
                        EnumChatFormatting.GOLD + (Phosphate.getVersion().toString().equals(" Release") ? "" : Phosphate.getVersion().toString()),
                        EnumChatFormatting.RESET
                )/*,
                String.format(
                        "%sInternal Testing %s& %sProof of Concept %sONLY.",
                        EnumChatFormatting.GREEN,
                        EnumChatFormatting.RESET,
                        EnumChatFormatting.GOLD,
                        EnumChatFormatting.RED

                ),
                EnumChatFormatting.GOLD + "保! 证! 不! 卖!"*/
        );

        List<String> textsLeft = Arrays.asList(
                "特别感谢: Eplor",
                "BiliBili: " + EnumChatFormatting.GREEN + "@IzumiiKonata"
        );

        List<String> textsRight = Collections.singletonList(
                Localizer.getInstance().translate("mainmenu.copyright")
        );

        CFontRenderer fr = FontManager.pf20;

        int alpha = (int) this.alphaAnimation.getValue();

        double offsetY = this.getHeight() - fr.getHeight();
        for (String text : textsMid) {
            FontManager.baloo18.drawCenteredString(
                    text,
                    this.getWidth() / 2.0,
                    offsetY,
//                1,
                    this.hexColor(233, 233, 233, alpha)
//                this.hexColor(64, 64, 64, alpha)
            );

            offsetY -= FontManager.baloo18.getHeight() + 2;
        }

        offsetY = this.getHeight() - fr.getHeight() - 2;
        for (String text : textsLeft) {
            fr.drawString(
                    text,
                    2,
                    offsetY,
//                1,
                    this.hexColor(233, 233, 233, alpha)
//                this.hexColor(64, 64, 64, alpha)
            );

            offsetY -= fr.getHeight() + 2;
        }

        offsetY = this.getHeight() - fr.getHeight() - 2;
        for (String text : textsRight) {
            fr.drawString(
                    text,
                    this.getWidth() - fr.getStringWidth(text) - 2,
                    offsetY,
//                1,
                    this.hexColor(233, 233, 233, alpha)
//                this.hexColor(64, 64, 64, alpha)
            );

            offsetY -= fr.getHeight() + 2;
        }

    }

}
