package tritium.screens.ncm.panels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.ncmplayer.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.widgets.*;
import tritium.screens.ncm.NCMPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.io.InputStream;

/**
 * @author IzumiiKonata
 * Date: 2025/10/17 18:42
 */
public class PlaylistPanel extends NCMPanel {

    public PlayList playList;

    public PlaylistPanel(PlayList playlist) {
        this.playList = playlist;
    }

    @Override
    public void onInit() {
        RoundedImageWidget cover = new RoundedImageWidget(this.getCoverLocation(), 0, 0, 0, 0);

        cover.setPosition(16, 16);
        cover.setBounds(128, 128);
        cover.setAlpha(0);

        this.addChild(cover);
        this.loadCover();

        cover.setBeforeRenderCallback(() -> {
            cover.setRadius(4);
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            Location coverLoc = this.getCoverLocation();
            if (textureManager.getTexture(coverLoc) != null)
                cover.setAlpha(Interpolations.interpBezier(cover.getAlpha(), 1.0f, 0.2f));
        });

//        LabelWidget lblPlaylistName = new LabelWidget(playList.name, FontManager.pf);
        RoundedButtonWidget btnPlay = new RoundedButtonWidget("播放歌单", FontManager.pf16);
        this.addChild(btnPlay);

        btnPlay.setBeforeRenderCallback(() -> {
            btnPlay.setBounds(57, 17);
            btnPlay.setPosition(cover.getRelativeX() + cover.getWidth() + 8, cover.getRelativeY() + cover.getHeight() - btnPlay.getHeight());
            btnPlay.setRadius(4);
            btnPlay.setColor(0xFFd60017);
            btnPlay.setTextColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        RoundedButtonWidget btnPlayRandomOrder = new RoundedButtonWidget("乱序播放歌单", FontManager.pf16);
        this.addChild(btnPlayRandomOrder);

        btnPlayRandomOrder.setBeforeRenderCallback(() -> {
            btnPlayRandomOrder.setBounds(57, 17);
            btnPlayRandomOrder.setPosition(cover.getRelativeX() + cover.getWidth() + 8 + btnPlay.getWidth() + 8, cover.getRelativeY() + cover.getHeight() - btnPlayRandomOrder.getHeight());
            btnPlayRandomOrder.setRadius(4);
            btnPlayRandomOrder.setColor(0xFFd60017);
            btnPlayRandomOrder.setTextColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        RoundedRectWidget rrwCreatorPlaceholder = new RoundedRectWidget(0, 0, 0, 0);
    }

    private void loadCover() {

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location coverLoc = this.getCoverLocation();
        if (textureManager.getTexture(coverLoc) != null)
            return;

        MultiThreadingUtil.runAsync(() -> {
            try (InputStream inputStream = HttpUtils.downloadStream(playList.coverUrl + "?param=256y256")) {
                if (inputStream != null) {
                    NativeBackedImage img = NativeBackedImage.make(inputStream);
                    AsyncGLContext.submit(() -> {
                        if (textureManager.getTexture(coverLoc) != null) {
                            textureManager.deleteTexture(coverLoc);
                        }
                        Textures.loadTexture(coverLoc, img);
                        img.close();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private Location getCoverLocation() {
        return Location.of("tritium/textures/playlist/" + this.playList.id + "/cover.png");
    }
}
