package tritium.screens.ncm.panels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import org.lwjgl.input.Keyboard;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.*;
import tritium.screens.ncm.NCMPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.json.JsonUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/17 18:42
 */
public class PlaylistPanel extends NCMPanel {

    public PlayList playList;

    public PlaylistPanel(PlayList playlist) {
        this.playList = playlist;
    }

    private TextFieldWidget tfSearch;
    private double tfOpenAnimation = 20;

    @Override
    public void onInit() {

        double musicsContainerOffsetY;

        if (!playList.isSearchMode()) {
            RoundedImageWidget cover = new RoundedImageWidget(this.getCoverLocation(), 0, 0, 0, 0);

            cover.setPosition(24, 24);
            cover.setBounds(128, 128);
            cover.fadeIn();
            cover.setLinearFilter(true);

            this.addChild(cover);
            this.loadCover();

            cover.setBeforeRenderCallback(() -> {
                cover.setRadius(4);
            });

//        LabelWidget lblPlaylistName = new LabelWidget(playList.name, FontManager.pf);
            RoundedButtonWidget btnPlay = new RoundedButtonWidget("播放歌单", FontManager.pf16);
            this.addChild(btnPlay);

            btnPlay.setBeforeRenderCallback(() -> {
                btnPlay.setBounds(57, 17);
                btnPlay.setPosition(cover.getRelativeX() + cover.getWidth() + 12, cover.getRelativeY() + cover.getHeight() - btnPlay.getHeight());
                btnPlay.setRadius(3);
                btnPlay.setColor(0xFFd60017);
                btnPlay.setTextColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            });

            btnPlay.setOnClickCallback((relativeX, relativeY, mouseButton) -> {

                if (mouseButton == 0) {
                    playList.loadMusicsWithCallback(musics -> {
                        CloudMusic.play(musics, 0);
                    });
                }

                return true;
            });

            RoundedButtonWidget btnPlayRandomOrder = new RoundedButtonWidget("乱序播放歌单", FontManager.pf16);
            this.addChild(btnPlayRandomOrder);

            btnPlayRandomOrder.setBeforeRenderCallback(() -> {
                btnPlayRandomOrder.setBounds(57, 17);
                btnPlayRandomOrder.setPosition(cover.getRelativeX() + cover.getWidth() + 12 + btnPlay.getWidth() + 8, cover.getRelativeY() + cover.getHeight() - btnPlayRandomOrder.getHeight());
                btnPlayRandomOrder.setRadius(3);
                btnPlayRandomOrder.setColor(0xFFd60017);
                btnPlayRandomOrder.setTextColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            });

            btnPlayRandomOrder.setOnClickCallback((relativeX, relativeY, mouseButton) -> {

                if (mouseButton == 0) {
                    playList.loadMusicsWithCallback(musics -> {
                        ArrayList<Music> music = new ArrayList<>(musics);
                        Collections.shuffle(music);
                        CloudMusic.play(music, 0);
                    });
                }

                return true;
            });

            RoundedRectWidget searchBar = new RoundedRectWidget();
            this.addChild(searchBar);

            searchBar.setOnClickCallback((relativeX, relativeY, mouseButton) -> {

                if (mouseButton == 0) {
                    if (!this.tfSearch.isFocused()) {
                        this.tfSearch.setFocused(true);
                        this.tfSearch.getTextField().lmbPressed = true;
                    }
                }

                return true;
            });

            searchBar.setBeforeRenderCallback(() -> {
                tfOpenAnimation = Interpolations.interpBezier(tfOpenAnimation, this.tfSearch.isFocused() ? 80 : 20, .3f);
                searchBar.setAlpha(1f);
                searchBar.setColor(0xFF5E5E5E);
                searchBar.setWidth(tfOpenAnimation);
                searchBar.setHeight(btnPlayRandomOrder.getHeight());
                searchBar.setRadius(7);
                searchBar.setPosition(btnPlayRandomOrder.getRelativeX() + btnPlayRandomOrder.getWidth() + 8, btnPlayRandomOrder.getRelativeY());
            });

            RoundedRectWidget searchBarBg = new RoundedRectWidget();
            searchBar.addChild(searchBarBg);
            searchBarBg.setClickable(false);

            searchBarBg.setBeforeRenderCallback(() -> {
                searchBarBg.setMargin(.5);
                searchBarBg.setAlpha(.6f);
                searchBar.setColor(0xFF292727);
                searchBarBg.setRadius(searchBar.getRadius() - .5);
            });

            LabelWidget lblSearchIcon = new LabelWidget("K", FontManager.music18);
            searchBar.addChild(lblSearchIcon);
            lblSearchIcon.setClickable(false);

            lblSearchIcon.setBeforeRenderCallback(() -> {
                lblSearchIcon.setColor(hexColor(100, 100, 100));
                lblSearchIcon.centerVertically();
                lblSearchIcon.setPosition(lblSearchIcon.getRelativeY(), lblSearchIcon.getRelativeY());
            });

            this.tfSearch = new TextFieldWidget(FontManager.pf14bold);
            searchBar.addChild(tfSearch);

            this.tfSearch.setOnKeyTypedCallback((character, keyCode) -> {
                if (this.tfSearch.isFocused()) {
                    if (keyCode == Keyboard.KEY_ESCAPE)
                        this.tfSearch.setFocused(false);


                    return true;
                }

                return false;
            });

            tfSearch.setBeforeRenderCallback(() -> {
                tfSearch.drawUnderline(false);
                tfSearch.setMargin(2);
                double xSpacing = lblSearchIcon.getRelativeX() + lblSearchIcon.getWidth() + 4;
                tfSearch.setBounds(xSpacing, tfSearch.getRelativeY(), tfSearch.getWidth() - xSpacing, tfSearch.getHeight());
                tfSearch.setColor(this.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                tfSearch.setDisabledTextColor(RenderSystem.reAlpha(this.getColor(NCMScreen.ColorType.PRIMARY_TEXT), .4f));
            });

            RoundedImageWidget creatorAvatar = new RoundedImageWidget(this.getUserAvatarLocation(), 0, 0, 0, 0);
            this.addChild(creatorAvatar);
            creatorAvatar.fadeIn();
            creatorAvatar.setLinearFilter(true);

            this.loadAvatar();

            creatorAvatar.setBeforeRenderCallback(() -> {
                creatorAvatar.setBounds(16, 16);
                creatorAvatar.setPosition(cover.getRelativeX() + cover.getWidth() + 12, btnPlay.getRelativeY() - 6 - creatorAvatar.getHeight());
                creatorAvatar.setRadius(7.25);
            });

            LabelWidget lblCreator = new LabelWidget(playList.getCreator().getName(), FontManager.pf16bold);
            this.addChild(lblCreator);

            lblCreator.setBeforeRenderCallback(() -> {
                lblCreator.setPosition(creatorAvatar.getRelativeX() + creatorAvatar.getWidth() + 4, creatorAvatar.getRelativeY() + creatorAvatar.getHeight() * .5 - lblCreator.getHeight() * .5);
                lblCreator.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            });

            LabelWidget lblPlaylistInfo = new LabelWidget(() -> this.getPlayListInfo(), FontManager.pf12);
            this.addChild(lblPlaylistInfo);

            lblPlaylistInfo.setBeforeRenderCallback(() -> {
                lblPlaylistInfo.setPosition(cover.getRelativeX() + cover.getWidth() + 12, creatorAvatar.getRelativeY() - 8 - lblPlaylistInfo.getHeight());
                lblPlaylistInfo.setColor(NCMScreen.getColor(NCMScreen.ColorType.SECONDARY_TEXT));
            });

            LabelWidget lblPlaylistName = new LabelWidget(playList.getName(), FontManager.pf32);
            this.addChild(lblPlaylistName);

            lblPlaylistName.setBeforeRenderCallback(() -> {
                lblPlaylistName.setPosition(cover.getRelativeX() + cover.getWidth() + 12, lblPlaylistInfo.getRelativeY() - 4 - lblPlaylistName.getHeight());
                lblPlaylistName.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
            });

            musicsContainerOffsetY = cover.getRelativeY() + cover.getHeight() + 24;
        } else {
            musicsContainerOffsetY = 20;
        }

        Panel rwMusicsContainer = new Panel();

        this.addChild(rwMusicsContainer);

        rwMusicsContainer.setBeforeRenderCallback(() -> {
            rwMusicsContainer.setBounds(this.getWidth() - 36, this.getHeight() - (musicsContainerOffsetY));
            rwMusicsContainer.centerHorizontally();
            rwMusicsContainer.setPosition(rwMusicsContainer.getRelativeX(), musicsContainerOffsetY);
        });

        ScrollPanel musicsPanel = new ScrollPanel();

        rwMusicsContainer.addChild(musicsPanel);
        musicsPanel.setSpacing(0);

        musicsPanel.setBeforeRenderCallback(() -> {
            musicsPanel.setMargin(0);
        });

        playList.loadMusicsWithCallback(musics -> {
            musicsPanel.addChild(musics.stream().map(music -> new MusicWidget(music, playList, playList.getMusics().indexOf(music))).collect(Collectors.toList()));
        });

        if (this.tfSearch != null) {
            this.tfSearch.setTextChangedCallback(text -> {
                if (text.isEmpty()) {
                    musicsPanel.getChildren().forEach(child -> child.setHidden(false));
                } else {
                    for (AbstractWidget<?> child : musicsPanel.getChildren()) {
                        if (child instanceof MusicWidget widget) {

                            if (
                                    widget.music.getName().toLowerCase().contains(text.toLowerCase()) ||
                                    widget.music.getArtists().stream().anyMatch(artist -> artist != null && artist.getName() != null && artist.getName().toLowerCase().contains(text.toLowerCase())) ||
                                    (widget.music.getAlbum() != null && widget.music.getAlbum().getName() != null && widget.music.getAlbum().getName().toLowerCase().contains(text.toLowerCase()))
                            ) {
                                widget.setHidden(false);
                            } else {
                                widget.setHidden(true);
                            }

                        }
                    }
                }

            });
        }
    }

    private String formatDuration(long totalMillis) {
        long totalSeconds = totalMillis / 1000;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(String.format("%02d时", hours));
        }

        if (minutes > 0) {
            sb.append(String.format("%02d分", minutes));
        }

        sb.append(String.format("%02d秒", seconds));

        return sb.toString();
    }

