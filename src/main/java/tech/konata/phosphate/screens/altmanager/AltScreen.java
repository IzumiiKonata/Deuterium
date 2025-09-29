package tech.konata.phosphate.screens.altmanager;

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
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.utils.alt.Alt;
import tech.konata.phosphate.utils.alt.AltManager;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.TitleBar;
import tech.konata.phosphate.rendering.TransitionAnimation;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.fake.FakeEntityPlayerSP;
import tech.konata.phosphate.rendering.fake.FakeNetHandlerPlayClient;
import tech.konata.phosphate.rendering.fake.FakeWorld;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.ShaderUtil;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.screens.BaseScreen;
import tech.konata.phosphate.screens.MainMenu;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextEntity;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.ContextMenu;
import tech.konata.phosphate.screens.clickgui.panels.musicpanel.entities.ContextLabel;
import tech.konata.phosphate.screens.dialog.impl.DialogAddAccount;
import tech.konata.phosphate.screens.dialog.impl.DialogMicrosoftLoginProgress;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.oauth.OAuth;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.*;

public class AltScreen extends BaseScreen {

    @Getter
    private static final AltScreen instance = new AltScreen();

    Localizable title = Localizable.of("altscreen.name");
    Localizable addAlt = Localizable.of("altscreen.addAlt.name");

    WorldClient world;
    EntityPlayerSP player;

    Map<Alt, Object> infos = new HashMap<>();

    double spacing = 4;

    ChangeCapeComponent component = null;

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
                this.player = new FakeEntityPlayerSP(Minecraft.getMinecraft(), this.world, netHandler, null);
                int ModelParts = 0;
                for (EnumPlayerModelParts enumplayermodelparts : Minecraft.getMinecraft().gameSettings.getModelParts()) {
                    ModelParts |= enumplayermodelparts.getPartMask();
                }
                this.player.getDataWatcher().updateObject(10, (Object) (byte) ModelParts);
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

    final Timer avoidDWheelTimer = new Timer();

    @Override
    public void initGui() {
        infos.clear();
        avoidDWheelTimer.reset();

        this.initFakePlayer();
    }

    Framebuffer playerPreviewFb = new Framebuffer(0, 0, true);

    public String status = "";
    float statusAlpha = 0.0f;
    Timer statusAlphaFadeTimer = new Timer();

    public ContextMenu rightClickMenu = null;

    @Override
    public void onGuiClosed() {
        Minecraft.getMinecraft().thePlayer = null;
        Minecraft.getMinecraft().theWorld = null;
    }

    @Override
    public void drawScreen(double mouseX, double mouseY) {

//        Rect.draw(0, 0, this.getWidth(), this.getHeight(), ThemeManager.get(ThemeManager.ThemeColor.Surface), Rect.RectType.EXPAND);

//        RenderSystem.resetColor();

        MainMenu.getInstance().renderBackground();

        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(ShaderRenderType.OVERLAY, Collections.singletonList(() -> {
            Rect.draw(0, 0, this.getWidth(), this.getHeight(), -1, Rect.RectType.EXPAND);
        }));

        getFrTitle().drawString(title.get(), 16, 9 + TitleBar.getTitlebarHeight(), hexColor(233, 233, 233));

        if (statusAlpha > 0.02f) {
            getFrTitle().drawCenteredString(status, this.getWidth() * 0.5, spacing * 1.5 + TitleBar.getTitlebarHeight(), ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (statusAlpha * 255)));
        }

        boolean bDelayed = statusAlphaFadeTimer.isDelayed(4000);

        statusAlpha = Interpolations.interpBezier(statusAlpha, bDelayed ? 0.0f : 1.0f, bDelayed ? 0.2f : 0.1f);

        getFrTitle().drawString(addAlt.get(), this.getWidth() - 16 - getFrTitle().getStringWidth(addAlt.get()), 9 + TitleBar.getTitlebarHeight(), ThemeManager.getHexAccentColor());

        this.renderAlts(16, 9 + getFrTitle().getHeight() + spacing, mouseX, mouseY, Mouse.getDWheel2());

        this.renderPlayerPreview(mouseX, mouseY);

