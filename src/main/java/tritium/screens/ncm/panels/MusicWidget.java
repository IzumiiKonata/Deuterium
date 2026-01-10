package tritium.screens.ncm.panels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RoundedImageWidget;
import tritium.rendering.ui.widgets.RoundedRectWidget;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.awt.*;
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
        super(0, 0, 0, 30);
        this.music = music;
        this.playList = playList;

        RoundedRectWidget rrHoverIndicator = new RoundedRectWidget();
        this.addChild(rrHoverIndicator);
        rrHoverIndicator
                .setAlpha(0f)
                .setClickable(false);
        rrHoverIndicator.setBeforeRenderCallback(() -> {
            rrHoverIndicator
                    .setMargin(0)
                    .setRadius(this.getRadius())
                    .setColor(NCMScreen.getColor(NCMScreen.ColorType.ELEMENT_HOVER));
        });

        RoundedRectWidget rrPlayingIndicator = new RoundedRectWidget();
        this.addChild(rrPlayingIndicator);
        rrPlayingIndicator
                .setAlpha(0f)
                .setClickable(false);
        rrPlayingIndicator.setBeforeRenderCallback(() -> {
            rrPlayingIndicator
                    .setMargin(0)
                    .setRadius(this.getRadius())
                    .setColor(0xFFD60017);
        });

        this.setBeforeRenderCallback(() -> {

            // 只在这个 music 被渲染的时候才加载封面
            if (!coverLoaded) {
                coverLoaded = true;
                this.loadCover();
            }

            this.setBounds(this.getParentWidth(), 30);
            this.setColor(NCMScreen.getColor(index % 2 == 0 ? NCMScreen.ColorType.ELEMENT_BACKGROUND : NCMScreen.ColorType.GENERIC_BACKGROUND));

            if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying.getId() == music.getId()) {
//                this.setColor(0xFFD60017);
                rrPlayingIndicator.setAlpha(Interpolations.interpBezier(rrPlayingIndicator.getAlpha(), .9f, .6f));
                rrPlayingIndicator.setHidden(false);
            } else if (this.isHovering()) {
//                this.setColor(NCMScreen.getColor(NCMScreen.ColorType.ELEMENT_HOVER));
                rrHoverIndicator.setAlpha(Interpolations.interpBezier(rrHoverIndicator.getAlpha(), 1, .4f));
                rrHoverIndicator.setHidden(false);
            } else {
                rrPlayingIndicator.setAlpha(Interpolations.interpBezier(rrPlayingIndicator.getAlpha(), 0, .6f));
                rrHoverIndicator.setAlpha(Interpolations.interpBezier(rrHoverIndicator.getAlpha(), 0, .4f));

                if (rrPlayingIndicator.getWidgetAlpha() <= .05f)
                    rrPlayingIndicator.setHidden(true);

                if (rrHoverIndicator.getWidgetAlpha() <= .05f)
                    rrHoverIndicator.setHidden(true);
            }

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

        boolean musicDirty = music.isDirty();
        double dirtyIndicatorSize = 8;

        String translatedNames = music.getTranslatedNames();

        LabelWidget lblMusicName = new LabelWidget(music.getName() + (translatedNames.isEmpty() ? "" : EnumChatFormatting.GRAY + " (" + translatedNames + ")"), FontManager.pf14bold);
        this.addChild(lblMusicName);

        lblMusicName
                .setWidthLimitType(LabelWidget.WidthLimitType.TRIM_TO_WIDTH)
                .setBeforeRenderCallback(() -> {
                    lblMusicName.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                    lblMusicName.centerVertically();
                    lblMusicName.setPosition(cover.getRelativeX() + cover.getWidth() + 4, lblMusicName.getRelativeY() - lblMusicName.getHeight() * .5 - 2);
                    lblMusicName.setMaxWidth(this.getWidth() - (cover.getRelativeX() + cover.getWidth() + 4 + 32 + (musicDirty ? (dirtyIndicatorSize + 4) : 0)));
                });
        lblMusicName.setClickable(false);

        if (musicDirty) {
            RoundedRectWidget dirtyIndicator = new RoundedRectWidget(0, 0, dirtyIndicatorSize, dirtyIndicatorSize);
            this.addChild(dirtyIndicator);
            dirtyIndicator
                    .setRadius(1.5)
                    .setColor(Color.GRAY);

            dirtyIndicator.setBeforeRenderCallback(() -> {
//                dirtyIndicator.centerVertically();
                dirtyIndicator.setPosition(lblMusicName.getRelativeX() + lblMusicName.getWidth() + 2, lblMusicName.getRelativeY() + lblMusicName.getHeight() * .5 - dirtyIndicatorSize * .5);
            });

            dirtyIndicator.setClickable(false);

            LabelWidget lblDirty = new LabelWidget("E", FontManager.pf12bold);
            dirtyIndicator.addChild(lblDirty);
            lblDirty.setBeforeRenderCallback(() -> {
                lblDirty.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                lblDirty.center();
            });
        }

        LabelWidget lblMusicArtist = new LabelWidget(music.getArtistsName() + " - " + music.getAlbum().getName(), FontManager.pf14);
        this.addChild(lblMusicArtist);

        lblMusicArtist
                .setWidthLimitType(LabelWidget.WidthLimitType.TRIM_TO_WIDTH)
                .setBeforeRenderCallback(() -> {
                    if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying.getId() == music.getId())
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
            if (CloudMusic.currentlyPlaying != null && CloudMusic.currentlyPlaying.getId() == music.getId())
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
                        if (textureManager.getTexture(coverLoc) != null) {
                            textureManager.deleteTexture(coverLoc);
                        }
                        Textures.loadTexture(coverLoc, img);
                        img.close();
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
