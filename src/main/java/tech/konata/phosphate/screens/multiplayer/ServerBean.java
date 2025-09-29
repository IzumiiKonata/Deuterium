package tech.konata.phosphate.screens.multiplayer;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import org.apache.commons.lang3.Validate;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.CheckRenderer;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.SVGImage;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.screens.multiplayer.dialog.dialogs.ServerInfoDialog;
import tech.konata.phosphate.utils.other.StringUtils;
import tech.konata.phosphate.utils.timing.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.time.Duration;

public class ServerBean implements SharedRenderingConstants {

    @Getter
    private final ServerData server;

    public float shadowAlpha = 0.4f, signalHoveredAlpha = 0;
    public Animation yAnimation = new Animation(Easing.LINEAR, Duration.ofMillis(120));
    public boolean floatSwitchDirection = false;
    @Getter
    private int radius = 8;
    private boolean mouseDown = true;
    private String base64EncodedIconData;
    private final Location serverIcon, UNKNOWN_SERVER = Location.of("textures/misc/unknown_server.png");
    private DynamicTexture serverTexture;
    int signalStrength = -1;

    boolean selected = false;

    RoundedRectWithTriangle roundedRectWithTriangle = new RoundedRectWithTriangle();

    @Getter
    @Setter
    private String status;

    Timer serverMotdScrollTimer = new Timer();

    Minecraft mc = Minecraft.getMinecraft();

    float deleteCheckedAlpha = 0.0f;

    public ServerBean(ServerData server) {
        this.server = server;
        this.serverIcon = Location.of("servers/" + server.serverIP + "/icon");
        this.serverTexture = (DynamicTexture)this.mc.getTextureManager().getTexture(this.serverIcon);
        if (this.serverTexture != null) {
            this.serverTexture.setClearable(false);
        }
    }

    public void draw(double x, double y, double width, double height, double mouseX, double mouseY, ZephyrMultiPlayerUI inst) {
        
        double offsetY = y - this.yAnimation.getValue();

        inst.bloom.add(() -> {
            this.roundedRect(x, offsetY, width, height, this.getRadius(), new Color(0, 0, 0, 60));
        });

        this.roundedRect(x, offsetY, width, height, this.getRadius(), new Color(0, 0, 0, 120));

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, x, offsetY, width, height) && inst.dialog == null;

        this.shadowAlpha = Interpolations.interpBezier(this.shadowAlpha, hovered ? 1.0f : 0.4f, 0.2f);
        this.yAnimation.run(hovered ? 2 : 0);

        boolean signalHovered = RenderSystem.isHovered(mouseX, mouseY, x + width - 26, offsetY + 10, 20, 16) && inst.dialog == null;

        this.signalHoveredAlpha = Interpolations.interpBezier(this.signalHoveredAlpha, signalHovered ? 1.0f : 0.0f, 0.2f);

        this.checkServer(inst);

        double imgX = x + 8;
        double imgY = offsetY + 12;
        double imgSize = 32;

        mc.getTextureManager().bindTexture(this.serverTexture == null ? UNKNOWN_SERVER : this.serverIcon);
        roundedRectTextured(imgX, imgY, imgSize, imgSize, 4);

        double alighX = x + 48;
        FontManager.pf20bold.drawString(this.server.serverName, alighX, imgY, RenderSystem.hexColor(233, 233, 233));
        FontManager.pf16.drawString(StringUtils.removeFormattingCodes(this.server.populationInfo).isEmpty() ? "N/A" : StringUtils.removeFormattingCodes(this.server.populationInfo), alighX, imgY + FontManager.pf20.getHeight() + 2, RenderSystem.hexColor(200, 200, 200));

        String[] motd = FontManager.pf16.fitWidth(this.server.serverMOTD.trim(), width - 76);

        FontManager.pf16bold.drawString(String.join("\n", motd), alighX, imgY + FontManager.pf20.getHeight() + 6 + FontManager.pf16.getHeight(), RenderSystem.hexColor(130, 130, 130));

        double signalOffsetX = x + width - 12;
        double signalOffsetY = offsetY + 12;

        double signalWidth = 2.5;
        double signalRadius = 0.5;
        double spacing = signalWidth + 0.5;

        Color bg = Color.GRAY;

        if (signalStrength == -1) {
            this.roundedRect(signalOffsetX, signalOffsetY, signalWidth, 10, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing, signalOffsetY + 2, signalWidth, 8, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 2, signalOffsetY + 4, signalWidth, 6, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 3, signalOffsetY + 5, signalWidth, 5, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 4, signalOffsetY + 6, signalWidth, 4, signalRadius, bg);


            SVGImage.draw(Location.of(Phosphate.NAME + "/textures/cross.svg"), signalOffsetX - spacing * 4, signalOffsetY - 2, 16, 16, hexColor(247, 13, 26));

//            FontManager.pf25.drawString("x", signalOffsetX - spacing * 2.5, signalOffsetY - 1, hexColor(255, 0, 0));
        }

