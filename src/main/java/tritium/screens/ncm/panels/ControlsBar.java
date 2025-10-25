package tritium.screens.ncm.panels;

import org.lwjglx.input.Mouse;
import tritium.ncm.music.AudioPlayer;
import tritium.ncm.music.CloudMusic;
import tritium.management.FontManager;
import tritium.rendering.ui.widgets.*;
import tritium.screens.ncm.FuckPussyPanel;
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

        bg.setBeforeRenderCallback(() -> {
            bg.setMargin(0);

            bg.setColor(0xFF1D1D1D);
            bg.setAlpha(.95f);
        });

        RoundedImageWidget playingCover = new RoundedImageWidget(() -> {
            if (CloudMusic.currentlyPlaying == null)
                return null;

            return MusicInfoWidget.getMusicCover(CloudMusic.currentlyPlaying);
        }, 0 , 0, 0, 0);

        this.addChild(playingCover);
        playingCover.fadeIn();
        playingCover.setLinearFilter(true);

        playingCover.setBeforeRenderCallback(() -> {
            playingCover.setMargin(5);
            playingCover.setBounds(playingCover.getHeight(), playingCover.getHeight());
            playingCover.setRadius(2);
        });

        playingCover.setOnClickCallback((relativeX, relativeY, mouseButton) -> {
            if (CloudMusic.currentlyPlaying != null) {
                NCMScreen.getInstance().fuckPussyPanel = new FuckPussyPanel(CloudMusic.currentlyPlaying);
            }

            return true;
        });

        LabelWidget lblMusicName = new LabelWidget(() -> CloudMusic.currentlyPlaying == null ? "未在播放" : CloudMusic.currentlyPlaying.getName(), FontManager.pf14bold);
        this.addChild(lblMusicName);

        lblMusicName.setBeforeRenderCallback(() -> {
            lblMusicName.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            lblMusicName.centerVertically();
            lblMusicName.setPosition(playingCover.getRelativeX() + playingCover.getWidth() + 4, lblMusicName.getRelativeY() - lblMusicName.getHeight() * .5 - 2);
        });
        lblMusicName.setClickable(false);

        LabelWidget lblMusicArtist = new LabelWidget(() -> CloudMusic.currentlyPlaying == null ? "无" : CloudMusic.currentlyPlaying.getArtistsName() + " - " + CloudMusic.currentlyPlaying.getAlbumName(), FontManager.pf14bold);
        this.addChild(lblMusicArtist);

        lblMusicArtist.setBeforeRenderCallback(() -> {
            lblMusicArtist.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
            lblMusicArtist.centerVertically();
            lblMusicArtist.setPosition(playingCover.getRelativeX() + playingCover.getWidth() + 4, lblMusicArtist.getRelativeY() + lblMusicArtist.getHeight() * .5 + 2);
        });

        lblMusicArtist.setClickable(false);

        double buttonsYOffset = -4;

        IconWidget playPause = new IconWidget("B", FontManager.icon30, 0, 0, 20, 20);

        this.addChild(playPause);

        playPause.setBeforeRenderCallback(() -> {

            playPause.center();
            playPause.setPosition(playPause.getRelativeX(), playPause.getRelativeY() + buttonsYOffset);

            if (CloudMusic.player == null || CloudMusic.player.isPausing()) {
                playPause.setIcon("B");
            } else {
                playPause.setIcon("A");
            }

            playPause.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        playPause.setOnClickCallback((x, y, i) -> {

            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null) {
                if (CloudMusic.player.isPausing())
                    CloudMusic.player.unpause();
                else
                    CloudMusic.player.pause();

            }

            return true;
        });

        IconWidget prev = new IconWidget("H", FontManager.icon30, 0, 0, 20, 20);

        this.addChild(prev);

        prev.setOnClickCallback((x, y, i) -> {
            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                CloudMusic.prev();

            return true;
        });

        prev.setBeforeRenderCallback(() -> {
            prev.center();
            prev.setPosition(prev.getRelativeX() - 20 - prev.getWidth() * .5, prev.getRelativeY() + buttonsYOffset);

            prev.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        IconWidget next = new IconWidget("E", FontManager.icon30, 0, 0, 20, 20);
        this.addChild(next);

        next.setOnClickCallback((x, y, i) -> {

            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                CloudMusic.next();

            return true;
        });

        next.setBeforeRenderCallback(() -> {

            next.center();
            next.setPosition(next.getRelativeX() + next.getWidth() * .5 + 20, next.getRelativeY() + buttonsYOffset);

            next.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        RoundedRectWidget progressBarBg = new RoundedRectWidget() {

            boolean prevMouse = false;

            @Override
            public void onRender(double mouseX, double mouseY, int dWheel) {
                super.onRender(mouseX, mouseY, dWheel);

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
                        FuckPussyPanel.resetProgress(progress);
                    }
                }
            }
        };

        this.addChild(progressBarBg);

        progressBarBg.setBeforeRenderCallback(() -> {
            progressBarBg.setColor(Color.GRAY);
            progressBarBg.setRadius(1);
            progressBarBg.setBounds(135, 3);
            progressBarBg.center();
            progressBarBg.setPosition(progressBarBg.getRelativeX(), progressBarBg.getRelativeY() + 8);
        });

        RoundedRectWidget progressBar = new RoundedRectWidget();

        progressBarBg.addChild(progressBar);
        progressBar.setColor(-1);
        progressBar.setWidth(0);

        progressBar.setBeforeRenderCallback(() -> {

            progressBar.setMargin(0);

            AudioPlayer player = CloudMusic.player;
            if (player == null)
                return;

            float perc = player.getCurrentTimeMillis() / player.getTotalTimeMillis();
            progressBar.setWidth(perc * progressBarBg.getWidth());
            progressBar.setRadius(perc);
        });

        progressBar.setClickable(false);

        LabelWidget lblCurTime = new LabelWidget(() -> CloudMusic.player == null ? "00:00" : formatDuration(CloudMusic.player.getCurrentTimeMillis()), FontManager.pf12);
        this.addChild(lblCurTime);

        lblCurTime.setBeforeRenderCallback(() -> {
            lblCurTime.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
            lblCurTime.setPosition(progressBarBg.getRelativeX() - lblCurTime.getWidth() - 4, progressBarBg.getRelativeY() + progressBarBg.getHeight() * .5 - lblCurTime.getHeight() * .5);
        });
        lblCurTime.setClickable(false);

        LabelWidget lblRemainingTime = new LabelWidget(() -> CloudMusic.player == null ? "00:00" : "-" + formatDuration(CloudMusic.player.getTotalTimeMillis() - CloudMusic.player.getCurrentTimeMillis()), FontManager.pf12);
        this.addChild(lblRemainingTime);

        lblRemainingTime.setBeforeRenderCallback(() -> {
            lblRemainingTime.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
            lblRemainingTime.setPosition(progressBarBg.getRelativeX() + progressBarBg.getWidth() + 4, lblCurTime.getRelativeY());
        });
        lblRemainingTime.setClickable(false);
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
