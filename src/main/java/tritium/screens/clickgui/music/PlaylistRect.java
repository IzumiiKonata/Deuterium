package tritium.screens.clickgui.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.ncmplayer.music.CloudMusic;
import tech.konata.ncmplayer.music.dto.Music;
import tech.konata.ncmplayer.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.widgets.ImageWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.module.ModuleListWindow;
import tritium.utils.math.RandomUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date 2025/10/3 12:14
 */
public class PlaylistRect extends RectWidget {

    private PlayList playlist;
    boolean imgLoaded = false;

    public PlaylistRect(PlayList playlist, Supplier<Integer> colorSupplier) {
        this.playlist = playlist;

        this.setBounds(0, 24);

        this.setBeforeRenderCallback(() -> {
            this.setColor(isHovering() ? ClickGui.getColor(22) : colorSupplier.get());
        });

        this.setOnClickCallback((rx, ry, i) -> {

            if (i != 0)
                return true;

            PlaylistsWindow playlists = ClickGui.getInstance().getPlaylistsWindow();
            if (playlists.getOnSetting() == this.playlist)
                return true;
            if (playlists.getLastOnSetting() == null) {
                playlists.setLastOnSetting(this.playlist);
            } else if (playlists.getLastOnSetting() == playlists.getOnSetting()) {
                playlists.setLastOnSetting(playlists.getOnSetting());
            }

            playlists.setOnSetting(this.playlist);

            return true;
        });

        RectWidget imgBg = new RectWidget();
        this.addChild(imgBg);
        imgBg.setMargin(2);
        imgBg.setBounds(imgBg.getHeight(), imgBg.getHeight());
        imgBg.setColor(Color.GRAY);
        imgBg.setClickable(false);

        ImageWidget cover = new ImageWidget(() -> {
            if (CloudMusic.profile == null)
                return null;

            Location coverLoc = Location.of("tritium/textures/playlist_" + playlist.id + ".png");

            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            if (textureManager.getTexture(coverLoc) == null) {

                if (!imgLoaded) {
                    imgLoaded = true;
                    MultiThreadingUtil.runAsync(() -> {
                        try (InputStream inputStream = HttpUtils.downloadStream(playlist.coverUrl + "?param=" + (int) (imgBg.getWidth() * 2) + "y" + (int) (imgBg.getHeight() * 2))) {
                            if (inputStream != null) {
                                BufferedImage img = NativeBackedImage.make(inputStream);
                                AsyncGLContext.submit(() -> {
                                    if (textureManager.getTexture(coverLoc) != null) {
                                        textureManager.deleteTexture(coverLoc);
                                    }
                                    Textures.loadTexture(coverLoc, img);
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                return null;
            }

            return coverLoc;
        }, 0, 0, imgBg.getWidth(), imgBg.getHeight()) {

            float alpha = .0f;

            @Override
            public void onRender(double mouseX, double mouseY, int dWheel) {
                super.onRender(mouseX, mouseY, dWheel);
                this.setAlpha(alpha);

                alpha = Interpolations.interpBezier(alpha, this.getLocImg().get() != null ? 1f : 0f, 0.2f);
            }
        };
        imgBg.addChild(cover);
        cover.setClickable(false);

        LabelWidget rightArrowLabel = new LabelWidget(">", FontManager.pf20);
        this.addChild(rightArrowLabel);
        rightArrowLabel.setClickable(false);
        rightArrowLabel.setBeforeRenderCallback(() -> {
            rightArrowLabel.setColor(ClickGui.getColor(9));
            rightArrowLabel.setPosition(rightArrowLabel.getParentWidth() - rightArrowLabel.getWidth() - 4, 0);
            rightArrowLabel.centerVertically();
        });

        LabelWidget playlistLabel = new LabelWidget(() -> this.playlist.name, FontManager.pf16);
        this.addChild(playlistLabel);
        playlistLabel.setClickable(false);
        playlistLabel.setBeforeRenderCallback(() -> {
            playlistLabel.setColor(ClickGui.getColor(9));
            playlistLabel.setPosition(6 + imgBg.getWidth(), 0);
            playlistLabel.centerVertically();
            playlistLabel.setMaxWidth(this.getWidth() - 6 - imgBg.getWidth() - 8 - rightArrowLabel.getWidth());
        });
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        super.onRender(mouseX, mouseY, dWheel);
    }
}
