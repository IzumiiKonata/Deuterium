package tritium.widget.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.ncmplayer.music.CloudMusic;
import tech.konata.ncmplayer.music.Quality;
import tech.konata.ncmplayer.music.dto.Music;
import tech.konata.ncmplayer.music.lyric.LyricLine;
import tritium.management.FontManager;
import tritium.rendering.Stencil;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.entities.impl.ScrollText;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.settings.BooleanSetting;
import tritium.settings.ModeSetting;
import tritium.settings.NumberSetting;
import tritium.widget.Widget;

import java.awt.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/2/16 14:19
 */
public class MusicInfoWidget extends Widget {

    public BooleanSetting turnComposerIntoLyric = new BooleanSetting("Turn Composer Into Lyric", false);
    public ModeSetting<Quality> quality = new ModeSetting<Quality>("Quality", Quality.STANDARD) {
        @Override
        public void onModeChanged(Quality before, Quality now) {

            CloudMusic.quality = now;

        }
    };
    public NumberSetting<Double> volume = new NumberSetting<Double>("Volume", 0.1, 0.0, 1.0, 0.01) {
        @Override
        public void onValueChanged(Double last, Double now) {
            if (CloudMusic.player != null)
                CloudMusic.player.setVolume(now.floatValue());
        }
    };

    public MusicInfoWidget() {
        super("Music");
    }

    float alpha = 0.0f;

    ScrollText musicName = new ScrollText();
    ScrollText artists = new ScrollText();

    public double downloadProgHeight = 0;
    public boolean downloading = false;
    public double downloadProgress = 0;
    public String downloadSpeed = "0 b/s";
    float downloadPanelAlpha = 0.0f;

    float musicBgAlpha = 0.0f;
    ITextureObject prevBlurredBg = null;
    ITextureObject prevBg = null;
    Music prevMusic = null;