        GlStateManager.pushMatrix();
        Shaders.POST_BLOOM_SHADER.update();
        Shaders.POST_BLOOM_SHADER.runNoCaching(ShaderRenderType.OVERLAY, BLOOM);

        SharedRenderingConstants.clearRunnables();
        GlStateManager.popMatrix();

        if (this.rightClickMenu != null) {
            if (this.rightClickMenu.shouldClose)
                this.rightClickMenu = null;
            else
                this.rightClickMenu.render(mouseX, mouseY);
        }

        if (component != null) {
            component.render(mouseX, mouseY);

            if (component.scaleAnimation.isFinished() && component.closing)
                component = null;
        }

        // IDK why but without this line of code the font will be glitched out :(
        // maybe I just need to learn more about ogl...
//        Rect.draw(0, 0, 1, 1, 0, Rect.RectType.EXPAND);

//        Image.draw(lSteve, 100, 100, 100, 100, Image.Type.Normal);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (mouseButton == 0) {
            if (isHovered(mouseX, mouseY, this.getWidth() - 16 - getFrTitle().getStringWidth(addAlt.get()), 9 + TitleBar.getTitlebarHeight(), getFrTitle().getStringWidth(addAlt.get()), getFrTitle().getHeight()) && !Mouse.isButtonDown(1) && this.dialog == null) {
                this.setDialog(new DialogAddAccount());
            }
        }

        double posX = 16 + spacing;
        double posY = 9 + getFrTitle().getHeight() + spacing + spacing - scroll + TitleBar.getTitlebarHeight();
        double xSplit = this.getWidth() * 3 / 4 - 14;
        double panelHeight = this.getHeight() - 9 - spacing - 16 - getFrTitle().getHeight() - TitleBar.getTitlebarHeight();


        double altWidth = xSplit - spacing - 24, altHeight = 60;

        synchronized (AltManager.getAlts()) {
            for (Alt alt : AltManager.getAlts()) {

                if (posY + altHeight < spacing + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight()) {
                    posY += spacing + altHeight;
                    continue;
                }

                if (posY > spacing + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight() + panelHeight) {
                    break;
                }

                if (isHovered(mouseX, mouseY, posX, posY, altWidth, altHeight)) {

                    if (mouseButton == 0) {
                        if (this.rightClickMenu == null && this.dialog == null && this.component == null) {
                            this.logIn(alt);
                        }
                    }

                    if (this.rightClickMenu == null && mouseButton == 1 && component == null && dialog == null) {
                        this.rightClickMenu = this.buildMenu(mouseX, mouseY, alt);
                    }
                }

                posY += spacing + altHeight;
            }
        }

    }

    private void renderPlayerPreview(double mouseX, double mouseY) {

        double xSplit = this.getWidth() * 3 / 4 - 14;

            roundedRect(xSplit + spacing, 9 + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight(), this.getWidth() - xSplit - spacing - 16, this.getHeight() - 9 - spacing - 16 - getFrTitle().getHeight() - TitleBar.getTitlebarHeight(), 6, new Color(0, 0, 0, 80));

        playerPreviewFb = RenderSystem.createFrameBuffer(playerPreviewFb);
        playerPreviewFb.bindFramebuffer(true);
        playerPreviewFb.framebufferClearNoBinding();
        GlStateManager.pushMatrix();
        this.drawPlayer(mouseX, mouseY);
        GlStateManager.popMatrix();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.bindTexture(playerPreviewFb.framebufferTexture);
        ShaderUtil.drawQuads();

        BLOOM.add(() -> {
            GlStateManager.bindTexture(playerPreviewFb.framebufferTexture);
            GlStateManager.color(0, 0, 0, 1);
            ShaderUtil.drawQuads();
        });

        double panelWidth = this.getWidth() - xSplit - spacing - 16;
        double panelHeight = this.getHeight() - 9 - spacing - 16 - getFrTitle().getHeight() - TitleBar.getTitlebarHeight();

        FontManager.pf40.drawCenteredString(Minecraft.getMinecraft().getSession().getUsername(), xSplit + spacing + panelWidth * 0.5, spacing + getFrTitle().getHeight() + spacing + panelHeight * 3 / 4, hexColor(233, 233, 233));

    }

    final List<String> likeList = Arrays.asList(
            "Loyisa",
            "a6513375",
            "mckuhei",
            "trytodupe",
            "Eplor",
            "Real_Eplor",
            "cubk",
            "Nplus",
            "Margele"
    );

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
            double xSplit = this.getWidth() * 3 / 4 - 14;
            double panelWidth = this.getWidth() - xSplit - spacing - 28;

            entRotYaw = (/*(int) */entRotYaw % 360);
            entRotYaw += (float) RenderSystem.getFrameDeltaTime() * 0.75f;
            drawEntityOnScreen(xSplit + spacing + panelWidth * 0.5, spacing + getFrTitle().getHeight() + spacing + this.getHeight() * 0.6, targetHeight, (float) mouseX, (float) mouseY, this.player);
            Minecraft.getMinecraft().thePlayer = null;
            Minecraft.getMinecraft().theWorld = null;
        }
    }

    double scroll = 0, ySmooth = 0;

    private void renderAlts(double posX, double posY, double mouseX, double mouseY, int dWheel) {

        if (avoidDWheelTimer.isDelayed(100)) {
            double yAdd = 8;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                yAdd *= 2;

            if (dWheel > 0)
                ySmooth -= yAdd;
            else if (dWheel < 0)
                ySmooth += yAdd;
        }

        double xSplit = this.getWidth() * 3 / 4 - 14;

        double panelHeight = this.getHeight() - 9 - spacing - 16 - getFrTitle().getHeight() - TitleBar.getTitlebarHeight();

        roundedRect(16, 9 + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight(), xSplit - spacing - 16, panelHeight, 8, new Color(0, 0, 0, 80));

        ySmooth = Interpolations.interpBezier(ySmooth, 0, 0.1f);
        scroll = Interpolations.interpBezier(scroll, scroll + ySmooth, 0.6f);

        if (scroll < 0)
            scroll = Interpolations.interpBezier(scroll, 0, 0.6f);

        double altWidth = xSplit - spacing - 24, altHeight = 60;

        CFontRenderer frBig = FontManager.pf25bold;
        CFontRenderer frSmall = FontManager.pf14;

        posX += spacing;
        posY += spacing - scroll + TitleBar.getTitlebarHeight();

        Stencil.write();
        roundedRect(16, 9 + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight(), xSplit - spacing, panelHeight, 8, -4, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));
        Stencil.erase();

        synchronized (AltManager.getAlts()) {

            if (scroll > (AltManager.getAlts().size() - 1) * (spacing + altHeight))
                scroll = Interpolations.interpBezier(scroll, (AltManager.getAlts().size() - 1) * (spacing + altHeight), 0.6f);

            AltManager.alts.removeIf(alt -> alt.getUsername() == null);

            for (Alt alt : AltManager.getAlts()) {

                if (posY + altHeight < spacing + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight()) {
                    posY += spacing + altHeight;
                    continue;
                }

                if (posY > spacing + getFrTitle().getHeight() + spacing + TitleBar.getTitlebarHeight() + panelHeight) {
                    break;
                }

                roundedRect(posX, posY, altWidth, altHeight, 6, new Color(0, 0, 0, 100));

                if (isHovered(mouseX, mouseY, posX, posY, altWidth, altHeight)) {
                    alt.hoveredAlpha = Interpolations.interpBezier(alt.hoveredAlpha, 0.2f, 0.2f);
                } else {
                    alt.hoveredAlpha = Interpolations.interpBezier(alt.hoveredAlpha, 0f, 0.2f);
                }

                Location location = alt.getSkinLocation();

                double imgSize = altHeight - spacing * 2;

                int textColor = hexColor(222, 222, 222);

                frBig.drawString(alt.getUsername(), posX + altHeight, posY + spacing * 2, textColor);
                frSmall.drawString(this.getAltDescription(alt), posX + altHeight, posY + spacing * 3.5 + frBig.getHeight(), hexColor(222, 222, 222, 222));

                if (location != null && Minecraft.getMinecraft().getTextureManager().getTexture(location) != null || !alt.isMicrosoft()) {
                    Minecraft.getMinecraft().getTextureManager().bindTexture(location);
                    roundedRectTextured(posX + spacing, posY + spacing, imgSize, imgSize, 8);
                } else {
                    roundedRect(posX + spacing, posY + spacing, imgSize, imgSize, 8, Color.GRAY);
                }

                if (alt.hoveredAlpha > 0.02f) {
                    roundedRect(posX, posY, altWidth, altHeight, 6, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, (int) (alt.hoveredAlpha * 255)));
                }

                posY += spacing + altHeight;
            }
        }

        Stencil.dispose();

    }

    Localizable lMicrosoftAccount = Localizable.of("altscreen.microsoftaccount");
    Localizable lExpired = Localizable.of("altscreen.expired");
    Localizable lExpiring = Localizable.of("altscreen.expiring");
    Localizable lOffline = Localizable.of("altscreen.offlineaccount");

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
                        Alt at = new Alt(userName, refreshToken, token, uuid);
                        at.setLastRefreshedTime(System.currentTimeMillis() / 1000L);

                        synchronized (AltManager.getAlts()) {
                            AltManager.getAlts().set(AltManager.getAlts().indexOf(alt), at);
                        }

                        Session session = new Session(userName, uuid, token, "mojang");
                        Minecraft.getMinecraft().setSession(session);

                        Minecraft.getMinecraft().addScheduledTask(AltScreen.this::initFakePlayer);
                        AltScreen.this.status = EnumChatFormatting.DARK_GREEN + lLoggedIn.get() + " (" + alt.getUsername() + ")";
                        AltScreen.this.statusAlphaFadeTimer.reset();
                        dialog.close();
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
            }

        } else {
            Session session = new Session(alt.getUsername(), "", "", "mojang");
            Minecraft.getMinecraft().setSession(session);

            this.initFakePlayer();

            this.status = EnumChatFormatting.DARK_GREEN + lLoggedIn.get() + " (" + alt.getUsername() + ")";
            this.statusAlphaFadeTimer.reset();
        }

    }

    Localizable lLoggingIn = Localizable.of("altscreen.loggingin");
    Localizable lLoggedIn = Localizable.of("altscreen.loggedin");
    Localizable lFailed = Localizable.of("altscreen.failed");
    Localizable lDelete = Localizable.of("altscreen.delete");
    Localizable lChangeCape = Localizable.of("altscreen.changecape");

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

        if (component != null) {
            this.component.keyTyped(typedChar, keyCode);
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            TransitionAnimation.task(() -> Minecraft.getMinecraft().displayGuiScreen0(MainMenu.getInstance()));
        }

    }

    float entRotYaw = 0.0f;

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
        //GlStateManager.pushMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.translate(posX, posY, 50.0f);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float f2 = ent.renderYawOffset;
        final float f3 = ent.rotationYaw;
        final float f4 = ent.rotationPitch;
        final float f5 = ent.prevRotationYawHead;
        final float f6 = ent.rotationYawHead;
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(150.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        ent.renderYawOffset = 0;
        ent.rotationYaw = 0;
        ent.rotationYawHead = 0;
//        ent.prevRotationYawHead = 0;
//        ent.prevRenderYawOffset = 0;
        ent.prevRotationPitch = ent.rotationPitch;
//        ent.prevRotationYawHead = ent.rotationYaw;

        ent.renderYawOffset = ent.rotationYaw = ent.rotationYawHead = entRotYaw;
//        ent.prevCameraYaw = ent.cameraYaw = 0.0F;
//        ent.prevDistanceWalkedModified = ent.distanceWalkedModified = 0.0F;
//        ent.prevChasingPosX = ent.chasingPosX = ent.prevChasingPosY = ent.chasingPosY = ent.prevChasingPosZ = ent.chasingPosZ = 0.0F;

        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        //try {
        final RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0f);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0, 0.0, 0.0, 0.0f, Minecraft.getMinecraft().timer.renderPartialTicks);
        rendermanager.setRenderShadow(true);
        //}
        // finally {
        ent.renderYawOffset = f2;
        ent.rotationYaw = f3;
        ent.rotationPitch = f4;
        ent.prevRotationYawHead = ent.prevRenderYawOffset = entRotYaw;
        ent.rotationYawHead = f6;
        // GlStateManager.popMatrix();
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
        //}
    }


    public CFontRenderer getFrTitle() {
        return FontManager.pf40;
    }
}
