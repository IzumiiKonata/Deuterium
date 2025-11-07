package tritium.screens.ncm.panels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RoundedImageWidget;
import tritium.rendering.ui.widgets.RoundedRectWidget;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.io.InputStream;

/**
 * @author IzumiiKonata
 * Date: 2025/10/17 20:40
 */
public class MusicWidget extends RoundedRectWidget {

    public PlayList playList;
    public Music music;
    boolean coverLoaded = false;

    public MusicWidget(Music music, PlayList playList, int index) {
        super(0, 0, 0, 0);
        this.music = music;
        this.playList = playList;

        this.setBeforeRenderCallback(() -> {

            // 只在这个屌 music 可以被看到的时候才加载封面
            if (!coverLoaded) {
                coverLoaded = true;
                this.loadCover();
            }

            this.setBounds(this.getParentWidth(), 30);

            if (CloudMusic.currentlyPlaying == music)
                this.setColor(0xFFD60017);
            else if (this.isHovering())
                this.setColor(NCMScreen.getColor(NCMScreen.ColorType.ELEMENT_HOVER));
            else
                this.setColor(NCMScreen.getColor(index % 2 == 0 ? NCMScreen.ColorType.ELEMENT_BACKGROUND : NCMScreen.ColorType.GENERIC_BACKGROUND));

            this.setRadius(2);
        });

        this.setOnClickCallback((x, y, i) -> {

            if (i == 0)
                CloudMusic.play(playList.getMusics(), index);

            return true;
        });

        RoundedImageWidget cover = new RoundedImageWidget(this.getCover(), 0, 0, 0, 0);
        this.addChild(cover);
        cover.fadeIn();
        cover.setLinearFilter(true);
        cover.setBeforeRenderCallback(() -> {
            cover.setRadius(2);
            cover.setBounds(24, 24);
            cover.centerVertically();
            cover.setPosition(30, cover.getRelativeY());
        });
        cover.setClickable(false);

        LabelWidget lblMusicIndex = new LabelWidget(String.valueOf(index + 1), FontManager.pf14bold);
        this.addChild(lblMusicIndex);

        lblMusicIndex.setBeforeRenderCallback(() -> {
            if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying.getId() == music.getId())
                lblMusicIndex.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            else
                lblMusicIndex.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
            lblMusicIndex.centerVertically();
            lblMusicIndex.setPosition(cover.getRelativeX() - 4 - lblMusicIndex.getWidth(), lblMusicIndex.getRelativeY());
        });

        lblMusicIndex.setClickable(false);

        LabelWidget lblMusicName = new LabelWidget(music.getName(), FontManager.pf14bold);
        this.addChild(lblMusicName);

        lblMusicName
                .setWidthLimitType(LabelWidget.WidthLimitType.TRIM_TO_WIDTH)
                .setBeforeRenderCallback(() -> {
                    lblMusicName.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                    lblMusicName.centerVertically();
                    lblMusicName.setPosition(cover.getRelativeX() + cover.getWidth() + 4, lblMusicName.getRelativeY() - lblMusicName.getHeight() * .5 - 2);
                    lblMusicName.setMaxWidth(this.getWidth() - (cover.getRelativeX() + cover.getWidth() + 4 + 32));
                });
        lblMusicName.setClickable(false);

        LabelWidget lblMusicArtist = new LabelWidget(music.getArtistsName() + " - " + music.getAlbum().getName(), FontManager.pf14bold);
        this.addChild(lblMusicArtist);

        lblMusicArtist
                .setWidthLimitType(LabelWidget.WidthLimitType.TRIM_TO_WIDTH)
                .setBeforeRenderCallback(() -> {
                    if (CloudMusic.currentlyPlaying == music)
                        lblMusicArtist.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                    else
                        lblMusicArtist.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
                    lblMusicArtist.centerVertically();
                    lblMusicArtist.setPosition(cover.getRelativeX() + cover.getWidth() + 4, lblMusicArtist.getRelativeY() + lblMusicArtist.getHeight() * .5 + 2);
                    lblMusicArtist.setMaxWidth(this.getWidth() - (cover.getRelativeX() + cover.getWidth() + 4 + 32));
                });

        lblMusicArtist.setClickable(false);

        LabelWidget lblMusicDuration = new LabelWidget(formatDuration(music.getDuration()), FontManager.pf14bold);
        this.addChild(lblMusicDuration);
        lblMusicDuration.setBeforeRenderCallback(() -> {
            if (CloudMusic.currentlyPlaying == music)
                lblMusicDuration.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            else
                lblMusicDuration.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
            lblMusicDuration.centerVertically();
            lblMusicDuration.setPosition(this.getWidth() - 8 - lblMusicDuration.getWidth(), lblMusicDuration.getRelativeY());
        });
        lblMusicDuration.setClickable(false);
    }

    private String formatDuration(long totalMillis) {
        long totalSeconds = totalMillis / 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(String.format("%02d:", hours));
        }

        sb.append(String.format("%02d:", minutes));
        sb.append(String.format("%02d", seconds));

        return sb.toString();
    }

    private void loadCover() {

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location coverLoc = this.getCover();
        if (textureManager.getTexture(coverLoc) != null)
            return;

        MultiThreadingUtil.runAsync(() -> {
            try (InputStream inputStream = HttpUtils.downloadStream(music.getCoverUrl(64))) {
                if (inputStream != null) {
                    NativeBackedImage img = NativeBackedImage.make(inputStream);

                    if (img != null) {
                        AsyncGLContext.submit(() -> {
                            if (textureManager.getTexture(coverLoc) != null) {
                                textureManager.deleteTexture(coverLoc);
                            }
                            Textures.loadTexture(coverLoc, img);
                            img.close();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private Location getCover() {
        return Location.of("tritium/textures/music/" + music.getId() + "/cover.png");
    }

}
