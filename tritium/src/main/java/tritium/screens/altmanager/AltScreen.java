package tritium.screens.altmanager;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tritium.rendering.shader.ShaderProgram;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.ImageWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.contextmenu.ContextEntity;
import tritium.screens.contextmenu.ContextMenu;
import tritium.screens.contextmenu.entities.ContextLabel;
import tritium.screens.dialog.DialogMicrosoftLoginProgress;
import tritium.settings.ClientSettings;
import tritium.utils.alt.Alt;
import tritium.utils.alt.AltManager;
import tritium.utils.i18n.Localizable;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.TransitionAnimation;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.fake.FakeNetHandlerPlayClient;
import tritium.rendering.fake.FakeWorld;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.screens.BaseScreen;
import tritium.screens.MainMenu;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.oauth.OAuth;
import tritium.utils.timing.Timer;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.*;

public class AltScreen extends BaseScreen {

    @Getter
    private static final AltScreen instance = new AltScreen();

    private final Localizable lMicrosoftAccount = Localizable.of("altscreen.microsoftaccount");
    private final Localizable lExpired = Localizable.of("altscreen.expired");
    private final Localizable lExpiring = Localizable.of("altscreen.expiring");
    private final Localizable lOffline = Localizable.of("altscreen.offlineaccount");
    private final Localizable lLoggingIn = Localizable.of("altscreen.loggingin");
    private final Localizable lLoggedIn = Localizable.of("altscreen.loggedin");
    private final Localizable lFailed = Localizable.of("altscreen.failed");
    private final Localizable lDelete = Localizable.of("altscreen.delete");
    private final Localizable lChangeCape = Localizable.of("altscreen.changecape");

    public WorldClient world;
    public EntityPlayerSP player;
    private float entRotYaw = 0.0f;

    // UI rendering
    private final double spacing = 4;
    private ChangeCapeComponent component = null;
    private Framebuffer playerPreviewFb = new Framebuffer(0, 0, true);

    // Status and UI state
    private String status = "";
    private float statusAlpha = 0.0f;
    private final Timer statusAlphaFadeTimer = new Timer();
    private ContextMenu rightClickMenu = null;

    // lmao wtf
    private final List<String> likeList = Arrays.asList(
            "Loyisa",
            "a6513375",
            "mckuhei",
            "trytodupe",
            "Eplor",
            "Real_Eplor",
            "cubk"
    );