    @Override
    public void onRender(boolean editing) {

        double width = 230;
        double height = 56;

        Music playingMusic = CloudMusic.currentlyPlaying;

        boolean playing = playingMusic != null && CloudMusic.player != null && !CloudMusic.player.isFinished();

        alpha = Interpolations.interpBezier(alpha, playing ? 1 : 0, playing ? 0.15f : 0.2f);

        this.downloadProgHeight = Interpolations.interpBezier(this.downloadProgHeight, this.downloading ? (playing ? 26 : -26) : 0, 0.2f);
        this.downloadPanelAlpha = Interpolations.interpBezier(this.downloadPanelAlpha, this.downloading ? 1.0f : 0.0f, 0.4f);

        if (playingMusic != null) {

            Location cover = getMusicCover(playingMusic);

            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(cover);

            double imgSpacing = 4;

            double imgX = this.getX() + imgSpacing;

            float y = (float) (this.getY() + downloadProgHeight);
            double imgY = y + imgSpacing;

            double imgSize = height - imgSpacing * 2;

            NORMAL.add(() -> {
                GlStateManager.pushMatrix();

                {

                    double posX = this.getX();
                    double posY = this.getY();

                    Location musicCoverBlured = MusicInfoWidget.getMusicCoverBlurred(CloudMusic.currentlyPlaying);

                    TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
                    ITextureObject texBg = textureManager.getTexture(musicCoverBlured);

                    if (texBg != null || prevBlurredBg != null) {

                        if (playingMusic != prevMusic) {
                            prevBlurredBg = prevMusic == null ? null : textureManager.getTexture(MusicInfoWidget.getMusicCoverBlurred(prevMusic));
                            prevBg = prevMusic == null ? null : textureManager.getTexture(MusicInfoWidget.getMusicCover(prevMusic));
                            prevMusic = playingMusic;
                            musicBgAlpha = 0.0f;
                        }

                        double v = (height) / width;

                        if (prevBlurredBg != null && musicBgAlpha < 0.99f) {
                            GlStateManager.bindTexture(prevBlurredBg.getGlTextureId());
                            prevBlurredBg.linearFilter();
                            this.roundedRectTextured(posX, posY, width, height + downloadProgHeight, 0, v, 1, v, 5.6, 1, alpha);

                        }

                        if (texBg != null) {
                            this.musicBgAlpha = Interpolations.interpBezier(this.musicBgAlpha, 1.0f, 0.3f);
                            GlStateManager.bindTexture(texBg.getGlTextureId());
                            texBg.linearFilter();
                            this.roundedRectTextured(posX, posY, width, height + downloadProgHeight, 0, .5 - v * .5, 1, v, 5.6, 1, this.musicBgAlpha * alpha);
//                        this.roundedRectTextured(posX + width, posY - width * .5 + (height) * .5, width, width, 5.6, 1, this.musicBgAlpha * alpha);
                        }

                    }
//                    this.roundedRect(posX, posY, width, height + downloadProgHeight, 5.6, 1, 0, 0, 0, this.alpha * .15f);

                }


                // render download panel

                if (this.downloading) {

                    double offsetY = this.getY() + imgSpacing;

                    CFontRenderer fr = FontManager.pf18bold;

                    fr.drawString("Downloading...", imgX, offsetY, new Color(1, 1, 1, downloadPanelAlpha).getRGB());
                    fr.drawString(downloadSpeed, imgX + width - imgSpacing * 2 - fr.getWidth(downloadSpeed), offsetY, new Color(1, 1, 1, downloadPanelAlpha).getRGB());

                    this.roundedRect(imgX, offsetY + fr.getHeight() + 4, width - imgSpacing * 2, 6, 2, 1, 1, 1, downloadPanelAlpha * 0.25f);

                    StencilClipManager.beginClip(() -> Rect.draw(imgX,offsetY + fr.getHeight() + 4, (width - imgSpacing * 2) * downloadProgress, 6, -1));

                    this.roundedRect(imgX, offsetY + fr.getHeight() + 4, width - imgSpacing * 2, 6, 2, 1, 1, 1, downloadPanelAlpha);

                    StencilClipManager.endClip();
                }

                if (prevBg != null) {
                    GlStateManager.bindTexture(prevBg.getGlTextureId());
                    prevBg.linearFilter();
                    double exp = 0;
                    this.roundedRectTextured(imgX - exp, imgY - exp, imgSize + exp * 2, imgSize + exp * 2, 4, alpha);
                }

                if (texture != null) {
                    GlStateManager.bindTexture(texture.getGlTextureId());
                    texture.linearFilter();
                    double exp = 0;
                    this.roundedRectTextured(imgX - exp, imgY - exp, imgSize + exp * 2, imgSize + exp * 2, 4, this.musicBgAlpha * alpha);
                }

                String secondaryText = playingMusic.getArtistsName();

                if (this.turnComposerIntoLyric.getValue() && CloudMusic.player != null) {
                    LyricLine currentDisplaying = null, next = null;
                    List<LyricLine> allLyrics = MusicLyricsWidget.allLyrics;

                    for (int i = 0; i < allLyrics.size(); i++) {
                        LyricLine lyric = allLyrics.get(i);

                        if (lyric.getTimeStamp() > CloudMusic.player.getCurrentTimeMillis()) {
                            if (i > 0) {
                                currentDisplaying = allLyrics.get(i - 1);
                            }
                            next = allLyrics.get(i);
                            break;
                        } else if (i == allLyrics.size() - 1) {
                            currentDisplaying = allLyrics.get(i);
                        }
                    }

                    if (currentDisplaying != null) {
                        secondaryText = currentDisplaying.getLyric();
                        artists.setWaitTime(100L);
                        artists.setOneShot(true);

                        if (next != null) {
                            artists.anim.setDuration(Duration.ofMillis(next.timeStamp - currentDisplaying.timeStamp - 500));
                        } else {
                            artists.anim.setDuration(Duration.ofMillis((long) (CloudMusic.player.getCurrentTimeMillis() - currentDisplaying.timeStamp - 500)));
                        }

                    } else {
                        artists.setWaitTime(2000L);
                        artists.setOneShot(false);
                        artists.anim.setDuration(Duration.ofMillis(0));
                    }
                } else {
                    artists.setWaitTime(2000L);
                    artists.setOneShot(false);
                    artists.anim.setDuration(Duration.ofMillis(0));
                }

                double offsetY = imgY;
                double progressBarWidth = width - (imgSize + imgSpacing * 3.25);

                String name1 = playingMusic.getName();

                musicName.render(FontManager.pf25bold, name1, imgX + imgSize + imgSpacing, offsetY + 3, progressBarWidth, new Color(1f, 1f, 1f, alpha).getRGB());

                double progressBarOffsetY = y + height - imgSpacing - 3 - FontManager.pf14bold.getHeight() - 8;

                artists.render(FontManager.pf20, secondaryText, imgX + imgSize + imgSpacing, offsetY + FontManager.pf25bold.getHeight() + (progressBarOffsetY - (offsetY + 2 + FontManager.pf25bold.getHeight())) * 0.5 - FontManager.pf20bold.getHeight() * 0.5 + 2, progressBarWidth, new Color(1f, 1f, 1f, alpha * 0.8f).getRGB());

                this.roundedRect(imgX + imgSize + imgSpacing, progressBarOffsetY, progressBarWidth, 5, 1, 1f, 1f, 1f, alpha * 0.3f);

                if (CloudMusic.player != null) {
                    StencilClipManager.beginClip(() -> {
                        Rect.draw(imgX + imgSize + imgSpacing, progressBarOffsetY, (progressBarWidth) * ((double) CloudMusic.player.getCurrentTimeMillis() / CloudMusic.player.getTotalTimeMillis()), 6, -1);
                    });
                    this.roundedRect(imgX + imgSize + imgSpacing, progressBarOffsetY, progressBarWidth, 5, 1, 233, 233, 233, (int) (alpha * 255));
                    StencilClipManager.endClip();

                    int cMin = (int) (CloudMusic.player.getCurrentTimeSeconds() / 60);
                    int cSec = (int) (CloudMusic.player.getCurrentTimeSeconds() - cMin * 60);
                    String currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                    int tMin = (int) (CloudMusic.player.getTotalTimeSeconds() / 60);
                    int tSec = (int) (CloudMusic.player.getTotalTimeSeconds() - tMin * 60);
                    String totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                    int textColor = RenderSystem.hexColor(255, 255, 255, (int) (alpha * 128));
                    double playbackTimeY = progressBarOffsetY + 9;
                    FontManager.pf14bold.drawString(currentTime, imgX + imgSize + imgSpacing, playbackTimeY, textColor);
                    FontManager.pf14bold.drawString(totalTime, imgX + imgSize + imgSpacing + progressBarWidth - FontManager.pf14bold.getStringWidth(totalTime) - 1, playbackTimeY, textColor);

                }

                GlStateManager.popMatrix();
            });

        }

        this.setWidth((float) width);
        this.setHeight((float) (height + downloadProgHeight));
    }

    public static Location getMusicCover(Music music) {
        return Location.of("tritium/textures/MusicCover" + music.getId() + ".png");
    }

    public static Location getMusicCoverBlurred(Music music) {
        return Location.of("tritium/textures/MusicCoverBlurred" + music.getId() + ".png");
    }

    public static Location getMusicCoverSmall(Music music) {
        return Location.of("tritium/textures/MusicCoverSmall" + music.getId() + ".png");
    }

}
