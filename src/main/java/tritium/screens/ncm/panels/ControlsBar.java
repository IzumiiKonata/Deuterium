package tritium.screens.ncm.panels;

import org.lwjgl.input.Mouse;
import tritium.ncm.music.AudioPlayer;
import tritium.ncm.music.CloudMusic;
import tritium.management.FontManager;
import tritium.rendering.ui.widgets.*;
import tritium.screens.ncm.MusicLyricsPanel;
import tritium.screens.ncm.NCMPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.widget.impl.MusicInfoWidget;
import tritium.widget.impl.MusicLyricsWidget;

import java.awt.*;

/**
 * @author IzumiiKonata
 * Date: 2025/10/17 21:24
 */
public class ControlsBar extends NCMPanel {

    public ControlsBar() {
    }

    @Override
    public void onInit() {
        RectWidget bg = new RectWidget();

        this.addChild(bg);

        bg.setColor(0xFF1D1D1D)
          .setAlpha(.95f)
          .setBeforeRenderCallback(() -> {
              bg.setMargin(0);
          });

        RoundedImageWidget playingCover = new RoundedImageWidget(() -> {
            if (CloudMusic.currentlyPlaying == null)
                return null;

            return MusicInfoWidget.getMusicCoverSmall(CloudMusic.currentlyPlaying);
        }, 0 , 0, 0, 0);

        this.addChild(playingCover);

        playingCover
                .fadeIn()
                .setLinearFilter(true)
                .setShouldSetMouseCursor(true)
                .setBeforeRenderCallback(() -> {
                    playingCover
                            .setMargin(5)
                            .setBounds(playingCover.getHeight(), playingCover.getHeight())
                            .setRadius(2);
                })
                .setOnClickCallback((relativeX, relativeY, mouseButton) -> {
                    if (CloudMusic.currentlyPlaying != null) {
                        NCMScreen.getInstance().musicLyricsPanel = new MusicLyricsPanel(CloudMusic.currentlyPlaying);
                    }

                    return true;
                });

        double buttonsYOffset = -4;

        IconWidget playPause = new IconWidget("B", FontManager.icon30, 0, 0, 20, 20);

        this.addChild(playPause);

        playPause
                .setBeforeRenderCallback(() -> {
                    boolean showPausingIcon = CloudMusic.player == null || CloudMusic.player.isPausing();

                    playPause
                            .center()
                            .setIcon(showPausingIcon ? "B" : "A")
                            .setPosition(playPause.getRelativeX(), playPause.getRelativeY() + buttonsYOffset)
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                })
                .setOnClickCallback((x, y, i) -> {
                    boolean hasCurrentlyPlaying = CloudMusic.player != null && CloudMusic.currentlyPlaying != null;
                    if (hasCurrentlyPlaying) {
                        if (CloudMusic.player.isPausing())
                            CloudMusic.player.unpause();
                        else
                            CloudMusic.player.pause();
                    }
                    return true;
                });

        IconWidget prev = new IconWidget("H", FontManager.icon30, 0, 0, 20, 20);

        this.addChild(prev);

        prev
                .setOnClickCallback((x, y, i) -> {
                    if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                        CloudMusic.prev();

                    return true;
                })
                .setBeforeRenderCallback(() -> {
                    prev
                            .center()
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT))
                            .setPosition(prev.getRelativeX() - 20 - prev.getWidth() * .5, prev.getRelativeY() + buttonsYOffset);
                });

        IconWidget next = new IconWidget("E", FontManager.icon30, 0, 0, 20, 20);
        this.addChild(next);

        next
                .setOnClickCallback((x, y, i) -> {
                    if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                        CloudMusic.next();

                    return true;
                })
                .setBeforeRenderCallback(() -> {
                    next
                            .center()
                            .setPosition(next.getRelativeX() + next.getWidth() * .5 + 20, next.getRelativeY() + buttonsYOffset)
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                });

        RoundedRectWidget progressBarBg = new RoundedRectWidget() {

            boolean prevMouse = false;

            @Override
            public void onRender(double mouseX, double mouseY) {
                super.onRender(mouseX, mouseY);

                if (prevMouse && !Mouse.isButtonDown(0))
                    prevMouse = false;

                if (this.testHovered(mouseX, mouseY, 1) && Mouse.isButtonDown(0) && !prevMouse) {
                    prevMouse = true;
                    double xDelta = Math.max(0, Math.min(this.getWidth(), (mouseX - this.getX())));
                    double percent = xDelta / this.getWidth();

                    if (CloudMusic.player != null) {
                        float progress = (float) (percent * CloudMusic.player.getTotalTimeMillis());
                        CloudMusic.player.setPlaybackTime(progress);
                        MusicLyricsWidget.quickResetProgress(progress);
                        MusicLyricsPanel.resetProgress(progress);
                    }
                }
            }
        };

        this.addChild(progressBarBg);

        progressBarBg
                .setColor(Color.GRAY)
                .setRadius(1)
                .setBounds(135, 3)
                .setShouldSetMouseCursor(true)
                .setBeforeRenderCallback(() -> {
                    progressBarBg
                            .center()
                            .setPosition(progressBarBg.getRelativeX(), progressBarBg.getRelativeY() + 8);
                });

        RoundedRectWidget progressBar = new RoundedRectWidget();

        progressBarBg.addChild(progressBar);
        progressBar
                .setColor(-1)
                .setWidth(0)
                .setClickable(false)
                .setBeforeRenderCallback(() -> {
                    progressBar.setMargin(0);

                    AudioPlayer player = CloudMusic.player;
                    if (player == null)
                        return;

                    float perc = player.getCurrentTimeMillis() / player.getTotalTimeMillis();
                    progressBar
                            .setWidth(perc * progressBarBg.getWidth())
                            .setRadius(perc);
                });

        LabelWidget lblCurTime = new LabelWidget(
                () -> {
                    if (CloudMusic.player == null)
                        return "00:00";
                    return formatDuration(CloudMusic.player.getCurrentTimeMillis());
                },
                FontManager.pf12
        );
        this.addChild(lblCurTime);

        lblCurTime
                .setClickable(false)
                .setBeforeRenderCallback(() -> {
                    lblCurTime
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT))
                            .setPosition(
                                    progressBarBg.getRelativeX() - lblCurTime.getWidth() - 4,
                                    progressBarBg.getRelativeY() + progressBarBg.getHeight() * .5 - lblCurTime.getHeight() * .5
                            );
                });

        LabelWidget lblRemainingTime = new LabelWidget(
                () -> {
                    if (CloudMusic.player == null)
                        return "00:00";
                    return "-" + formatDuration(CloudMusic.player.getTotalTimeMillis() - CloudMusic.player.getCurrentTimeMillis());
                },
                FontManager.pf12
        );
        this.addChild(lblRemainingTime);

        lblRemainingTime
                .setClickable(false)
                .setBeforeRenderCallback(() -> {
                    lblRemainingTime
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT))
                            .setPosition(progressBarBg.getRelativeX() + progressBarBg.getWidth() + 4, lblCurTime.getRelativeY());
                });

        LabelWidget lblMusicName = new LabelWidget(() -> CloudMusic.currentlyPlaying == null ? "未在播放" : CloudMusic.currentlyPlaying.getName(), FontManager.pf14bold);
        this.addChild(lblMusicName);

        lblMusicName
                .setClickable(false)
                .setBeforeRenderCallback(() -> {
                    lblMusicName
                            .centerVertically()
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT))
                            .setMaxWidth(lblCurTime.getRelativeX() - lblMusicName.getRelativeX() - 4)
                            .setPosition(
                                    playingCover.getRelativeX() + playingCover.getWidth() + 4,
                                    lblMusicName.getRelativeY() - lblMusicName.getHeight() * .5 - 2
                            );
                });

        LabelWidget lblMusicArtist = new LabelWidget(
                () -> {
                    if (CloudMusic.currentlyPlaying == null)
                        return "无";
                    return CloudMusic.currentlyPlaying.getArtistsName() + " - " + CloudMusic.currentlyPlaying.getAlbum().getName();
                },
                FontManager.pf14bold
        );
        this.addChild(lblMusicArtist);

        lblMusicArtist
                .setClickable(false)
                .setBeforeRenderCallback(() -> {
                    lblMusicArtist
                            .centerVertically()
                            .setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT))
                            .setMaxWidth(lblCurTime.getRelativeX() - lblMusicArtist.getRelativeX() - 4)
                            .setPosition(
                                    playingCover.getRelativeX() + playingCover.getWidth() + 4,
                                    lblMusicArtist.getRelativeY() + lblMusicArtist.getHeight() * .5 + 2
                            );
                });
    }

    private String formatDuration(float totalMillis) {
        float totalSeconds = totalMillis / 1000;

        float hours = totalSeconds / 3600;
        float minutes = (totalSeconds % 3600) / 60;
        float seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if ((int) hours > 0) {
            sb.append(String.format("%02d:", (int) hours));
        }

        sb.append(String.format("%02d:", (int) minutes));
        sb.append(String.format("%02d", (int) seconds));

        return sb.toString();
    }
}