    private String getPlayListInfo() {
        if (!playList.musicsLoaded)
            return "";

        List<Music> musics = playList.musics;
        if (musics.isEmpty())
            return playList.getCount() + "首歌曲";

        return musics.size() + "首歌曲 · " + this.formatDuration(musics.stream().mapToLong(Music::getDuration).sum());
    }

    private void loadCover() {

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location coverLoc = this.getCoverLocation();
        if (textureManager.getTexture(coverLoc) != null)
            return;

        MultiThreadingUtil.runAsync(() -> {
            try (InputStream inputStream = HttpUtils.downloadStream(playList.getCoverUrl() + "?param=256y256")) {
                if (inputStream != null) {
                    NativeBackedImage img = NativeBackedImage.make(inputStream);
                    MultiThreadingUtil.runAsync(() -> {
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

    private void loadAvatar() {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location avatarLoc = this.getUserAvatarLocation();
        if (textureManager.getTexture(avatarLoc) != null)
            return;
        MultiThreadingUtil.runAsync(() -> {
            try (InputStream inputStream = HttpUtils.downloadStream(playList.getCreator().getAvatarUrl() + "?param=32y32")) {
                if (inputStream != null) {
                    NativeBackedImage img = NativeBackedImage.make(inputStream);
                    MultiThreadingUtil.runAsync(() -> {
                        if (textureManager.getTexture(avatarLoc) != null) {
                            textureManager.deleteTexture(avatarLoc);
                        }
                        Textures.loadTexture(avatarLoc, img);
                        img.close();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Location getCoverLocation() {
        return Location.of("tritium/textures/playlist/" + this.playList.getId() + "/cover.png");
    }

    private Location getUserAvatarLocation() {
        return Location.of("tritium/textures/users/" + this.playList.getCreator().getId() + "/avatar.png");
    }
}
