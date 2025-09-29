package tech.konata.phosphate.screens.clickgui.panels;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.ncm.OptionsUtil;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.QRCodeGenerator;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;

import java.awt.*;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 4:05 PM
 */
public class LoginRenderer implements SharedRenderingConstants {

    @Getter
    public boolean closing = false;
    Thread loginThread;
    boolean success = false;
    float screeMaskAlpha = 0;
    double scale = 0;

    Localizable login = Localizable.of("panel.music.login");

    public boolean avatarLoaded = false;
    public Location tempAvatar = Location.of(Phosphate.NAME + "/textures/TempAvatar.png");
    public String tempUsername = "";

    public LoginRenderer() {
        loginThread = new Thread(() -> {
            String cookie = CloudMusic.qrCodeLogin();
//            System.out.println("Cookie is " + cookie);
            OptionsUtil.setCookie(cookie);
            success = true;
            this.closing = true;
        });

        loginThread.start();
    }

    public void render(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        screeMaskAlpha = Interpolations.interpBezier(screeMaskAlpha * 255, this.isClosing() ? 0 : 120, 0.3f) * RenderSystem.DIVIDE_BY_255;
        scale = Interpolations.interpBezier(scale, this.isClosing() ? 0 : 0.99999, 0.2);

        Rect.draw(posX, posY, width, height, RenderSystem.hexColor(0, 0, 0, (int) (screeMaskAlpha * 255)), Rect.RectType.EXPAND);

        GlStateManager.pushMatrix();
        RenderSystem.translateAndScale(posX + width / 2.0, posY + height / 2.0, scale);

        double pWidth = width / 2.0;
        double pHeight = height / 1.2;

        double x = posX + width / 2.0 - pWidth / 2.0;
        double y = posY + height / 2.0 - pHeight / 2.0;

//        BLOOM.add(() -> {
//            roundedRect(x, y, pWidth, pHeight, 5, new Color(43, 43, 43));
//        });

//        RenderSystem.doScissor((inta) x, (int) y, (int) pWidth, (int) pHeight);

        roundedRect(x, y, pWidth, pHeight, 5, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface));

        double qWidth = 128, qHeight = 128;

        String[] strings = FontManager.pf40.fitWidth(login.get(), pWidth - 16);

        double startY = posY + height / 6.0;

        for (String string : strings) {
            FontManager.pf40.drawCenteredString(string, posX + width / 2.0, startY, ThemeManager.get(ThemeManager.ThemeColor.Text));
            startY += FontManager.pf40.getHeight();
        }

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        ITextureObject qrCode = textureManager.getTexture(QRCodeGenerator.qrCode);

        if (qrCode != null) {
            tech.konata.phosphate.rendering.entities.impl.Image.draw(qrCode.getGlTextureId(), posX + width / 2.0 - qWidth / 2.0, posY + height / 6.0 * 4.0 - qHeight / 2.0, qWidth, qHeight, tech.konata.phosphate.rendering.entities.impl.Image.Type.Normal);
        } else {
            Rect.draw(posX + width / 2.0 - qWidth / 2.0, posY + height / 6.0 * 4.0 - qHeight / 2.0, qWidth, qHeight, Color.GRAY.getRGB(), Rect.RectType.EXPAND);
        }

        if (avatarLoaded) {
            FontManager.pf25bold.drawCenteredString(tempUsername, posX + width / 2.0, posY + height / 4.0, ThemeManager.get(ThemeManager.ThemeColor.Text));

            ITextureObject tempAvatarTexture = textureManager.getTexture(tempAvatar);
            if (tempAvatarTexture != null) {
                GlStateManager.bindTexture(tempAvatarTexture.getGlTextureId());

                double size = 64;

                RenderSystem.linearFilter();

                roundedRectTextured(posX + width / 2.0 - size / 2.0, posY + height / 3.2, size, size, 4);
            }

        }

        GlStateManager.popMatrix();
    }

    public boolean canClose() {
        return this.isClosing() && this.screeMaskAlpha <= 0.05 && success && scale < 0.05;
    }

}
