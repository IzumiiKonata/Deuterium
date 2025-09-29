package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ThemeManager;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.dto.Music;
import tech.konata.phosphate.rendering.Stencil;
import tech.konata.phosphate.rendering.TexturedShadow;
import tech.konata.phosphate.rendering.animation.Interpolations;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.settings.BooleanSetting;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.settings.ModeSetting;
import tech.konata.phosphate.utils.music.lyric.LyricLine;
import tech.konata.phosphate.widget.Widget;

import java.awt.*;
import java.time.Duration;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 6/17/2023 10:03 AM
 */
public class MusicWidget extends Widget {

    double fftScale = 1;

    public final ModeSetting<Style> style = new ModeSetting<>("Style", Style.Style1);

    public enum Style {
        Style1,
        Style2
    }

    public BooleanSetting turnComposerIntoLyric = new BooleanSetting("Turn Author Into Lyrics", false, () -> this.style.getValue() == Style.Style1);

    public MusicWidget() {
        super("Music");
    }

    @Override
    public void onRender(boolean editing) {

        if (style.getValue() == Style.Style1) {

            if (GlobalSettings.HUD_STYLE.getValue() == GlobalSettings.HudStyle.Vanilla) {
                this.renderStyle1VanillaStyle();
            } else {
                this.renderStyle1();
            }

        }

        if (style.getValue() == Style.Style2) {
            this.renderStyle2();
        }

    }

    ScrollText musicName = new ScrollText();
    ScrollText artists = new ScrollText();

    float musicBgAlpha = 0.0f;
    ITextureObject prevBlurredBg = null;
    ITextureObject prevBg = null;
    Music prevMusic = null;