    public void initFakePlayer() {
        MultiThreadingUtil.runAsync(() -> {
            try {
                Minecraft.getMinecraft().profileProperties.clear();
                Minecraft.getMinecraft().sessionService = (new YggdrasilAuthenticationService(Minecraft.getMinecraft().config.userInfo.proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
                this.player = null;
                this.world = null;
                final WorldSettings worldSettings = new WorldSettings(0L, WorldSettings.GameType.NOT_SET, true, false, WorldType.DEFAULT);
                final FakeNetHandlerPlayClient netHandler = new FakeNetHandlerPlayClient(Minecraft.getMinecraft());
                this.world = new FakeWorld(worldSettings, netHandler);
                this.player = new EntityPlayerSP(Minecraft.getMinecraft(), this.world, netHandler);
                int modelParts = 0;
                for (EnumPlayerModelParts enumplayermodelparts : Minecraft.getMinecraft().gameSettings.getModelParts()) {
                    modelParts |= enumplayermodelparts.getPartMask();
                }
                this.player.getDataWatcher().updateObject(10, (Object) (byte) modelParts);
                this.player.dimension = 0;
                this.player.movementInput = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
                Minecraft.getMinecraft().getRenderManager().cacheActiveRenderInfo(this.world, Minecraft.getMinecraft().fontRendererObj, this.player, this.player, Minecraft.getMinecraft().gameSettings, 0.0f);
            } catch (Throwable e) {
                e.printStackTrace();
                this.player = null;
                this.world = null;
            }
        });
    }

    @Override
    public void initGui() {
        this.component = null;
        this.initFakePlayer();
        this.layoutAlts();
        this.layoutButton();
    }

    @Override
    public void onGuiClosed() {
        Minecraft.getMinecraft().thePlayer = null;
        Minecraft.getMinecraft().theWorld = null;
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {
        int dWheel = Mouse.getDWheel();

        renderBackground();
        renderStatus();
        renderAddAltButton(mouseX, mouseY);
        renderAltsPanel(mouseX, mouseY, dWheel);
        renderPlayerPreview(mouseX, mouseY);
        renderCapesPanel(mouseX, mouseY, dWheel);
        renderContextMenu(mouseX, mouseY);
    }

    private void renderBackground() {
        Rect.draw(0, 0, this.getWidth(), this.getHeight(), this.getColor(ColorType.BACKGROUND), Rect.RectType.EXPAND);
    }

    private void renderStatus() {
        if (statusAlpha > 0.02f) {
            getFrTitle().drawCenteredString(status, this.getWidth() * 0.5, spacing * 1.5, ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (statusAlpha * 255)));
        }
        boolean bDelayed = statusAlphaFadeTimer.isDelayed(4000);
        statusAlpha = Interpolations.interpolate(statusAlpha, bDelayed ? 0.0f : 1.0f, bDelayed ? 0.2f : 0.1f);
    }

    RectWidget buttonWidget = new RectWidget();
    private void layoutButton() {
        buttonWidget.setBeforeRenderCallback(() -> {
            buttonWidget.setColor(this.getColor(ColorType.CONTAINER_BACKGROUND));
            buttonWidget.setBounds(12, 12);
            buttonWidget.setPosition(RenderSystem.getWidth() - this.getScreenPadding() * .25 - buttonWidget.getWidth(), this.getScreenPadding() * .25);
        });

        buttonWidget.setOnClickCallback((x, y, i) -> {

            if (i == 0) {
                DialogMicrosoftLoginProgress dialog = new DialogMicrosoftLoginProgress();
                this.setDialog(dialog);
                OAuth oAuth = new OAuth();

                oAuth.logIn(new OAuth.LoginCallback() {
                    @Override
                    public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                        Alt alt = new Alt(userName, refreshToken, token, uuid);
                        alt.setLastRefreshedTime(System.currentTimeMillis() / 1000L);
                        synchronized (AltManager.getAlts()) {
                            AltManager.getAlts().add(alt);
                        }

                        dialog.close();
                    }

                    final Localizable lFailed = Localizable.of("altscreen.failed");

                    @Override
                    public void onFailed(Exception e) {
                        e.printStackTrace();
                        dialog.setLabel(Localizable.ofUntranslatable(lFailed.get() + "\n" + e.getMessage()));
                        AltScreen.getInstance().status = EnumChatFormatting.RED + lFailed.get();
                    }

                    @Override
                    public void setStatus(String status) {
                        dialog.setLabel(Localizable.of(status));
                    }
                });
            }

            return true;
        });
    }

    private void renderAddAltButton(double mouseX, double mouseY) {
        Rect.draw(this.buttonWidget.getX() - 1, this.buttonWidget.getY() - 1, this.buttonWidget.getWidth() + 2, this.buttonWidget.getHeight() + 2, this.getColor(ColorType.CONTAINER_OUTLINE));
        this.buttonWidget.renderWidget(mouseX, mouseY, 0);

        CFontRenderer fr = FontManager.pf20bold;
        fr.drawCenteredString(
                "+",
                this.buttonWidget.getX() + this.buttonWidget.getWidth() * 0.5,
                // 何意味 为什么要 -1 才能对齐
                this.buttonWidget.getY() + this.buttonWidget.getHeight() * 0.5 - fr.getHeight() * .5 - 1,
                this.getColor(ColorType.PRIMARY_TEXT)
        );
    }

    private void renderAltsPanel(double mouseX, double mouseY, int dWheel) {
        this.renderAlts(mouseX, mouseY, dWheel);
    }

    private void renderCapesPanel(double mouseX, double mouseY, int dWheel) {
        Rect.draw(this.baseWidget.getX() + (this.getPanelWidth() + this.getScreenPadding()) * 2 - 1, this.baseWidget.getY() - 1, this.getPanelWidth() * 2 + 2, this.baseWidget.getHeight() + 2, this.getColor(ColorType.CONTAINER_OUTLINE));
        Rect.draw(this.baseWidget.getX() + (this.getPanelWidth() + this.getScreenPadding()) * 2, this.baseWidget.getY(), this.getPanelWidth() * 2, this.baseWidget.getHeight(), this.getColor(ColorType.CONTAINER_BACKGROUND));

        if (component != null) {
            component.render(mouseX, mouseY, this.baseWidget.getX() + (this.getPanelWidth() + this.getScreenPadding()) * 2, this.baseWidget.getY(), this.getPanelWidth() * 2, this.baseWidget.getHeight(), dWheel);
        }
    }

    private void renderContextMenu(double mouseX, double mouseY) {
        if (this.rightClickMenu != null) {
            if (this.rightClickMenu.shouldClose)
                this.rightClickMenu = null;
            else
                this.rightClickMenu.render(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.baseWidget.onMouseClickReceived(mouseX, mouseY, mouseButton);
        this.buttonWidget.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

    private void renderPlayerPreview(double mouseX, double mouseY) {
        double width = this.getPanelWidth();

        Rect.draw(this.baseWidget.getX() + this.getPanelWidth() + this.getScreenPadding() - 1, this.baseWidget.getY() - 1, this.getPanelWidth() + 2, this.baseWidget.getHeight() + 2, this.getColor(ColorType.CONTAINER_OUTLINE));
        Rect.draw(this.baseWidget.getX() + this.getPanelWidth() + this.getScreenPadding(), this.baseWidget.getY(), this.getPanelWidth(), this.baseWidget.getHeight(), this.getColor(ColorType.CONTAINER_BACKGROUND));

        playerPreviewFb = RenderSystem.createFrameBuffer(playerPreviewFb);
        playerPreviewFb.bindFramebuffer(true);
        playerPreviewFb.framebufferClearNoBinding();

        GlStateManager.enableTexture2D();

        GlStateManager.pushMatrix();
        this.drawPlayer(mouseX, mouseY);
        GlStateManager.popMatrix();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(playerPreviewFb.framebufferTexture);
        ShaderProgram.drawQuadFlipped();

        double panelHeight = this.getHeight() - 9 - spacing - getScreenPadding() - getFrTitle().getHeight();
        FontManager.arial40bold.drawCenteredString(Minecraft.getMinecraft().getSession().getUsername(), this.baseWidget.getX() + this.getPanelWidth() + this.getScreenPadding() + width * 0.5, this.baseWidget.getY() + panelHeight * 3 / 4, this.getColor(ColorType.PRIMARY_TEXT));
    }

    private void drawPlayer(double mouseX, double mouseY) {
        if (this.player == null || this.world == null || this.player.worldObj == null) {
            return;
        }
        if (Minecraft.getMinecraft().getRenderManager().worldObj == null || Minecraft.getMinecraft().getRenderManager().livingPlayer == null) {
            Minecraft.getMinecraft().getRenderManager().cacheActiveRenderInfo(this.world, Minecraft.getMinecraft().fontRendererObj, this.player, this.player, Minecraft.getMinecraft().gameSettings, 0.0f);
        }
        if (this.world != null && this.player != null) {
            Minecraft.getMinecraft().thePlayer = this.player;
            Minecraft.getMinecraft().theWorld = this.world;
            final float targetHeight = 90;
            double width = this.getPanelWidth();

            entRotYaw = (/*(int) */entRotYaw % 360);
            entRotYaw += (float) RenderSystem.getFrameDeltaTime() * 0.75f;
            drawEntityOnScreen(this.baseWidget.getX() + this.getPanelWidth() + this.getScreenPadding() + width * 0.5, this.baseWidget.getY() + spacing + this.getHeight() * 0.6, targetHeight, (float) mouseX, (float) mouseY, this.player);
            Minecraft.getMinecraft().thePlayer = null;
            Minecraft.getMinecraft().theWorld = null;
        }
    }

    RectWidget baseWidget = new RectWidget();
    ScrollPanel altsPanel;
    private void layoutAlts() {

        this.baseWidget.getChildren().clear();

        this.baseWidget.setBeforeRenderCallback(() -> {
            this.baseWidget.setColor(this.getColor(ColorType.CONTAINER_BACKGROUND));
            this.baseWidget.setMargin(this.getScreenPadding());
            this.baseWidget.setWidth(this.getPanelWidth());
        });

        altsPanel = new ScrollPanel();
        this.baseWidget.addChild(altsPanel);

        this.altsPanel.setSpacing(8);

        altsPanel.setBeforeRenderCallback(() -> this.altsPanel.setMargin(8));

        this.refreshAltsPanel();
    }

    private void refreshAltsPanel() {
        this.altsPanel.getChildren().clear();

        synchronized (AltManager.getAlts()) {
            for (Alt alt : AltManager.getAlts()) {

                RectWidget rwOutline = new RectWidget();
                RectWidget rw = new RectWidget();

                rw.setOnClickCallback((x, y, i) -> {

                    if (i == 0) {
                        this.logIn(alt);
                    }

                    return true;
                });

                rwOutline.addChild(rw);

                altsPanel.addChild(rwOutline);

                rw.setBeforeRenderCallback(() -> {
                    rw.setPosition(1, 1);
                    rw.setBounds(altsPanel.getWidth() - 2, 48);
                    rw.setColor(this.getColor(ColorType.CONTAINER_ELEMENT_BACKGROUND));

                    rwOutline.setBounds(altsPanel.getWidth() + 2, rw.getHeight() + 2);
                    rwOutline.setColor(this.getColor(ColorType.CONTAINER_OUTLINE));
                });

                RectWidget rwHoverEffect = new RectWidget();

                rw.addChild(rwHoverEffect);
                rwHoverEffect.setClickable(false);
                rwHoverEffect.setAlpha(0f);
                rwHoverEffect.setBeforeRenderCallback(() -> {
                    rwHoverEffect.setMargin(0);
                    rwHoverEffect.setColor(this.getColor(ColorType.CONTAINER_ELEMENT_HOVERING));
                    rwHoverEffect.setAlpha(Interpolations.interpolate(rwHoverEffect.getAlpha(), rw.isHovering() ? 1f : 0f, 0.2f));
                });

                RectWidget avatarBg = new RectWidget();

                rw.addChild(avatarBg);
                avatarBg.setBeforeRenderCallback(() -> {
                    avatarBg.setMargin(4);
                    avatarBg.setBounds(avatarBg.getHeight(), avatarBg.getHeight());
                    avatarBg.setColor(Color.GRAY);
                });

                avatarBg.setClickable(false);

                ImageWidget imgWidget = new ImageWidget(alt::getSkinLocation, 0, 0, avatarBg.getWidth(), avatarBg.getHeight());

                avatarBg.addChild(imgWidget);
                imgWidget.setClickable(false);
                imgWidget.setBeforeRenderCallback(() -> imgWidget.setMargin(0));

                LabelWidget altName = new LabelWidget(alt::getUsername, FontManager.pf20bold);
                rw.addChild(altName);
                altName.setBeforeRenderCallback(() -> {
                    altName.setPosition(avatarBg.getRelativeX() + avatarBg.getWidth() + 4, avatarBg.getRelativeY());
                    altName.setColor(this.getColor(ColorType.PRIMARY_TEXT));
                });
                altName.setClickable(false);

                LabelWidget altDescription = new LabelWidget(() -> getAltDescription(alt), FontManager.pf16);
                rw.addChild(altDescription);
                altDescription.setBeforeRenderCallback(() -> {
                    altDescription.setPosition(altName.getRelativeX(), avatarBg.getRelativeY() + avatarBg.getHeight() - altDescription.getHeight());
                    altDescription.setColor(this.getColor(ColorType.SECONDARY_TEXT));
                });
                altDescription.setClickable(false);
            }
        }
    }

    private void renderAlts(double mouseX, double mouseY, int dWheel) {
        // outline
        Rect.draw(this.baseWidget.getX() - 1, this.baseWidget.getY() - 1, this.baseWidget.getWidth() + 2, this.baseWidget.getHeight() + 2, this.getColor(ColorType.CONTAINER_OUTLINE));
        this.baseWidget.renderWidget(mouseX, mouseY, dWheel);
    }

    private String getAltDescription(Alt alt) {
        if (alt.isMicrosoft()) {
            Duration duration = Duration.ofSeconds(alt.getLeftExpiringTime());
            return lMicrosoftAccount.get() + "\n" + (alt.isExpired() ? lExpired.get() : lExpiring.get() + " " + duration.toHours() + "h" + (duration.toMinutes() - duration.toHours() * 60) + "m.");
        } else {
            return lOffline.get();
        }
    }

    private void logIn(Alt alt) {
        if (alt.isMicrosoft()) {
            if (alt.isExpired() || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                OAuth oAuth = new OAuth();
                DialogMicrosoftLoginProgress dialog = new DialogMicrosoftLoginProgress();
                this.setDialog(dialog);
                AltScreen.this.status = EnumChatFormatting.YELLOW + lLoggingIn.get();
                AltScreen.this.statusAlphaFadeTimer.reset();
                oAuth.refresh(alt.getRefreshToken(), new OAuth.LoginCallback() {
                    @Override
                    public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                        alt.setLastRefreshedTime(System.currentTimeMillis() / 1000L);

                        alt.username = userName;
                        alt.refreshToken = refreshToken;
                        alt.accessToken = token;
                        alt.userUUID = uuid;

                        Session session = new Session(userName, uuid, token, "mojang");
                        Minecraft.getMinecraft().setSession(session);

                        Minecraft.getMinecraft().addScheduledTask(AltScreen.this::initFakePlayer);
                        AltScreen.this.status = EnumChatFormatting.DARK_GREEN + lLoggedIn.get() + " (" + alt.getUsername() + ")";
                        AltScreen.this.statusAlphaFadeTimer.reset();
                        dialog.close();
                        AltScreen.this.component = new ChangeCapeComponent(alt);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        e.printStackTrace();
                        dialog.setLabel(Localizable.ofUntranslatable(lFailed.get() + "\n" + e.getMessage()));
                        AltScreen.this.status = EnumChatFormatting.RED + lFailed.get();
                    }

                    @Override
                    public void setStatus(String status) {
                        Localizable localizable = Localizable.of(status);
                        dialog.setLabel(localizable);
                        AltScreen.this.status = EnumChatFormatting.YELLOW + lLoggingIn.get() + "(" + localizable.get() + ")";
                        AltScreen.this.statusAlphaFadeTimer.reset();
                    }
                });
            } else {
                Session session = new Session(alt.getUsername(), alt.getUserUUID(), alt.getAccessToken(), "mojang");
                Minecraft.getMinecraft().setSession(session);

                this.initFakePlayer();

                this.status = EnumChatFormatting.DARK_GREEN + lLoggedIn.get() + " (" + alt.getUsername() + ")";
                this.statusAlphaFadeTimer.reset();
                AltScreen.this.component = new ChangeCapeComponent(alt);
            }
        } else {
            Session session = new Session(alt.getUsername(), "", "", "mojang");
            Minecraft.getMinecraft().setSession(session);

            this.initFakePlayer();

            this.status = EnumChatFormatting.DARK_GREEN + lLoggedIn.get() + " (" + alt.getUsername() + ")";
            this.statusAlphaFadeTimer.reset();
        }
    }

    private ContextMenu buildMenu(double mouseX, double mouseY, Alt alt) {
        List<ContextEntity> contexts = new ArrayList<>();

        contexts.add(new ContextLabel(lDelete.get(), () -> {
            AltManager.getAlts().remove(alt);
            this.rightClickMenu = null;
        }));

        if (alt.isMicrosoft() && !alt.isExpired()) {
            contexts.add(new ContextLabel(lChangeCape.get(), () -> {
                component = new ChangeCapeComponent(alt);
                this.rightClickMenu = null;
            }));
        }

        return new ContextMenu(mouseX, mouseY, contexts);
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            TransitionAnimation.task(() -> Minecraft.getMinecraft().displayGuiScreen0(MainMenu.getInstance()));
        }
    }

    @Override
    public void updateScreen() {
        if (this.player != null && this.world != null) {
            this.updateLimb(this.player);
        }
    }

    private void updateLimb(EntityPlayer ent) {
        ent.prevLimbSwingAmount = ent.limbSwingAmount;
        double d2 = 0.05;
        double d3 = 0.05;
        float f7 = MathHelper.sqrt_double(d2 * d2 + d3 * d3) * 4.0F;

        if (f7 > 1.0F) {
            f7 = 1.0F;
        }

        ent.limbSwingAmount += (f7 - ent.limbSwingAmount) * 0.4F;
        ent.limbSwing += ent.limbSwingAmount;
    }

    public void drawEntityOnScreen(final double posX, final double posY, final float scale, final float mouseX, final float mouseY, final EntityPlayer ent) {
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableColorMaterial();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.translate(posX, posY, 50);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float f2 = ent.renderYawOffset;
        final float f3 = ent.rotationYaw;
        final float f4 = ent.rotationPitch;
        final float f6 = ent.rotationYawHead;

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enablePaperDollLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        ent.prevRotationPitch = ent.rotationPitch;
        ent.renderYawOffset = ent.rotationYaw = ent.rotationYawHead = entRotYaw;

        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0f);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0, 0.0, 0.0, 0.0f, Minecraft.getMinecraft().timer.renderPartialTicks);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f2;
        ent.rotationYaw = f3;
        ent.rotationPitch = f4;
        ent.prevRotationYawHead = ent.prevRenderYawOffset = entRotYaw;
        ent.rotationYawHead = f6;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.translate(0.0f, 0.0f, 20.0f);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    private double getScreenPadding() {
        return 24;
    }

    private double getPanelWidth() {
        return (RenderSystem.getWidth() - this.getScreenPadding() * 4) * 0.25;
    }

    public CFontRenderer getFrTitle() {
        return FontManager.pf20;
    }

    public enum ColorType {
        BACKGROUND,
        PRIMARY_TEXT,
        SECONDARY_TEXT,
        CONTAINER_BACKGROUND,
        CONTAINER_OUTLINE,
        CONTAINER_ELEMENT_BACKGROUND,
        CONTAINER_ELEMENT_HOVERING
    }

    public int getColor(ColorType type) {
        ThemeManager.Theme theme = ClientSettings.THEME.getValue();

        return switch (theme) {
            case Light -> switch (type) {
                case BACKGROUND, CONTAINER_ELEMENT_BACKGROUND -> 0xFFEBEBEB;
                case PRIMARY_TEXT -> 0xFF1F2937;
                case SECONDARY_TEXT -> 0xFF6B7280;
                case CONTAINER_BACKGROUND -> 0xFFF6F6F6;
                case CONTAINER_OUTLINE -> 0xFFE5E7EB;
                case CONTAINER_ELEMENT_HOVERING -> 0xFFF3F4F6;
            };
            case Dark -> switch (type) {
                case BACKGROUND, CONTAINER_ELEMENT_BACKGROUND -> 0xFF20202B;
                case PRIMARY_TEXT -> 0xFFF9FAFB;
                case SECONDARY_TEXT -> 0xFF9CA3AF;
                case CONTAINER_BACKGROUND -> 0xFF202020;
                case CONTAINER_OUTLINE -> 0xFF374151;
                case CONTAINER_ELEMENT_HOVERING -> 0xFF374151;
            };
        };

    }
}