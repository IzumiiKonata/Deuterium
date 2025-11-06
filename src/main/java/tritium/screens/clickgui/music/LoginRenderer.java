package tritium.screens.clickgui.music;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tritium.ncm.OptionsUtil;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.QRCodeGenerator;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Image;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.i18n.Localizable;

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
    double scale = 1;

    Localizable login = Localizable.of("panel.music.login");

    public boolean avatarLoaded = false;
    public Location tempAvatar = Location.of("tritium/textures/TempAvatar.png");
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

    public void render(double mouseX, double mouseY, double posX, double posY, double width, double height, float alpha) {
        screeMaskAlpha = Interpolations.interpBezier(screeMaskAlpha * 255, this.isClosing() ? 0 : 120, 0.3f) * RenderSystem.DIVIDE_BY_255;
//        scale = Interpolations.interpBezier(scale, this.isClosing() ? 0 : 0.99999, 0.2);

//        Rect.draw(posX, posY, width, height, RenderSystem.hexColor(0, 0, 0, (int) (screeMaskAlpha * 255)));

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

//        Rect.draw(x, y, pWidth, pHeight, ThemeManager.get(ThemeManager.ThemeColor.OnSurface));

        double qWidth = 96, qHeight = 96;

        String[] strings = FontManager.pf20.fitWidth(login.get(), width - 24);

        double startY = posY + height / 6.0;

        for (String string : strings) {
            FontManager.pf20.drawCenteredString(string, posX + width / 2.0, startY, ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (alpha * 255)));
            startY += FontManager.pf20.getHeight();
        }

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        ITextureObject qrCode = textureManager.getTexture(QRCodeGenerator.qrCode);

        if (qrCode != null) {
            GlStateManager.color(1, 1, 1, alpha);
            Image.draw(qrCode.getGlTextureId(), posX + width / 2.0 - qWidth / 2.0, posY + height / 6.0 * 4.0 - qHeight / 2.0, qWidth, qHeight, Image.Type.NoColor);
        } else {
            Rect.draw(posX + width / 2.0 - qWidth / 2.0, posY + height / 6.0 * 4.0 - qHeight / 2.0, qWidth, qHeight, hexColor(128, 128, 128, (int) (alpha * 255)));
        }

        if (avatarLoaded) {
            FontManager.pf25bold.drawCenteredString(tempUsername, posX + width / 2.0, posY + height / 4.0, ThemeManager.get(ThemeManager.ThemeColor.Text, (int) (alpha * 255)));

            ITextureObject tempAvatarTexture = textureManager.getTexture(tempAvatar);
            if (tempAvatarTexture != null) {
                GlStateManager.bindTexture(tempAvatarTexture.getGlTextureId());

                double size = 48;

                RenderSystem.linearFilter();

                GlStateManager.color(1, 1, 1, alpha);
                Image.draw(posX + width / 2.0 - size / 2.0, posY + height / 3.2, size, size, Image.Type.NoColor);
            }

        }

        GlStateManager.popMatrix();
    }

    public boolean canClose() {
        return this.isClosing() && this.screeMaskAlpha <= 0.05 && success;
    }

}