        if (signalStrength == 0) {
            this.roundedRect(signalOffsetX, signalOffsetY, signalWidth, 10, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing, signalOffsetY + 2, signalWidth, 8, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing * 2, signalOffsetY + 4, signalWidth, 6, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing * 3, signalOffsetY + 5, signalWidth, 5, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing * 4, signalOffsetY + 6, signalWidth, 4, signalRadius, Color.GREEN);
        }

        if (signalStrength == 1) {
            this.roundedRect(signalOffsetX, signalOffsetY, signalWidth, 10, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing, signalOffsetY + 2, signalWidth, 8, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing * 2, signalOffsetY + 4, signalWidth, 6, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing * 3, signalOffsetY + 5, signalWidth, 5, signalRadius, Color.GREEN);
            this.roundedRect(signalOffsetX - spacing * 4, signalOffsetY + 6, signalWidth, 4, signalRadius, Color.GREEN);
        }

        if (signalStrength == 2) {
            this.roundedRect(signalOffsetX, signalOffsetY, signalWidth, 10, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing, signalOffsetY + 2, signalWidth, 8, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 2, signalOffsetY + 4, signalWidth, 6, signalRadius, Color.YELLOW);
            this.roundedRect(signalOffsetX - spacing * 3, signalOffsetY + 5, signalWidth, 5, signalRadius, Color.YELLOW);
            this.roundedRect(signalOffsetX - spacing * 4, signalOffsetY + 6, signalWidth, 4, signalRadius, Color.YELLOW);
        }

        if (signalStrength == 3) {
            this.roundedRect(signalOffsetX, signalOffsetY, signalWidth, 10, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing, signalOffsetY + 2, signalWidth, 8, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 2, signalOffsetY + 4, signalWidth, 6, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 3, signalOffsetY + 5, signalWidth, 5, signalRadius, Color.RED);
            this.roundedRect(signalOffsetX - spacing * 4, signalOffsetY + 6, signalWidth, 4, signalRadius, Color.RED);
        }

        if (signalStrength == 4) {
            this.roundedRect(signalOffsetX, signalOffsetY, signalWidth, 10, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing, signalOffsetY + 2, signalWidth, 8, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 2, signalOffsetY + 4, signalWidth, 6, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 3, signalOffsetY + 5, signalWidth, 5, signalRadius, bg);
            this.roundedRect(signalOffsetX - spacing * 4, signalOffsetY + 6, signalWidth, 4, signalRadius, Color.RED);
        }

//        Image.draw(Location.of(Phosphate.NAME + "/textures/multiplayer/SignalStrengthBackground.png"), signalOffsetX, signalOffsetY, 73, 40, Image.Type.Normal);
//        Image.draw(Location.of(Phosphate.NAME + "/textures/multiplayer/SignalStrength.png"), signalOffsetX, signalOffsetY, 73, 40, strength, 40, Image.Type.Normal);

        int statusWidth = FontManager.pf14.getStringWidth(this.status);

        FontManager.pf14.drawString(this.status, x + width - 10 - statusWidth, offsetY + 26, RenderSystem.hexColor(233, 233, 233, (int) (this.signalHoveredAlpha * 200)));

        if (!inst.deleteMode) {
//            Image.draw(Location.of(Phosphate.NAME + "/textures/multiplayer/edit.png"), x + width - 23, offsetY + height - 18, 11.5, 11, Image.Type.Normal);
            SVGImage.draw(Location.of(Phosphate.NAME + "/textures/multiplayer/edit.svg"), x + width - 20, offsetY + height - 19, 12, 12, hexColor(255, 255, 255, 200));
        } else {
            roundedRect(x + width - 12 - 6, offsetY + height - 12 - 6, 12, 12, 5, new Color(223, 223, 223, 223));

            this.deleteCheckedAlpha = Interpolations.interpBezier(this.deleteCheckedAlpha, selected ? 1 : 0, 0.2f);

            roundedRect(x + width - 12 - 6, offsetY + height - 12 - 6, 12, 12, 5, new Color(0, 115, 221, (int) (deleteCheckedAlpha * 255)));
            this.checkRenderer.render(x + width - 12 - 6, offsetY + height - 12 - 6, 12, 2.5, selected);

        }