    private void renderStyle1() {
        double width = 230;
        double height = 56;

        Music playingMusic = CloudMusic.currentlyPlaying;

        boolean playing = playingMusic != null && CloudMusic.player != null && !CloudMusic.player.isFinished();

        alpha = Interpolations.interpBezier(alpha, playing ? 1 : 0, playing ? 0.15f : 0.2f);

        if (playingMusic != null) {

            Location cover = getMusicCover(playingMusic);

            ITextureObject texture = mc.getTextureManager().getTexture(cover);

            double imgSpacing = 4;

            double imgX = this.getX() + imgSpacing;
            double imgY = this.getY() + imgSpacing;

            double imgSize = height - imgSpacing * 2;

            NORMAL.add(() -> {

                GlStateManager.pushMatrix();

                this.doScale();

                {
                    double posX = this.getX();
                    double posY = this.getY();

                    Location musicCoverBlured = MusicWidget.getMusicCoverBlurred(CloudMusic.currentlyPlaying);

                    TextureManager textureManager = mc.getTextureManager();
                    ITextureObject texBg = textureManager.getTexture(musicCoverBlured);

                    if (texBg != null || prevBlurredBg != null) {

                        if (playingMusic != prevMusic) {
                            prevBlurredBg = prevMusic == null ? null : textureManager.getTexture(MusicWidget.getMusicCoverBlurred(prevMusic));
                            prevBg = prevMusic == null ? null : textureManager.getTexture(MusicWidget.getMusicCover(prevMusic));
                            prevMusic = playingMusic;
                            musicBgAlpha = 0.0f;
                        }

                        double v = (height + 17) / width;

                        double radius = 8;
                        if (prevBlurredBg != null && musicBgAlpha < 0.99f) {
                            GlStateManager.bindTexture(prevBlurredBg.getGlTextureId());
                            RenderSystem.linearFilter();
                            this.roundedRectTextured(posX, posY, width, height, 0, v, 1, v, radius, alpha);

                        }

                        if (texBg != null) {
                            this.musicBgAlpha = Interpolations.interpBezier(this.musicBgAlpha, 1.0f, 0.3f);
                            GlStateManager.bindTexture(texBg.getGlTextureId());
                            RenderSystem.linearFilter();
                            this.roundedRectTextured(posX, posY, width, height, 0, .5 - v * .5, 1, v, radius, this.musicBgAlpha * alpha);
//                        RenderSystem.roundedRectTextured(posX + width, posY - width * .5 + (height + 17) * .5, width, width, 5.6, 1, this.musicBgAlpha * alpha);
                        }

                    }
                }

                this.roundedRect(getX(), getY(), width, height, 8, new Color(0, 0, 0, alpha * .15f));

                if (prevBg != null && musicBgAlpha <= .95f) {
                    GlStateManager.bindTexture(prevBg.getGlTextureId());
                    RenderSystem.linearFilter();
                    double exp = 0;
                    this.roundedRectTextured(imgX - exp, imgY - exp, imgSize + exp * 2, imgSize + exp * 2, 5, alpha);
                }

                if (texture != null) {
                    GlStateManager.bindTexture(texture.getGlTextureId());
                    RenderSystem.linearFilter();
                    double exp = 0;
                    this.roundedRectTextured(imgX - exp, imgY - exp, imgSize + exp * 2, imgSize + exp * 2, 5, this.musicBgAlpha * alpha);
                }

//                roundedRect(this.getX(), this.getY(), width, height, 10, ThemeManager.getAsColor(ThemeManager.ThemeColor.Surface, (int) (255 * alpha)));

//                mc.getTextureManager().bindTexture(cover);
//                roundedRectTextured(imgX, imgY, imgSize, imgSize, 8, alpha);

                String secondaryText = playingMusic.getArtistsName();

                if (turnComposerIntoLyric.getValue()) {
                    LyricLine currentDisplaying = null, next = null;
                    List<LyricLine> allLyrics = MusicLyrics.allLyrics;

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
                            artists.anim.setDuration(Duration.ofMillis(0));
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
                double progressBarWidth = width - (imgSize + imgSpacing * 3.5);

                String name1 = playingMusic.getName();

//                    if (playingMusic.getTranslatedName() != null)
//                        name1 = name1 + EnumChatFormatting.GRAY + " (" + playingMusic.getTranslatedName() + ")";

                musicName.render(FontManager.pf25bold, name1, imgX + imgSize + imgSpacing, offsetY + 2, progressBarWidth, hexColor(255, 255, 255, (int) (alpha * 255)));

                double progressBarOffsetY = this.getY() + height - imgSpacing - 3 - FontManager.pf14bold.getHeight() - 2 - 5;

                artists.render(FontManager.pf20, secondaryText, imgX + imgSize + imgSpacing, offsetY + 2 + FontManager.pf25bold.getHeight() + (progressBarOffsetY - (offsetY + 2 + FontManager.pf25bold.getHeight())) * 0.5 - FontManager.pf20.getHeight() * 0.5, progressBarWidth, hexColor(255, 255, 255, (int) (alpha * 255 * .8f)));

                roundedRect(imgX + imgSize + imgSpacing, progressBarOffsetY, progressBarWidth, 5, 1, ThemeManager.getAsColor(ThemeManager.ThemeColor.OnSurface, (int) (alpha * 255 * .3f)));

                if (CloudMusic.player != null) {

                    Stencil.write();
                    Rect.draw(imgX + imgSize + imgSpacing, progressBarOffsetY, (progressBarWidth) * ((double) CloudMusic.player.getCurrentTimeMillis() / CloudMusic.player.getTotalTimeMillis()), 6, -1, Rect.RectType.EXPAND);
                    Stencil.erase();
                    roundedRectAccentColor(imgX + imgSize + imgSpacing, progressBarOffsetY, progressBarWidth, 5, 1, (int) (alpha * 255));
                    Stencil.dispose();

                    int cMin = CloudMusic.player.getCurrentTimeSeconds() / 60;
                    int cSec = (CloudMusic.player.getCurrentTimeSeconds() - (CloudMusic.player.getCurrentTimeSeconds() / 60) * 60);
                    String currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                    int tMin = CloudMusic.player.getTotalTimeSeconds() / 60;
                    int tSec = (CloudMusic.player.getTotalTimeSeconds() - (CloudMusic.player.getTotalTimeSeconds() / 60) * 60);
                    String totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                    int textColor = hexColor(255, 255, 255, (int) (alpha * 128));
                    FontManager.pf14bold.drawString(currentTime, imgX + imgSize + imgSpacing, progressBarOffsetY + 7, textColor);
                    FontManager.pf14bold.drawString(totalTime, imgX + imgSize + imgSpacing + progressBarWidth - FontManager.pf14bold.getStringWidth(totalTime), progressBarOffsetY + 7, textColor);

                }

                GlStateManager.popMatrix();
            });
        }

        this.setWidth(width);
        this.setHeight(height);
    }

    private void renderStyle1VanillaStyle() {
        double width = 190;
        double height = 56;

        Music playingMusic = CloudMusic.currentlyPlaying;

        boolean playing = playingMusic != null && CloudMusic.player != null/* && CloudMusic.player.player.getStatus() != MediaPlayer.Status.STOPPED*/;

        alpha = Interpolations.interpBezier(alpha * 255, playing ? 255 : 0, playing ? 0.15f : 0.2f) * RenderSystem.DIVIDE_BY_255;

        if (playingMusic != null) {

            Location cover = MusicWidget.getMusicCover(playingMusic);

            if (mc.getTextureManager().getTexture(cover) != null) {

                double imgSpacing = 4;

                double imgX = this.getX() + imgSpacing;
                double imgY = this.getY() + imgSpacing;

                double imgSize = height - imgSpacing * 2;

                NORMAL.add(() -> {

                    GlStateManager.pushMatrix();

                    this.doScale();

                    Rect.draw(this.getX(), this.getY(), width, height, hexColor(0, 0, 0, (int) (100 * alpha)), Rect.RectType.EXPAND);

                    mc.getTextureManager().bindTexture(cover);
                    GlStateManager.color(1, 1, 1, alpha);
                    Image.draw(imgX, imgY, imgSize, imgSize, Image.Type.NoColor);
//                    roundedRectTextured(, 8, simpleStyleAlpha);

                    String secondaryText = playingMusic.getArtistsName();

                    if (turnComposerIntoLyric.getValue()) {
                        LyricLine currentDisplaying = null, next = null;
                        List<LyricLine> allLyrics = MusicLyrics.allLyrics;

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
                                artists.anim.setDuration(Duration.ofMillis(0));
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
                    double progressBarWidth = width - (imgSize + imgSpacing * 3);
                    musicName.render(FontManager.pf25bold, playingMusic.getName().replaceAll("\n", " "), imgX + imgSize + imgSpacing, offsetY + 2, progressBarWidth, hexColor(255, 255, 255, (int) (alpha * 255)));

                    double progressBarOffsetY = this.getY() + height - imgSpacing - 3 - FontManager.pf14bold.getHeight() - 2 - 5;

                    artists.render(FontManager.pf20, secondaryText, imgX + imgSize + imgSpacing, offsetY + 2 + FontManager.pf25bold.getHeight() + (progressBarOffsetY - (offsetY + 2 + FontManager.pf25bold.getHeight())) * 0.5 - FontManager.pf20.getHeight() * 0.5, progressBarWidth, hexColor(255, 255, 255, (int) (alpha * 200)));

                    Rect.draw(imgX + imgSize + imgSpacing, progressBarOffsetY, progressBarWidth, 5, ThemeManager.get(ThemeManager.ThemeColor.OnSurface, (int) (alpha * 160)), Rect.RectType.EXPAND);

                    if (CloudMusic.player != null) {

                        Rect.draw(imgX + imgSize + imgSpacing, progressBarOffsetY, (progressBarWidth) * ((double) CloudMusic.player.getCurrentTimeMillis() / CloudMusic.player.getTotalTimeMillis()), 5, hexColor(255, 255, 255, (int) (alpha * 255)), Rect.RectType.EXPAND);

                        int cMin = CloudMusic.player.getCurrentTimeSeconds() / 60;
                        int cSec = (CloudMusic.player.getCurrentTimeSeconds() - (CloudMusic.player.getCurrentTimeSeconds() / 60) * 60);
                        String currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                        int tMin = CloudMusic.player.getTotalTimeSeconds() / 60;
                        int tSec = (CloudMusic.player.getTotalTimeSeconds() - (CloudMusic.player.getTotalTimeSeconds() / 60) * 60);
                        String totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                        int textColor = hexColor(255, 255, 255, (int) (alpha * 125));
                        FontManager.pf14bold.drawString(currentTime, imgX + imgSize + imgSpacing, progressBarOffsetY + 7, textColor);
                        FontManager.pf14bold.drawString(totalTime, imgX + imgSize + imgSpacing + progressBarWidth - FontManager.pf14bold.getStringWidth(totalTime), progressBarOffsetY + 7, textColor);

                    }

                    GlStateManager.popMatrix();
                });
            }
        }

        this.setWidth(width);
        this.setHeight(height);
    }

    private void renderStyle2() {
        Music playingMusic = CloudMusic.currentlyPlaying;

        boolean playing = playingMusic != null && CloudMusic.player != null/* && CloudMusic.player.player.getStatus() != MediaPlayer.Status.STOPPED*/;

        alpha = Interpolations.interpBezier(alpha * 255, playing ? 255 : 0, playing ? 0.15f : 0.2f) * RenderSystem.DIVIDE_BY_255;

        double width = 190;
        double height = 56;

        if (playingMusic != null) {

            double posX = this.getX();
            double posY = this.getY();

            Location cover = MusicWidget.getMusicCover(playingMusic);

            if (mc.getTextureManager().getTexture(cover) != null) {


                double space = 4;
                double imgSize = 48;

                NORMAL.add(() -> {
                    GlStateManager.pushMatrix();

                    GlStateManager.color(1, 1, 1, alpha);
                    Image.drawLinear(cover, posX + space, posY + space, imgSize, imgSize, Image.Type.NoColor);
                    GlStateManager.disableAlpha();
                    TexturedShadow.drawShadow(posX + space, posY + space, imgSize, imgSize, alpha);

                    GlStateManager.popMatrix();
                });

                BLOOM.add(() -> {
                    FontManager.pf25bold.drawString(playingMusic.getName().replaceAll("\n", " "), posX + space * 2 + imgSize, posY + space, RenderSystem.hexColor(255, 255, 255, Math.min((int) (alpha * 255), 180)));
                });

                width = Math.max(190, space * 3 + imgSize + Math.max(FontManager.pf25bold.getStringWidth(playingMusic.getName().replaceAll("\n", " ")), FontManager.pf20.getStringWidth(playingMusic.getArtistsName())));


                NORMAL.add(() -> {

                    FontManager.pf25bold.drawString(playingMusic.getName().replaceAll("\n", " "), posX + space * 2 + imgSize, posY + space, RenderSystem.hexColor(255, 255, 255, (int) (alpha * 255)));
                    FontManager.pf20.drawString(playingMusic.getArtistsName(), posX + space * 2 + imgSize, posY + space + FontManager.pf25bold.getHeight(), RenderSystem.hexColor(255, 255, 255, (int) (alpha * 125)));


                    if (CloudMusic.player != null) {
                        int cMin = CloudMusic.player.getCurrentTimeSeconds() / 60;
                        int cSec = (CloudMusic.player.getCurrentTimeSeconds() - (CloudMusic.player.getCurrentTimeSeconds() / 60) * 60);
                        String currentTime = (cMin < 10 ? "0" + cMin : cMin) + ":" + (cSec < 10 ? "0" + cSec : cSec);
                        int tMin = CloudMusic.player.getTotalTimeSeconds() / 60;
                        int tSec = (CloudMusic.player.getTotalTimeSeconds() - (CloudMusic.player.getTotalTimeSeconds() / 60) * 60);
                        String totalTime = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                        FontManager.pf20.drawString(currentTime + " - " + totalTime, posX + space * 2 + imgSize, posY + space + imgSize - FontManager.pf20.getHeight(), RenderSystem.hexColor(255, 255, 255, (int) (alpha * 125)));
                    }
                });


            }


        }

        this.setWidth(width);
        this.setHeight(height);
    }

    float alpha = 0.0f;


    public static Location getMusicCover(Music music) {
        return Location.of(Phosphate.NAME + "/textures/MusicCover" + music.getId() + ".png");
    }

    public static Location getMusicCoverBlurred(Music music) {
        return Location.of(Phosphate.NAME + "/textures/MusicCoverBlur" + music.getId() + ".png");
    }


    private int cRange(int c) {
        if (c < 0) {
            c = 0;
        }

        if (c > 255) {
            c = 255;
        }

        return c;
    }
}