        this.checkMouse(x, y, width, height, mouseX, mouseY, inst);
    }

    private final CheckRenderer checkRenderer = new CheckRenderer();

    private void checkMouse(double x, double y, double width, double height, double mouseX, double mouseY, ZephyrMultiPlayerUI inst) {
        double offsetY = y - this.yAnimation.getValue();

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, x, offsetY, width, height) && inst.dialog == null;
        if (hovered && Mouse.isButtonDown(0) && !mouseDown) {
            mouseDown = true;

            if (!inst.deleteMode) {

                boolean hoveredEdit = RenderSystem.isHovered(mouseX, mouseY, x + width - 20, offsetY + height - 19, 12, 12, -4) && inst.dialog == null;

                if (!hoveredEdit) {
                    mc.displayGuiScreen(new GuiConnecting(inst, this.mc, server));
                } else {
                    inst.dialog = new ServerInfoDialog(server, inst.serverList.getServers().indexOf(server));
                }

            } else {
                selected = !selected;
            }
        }

        if (!Mouse.isButtonDown(0) && mouseDown)
            mouseDown = false;

    }

    Localizable lCantResolveHostname = Localizable.of("multiplayerui.cantresolvehostname");
    Localizable lCantConnectToServer = Localizable.of("multiplayerui.cantconnecttoserver");
    Localizable lCOD = Localizable.of("multiplayerui.clientoutofdate");
    Localizable lSOD = Localizable.of("multiplayerui.serveroutofdate");
    Localizable lNoConnection = Localizable.of("multiplayerui.noconnection");
    Localizable lPinging = Localizable.of("multiplayerui.pinging");

    private void checkServer(ZephyrMultiPlayerUI inst) {
        if (!this.server.pinged)
        {
            this.server.pinged = true;
            this.server.pingToServer = -2L;
            this.server.serverMOTD = "";
            this.server.populationInfo = "";
            inst.pingers.submit(() -> {
                try
                {
                    inst.oldServerPinger.ping(server);
                }
                catch (UnknownHostException var2)
                {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + lCantResolveHostname.get();
                }
                catch (Exception var3)
                {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + lCantConnectToServer.get();
                }
            });
        }

        boolean clientOutdated = this.server.version > 47;
        boolean serverOutdated = this.server.version < 47;
        boolean clientOrServerOutdated = clientOutdated || serverOutdated;

        if (clientOrServerOutdated)
        {
            status = clientOutdated ? lCOD.get() : lSOD.get();
        }
        else if (this.server.pinged && this.server.pingToServer != -2L)
        {
            if (this.server.pingToServer < 0L)
            {
                signalStrength = 5;
            }
            else if (this.server.pingToServer < 150L)
            {
                signalStrength = 0;
            }
            else if (this.server.pingToServer < 300L)
            {
                signalStrength = 1;
            }
            else if (this.server.pingToServer < 600L)
            {
                signalStrength = 2;
            }
            else if (this.server.pingToServer < 1000L)
            {
                signalStrength = 3;
            }
            else
            {
                signalStrength = 4;
            }

            if (this.server.pingToServer < 0L)
            {
                signalStrength = -1;
                status = lNoConnection.get();
            }
            else
            {
                status = this.server.pingToServer + "ms";
            }
        }
        else
        {
            status = lPinging.get();
        }

        if (this.server.getBase64EncodedIconData() != null && !this.server.getBase64EncodedIconData().equals(this.base64EncodedIconData))
        {
            this.base64EncodedIconData = this.server.getBase64EncodedIconData();
            this.prepareServerIcon();
            inst.serverList.saveServerList();
        }
    }

    private void prepareServerIcon()
    {
        if (this.server.getBase64EncodedIconData() == null)
        {
            this.mc.getTextureManager().deleteTexture(this.serverIcon);
            this.serverTexture = null;
        }
        else
        {
            ByteBuf bytebuf = Unpooled.copiedBuffer(this.server.getBase64EncodedIconData(), Charsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);
            BufferedImage bufferedimage;
            label101:
            {
                try
                {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                    break label101;
                }
                catch (Throwable throwable)
                {
                    Minecraft.getLogger().error("Invalid icon for server " + this.server.serverName + " (" + this.server.serverIP + ")", throwable);
                    this.server.setBase64EncodedIconData(null);
                }
                finally
                {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (this.serverTexture == null)
            {
                this.serverTexture = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                this.serverTexture.setClearable(false);
                this.mc.getTextureManager().loadTexture(this.serverIcon, this.serverTexture);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), this.serverTexture.getTextureData(), 0, bufferedimage.getWidth());
            this.serverTexture.updateDynamicTexture();
        }
    }

}
