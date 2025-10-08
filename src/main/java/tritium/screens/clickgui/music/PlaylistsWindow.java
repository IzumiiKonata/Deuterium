package tritium.screens.clickgui.music;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.commons.ncm.OptionsUtil;
import tech.konata.ncmplayer.music.AudioPlayer;
import tech.konata.ncmplayer.music.CloudMusic;
import tech.konata.ncmplayer.music.dto.Music;
import tech.konata.ncmplayer.music.dto.PlayList;
import tech.konata.ncmplayer.music.dto.User;
import tritium.management.FontManager;
import tritium.management.WidgetsManager;
import tritium.module.Module;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.IconWidget;
import tritium.rendering.ui.widgets.ImageWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.category.CategoriesWindow;
import tritium.utils.i18n.Localizable;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.widget.impl.MusicInfoWidget;
import tritium.widget.impl.MusicLyricsWidget;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date 2025/10/3 10:23
 */
public class PlaylistsWindow extends Window {

    @Getter
    RectWidget baseRect = new RectWidget();

    public LoginRenderer loginRenderer = null;
    boolean avatarLoaded = false;

    boolean panelDbg = false;

    @Getter
    @Setter
    public PlayList lastOnSetting;
    @Getter
    @Setter
    public PlayList onSetting;

    @Override
    public void init() {
        this.baseRect.getChildren().clear();

        this.baseRect.setBounds(150, 300);
        this.baseRect.setBeforeRenderCallback(() -> {
            CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
            this.baseRect.setPosition(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth(), categoriesWindow.getTopRect().getY());
            this.baseRect.setColor(ClickGui.getColor(3));
        });

        panelDbg = true;
        this.genNickNamePanel();
        this.genPlayListsPanel();
        this.genControlsPanel();
    }

    private AbstractWidget<?> genBaseContainer() {
        AbstractWidget<?> base = this.panelDbg ? new RectWidget() : new Panel();

//        if (base instanceof RectWidget)
//            ((RectWidget) base).setColor(0xff0090ff);

        this.baseRect.addChild(base);

        return base;
    }

    private void genNickNamePanel() {

        AbstractWidget<?> base = this.genBaseContainer();

        base.setBeforeRenderCallback(() -> {
            base.setColor(ClickGui.getColor(11));
        });

        base.setMargin(4);
        base.setHeight(24);

        // nickname
        {
            LabelWidget nickname = new LabelWidget(() -> {
                User profile = CloudMusic.profile;
                if (profile == null)
                    return "";

                return profile.getName();
            }, FontManager.pf16);

            nickname.setMaxWidth(40);

            nickname.setBeforeRenderCallback(() -> {
                nickname.setPosition(nickname.getParentWidth() - nickname.getWidth(), 0);
                nickname.centerVertically();
                nickname.setColor(ClickGui.getColor(20));
            });

            base.addChild(nickname);

            // avatar
            {
                ImageWidget avatar = new ImageWidget(() -> {
                    if (CloudMusic.profile == null)
                        return null;

                    Location avatarLoc = Location.of("tritium/textures/ncm_avatar_" + CloudMusic.profile.getName() + ".png");

                    TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
                    if (textureManager.getTexture(avatarLoc) == null) {

                        if (!avatarLoaded) {
                            avatarLoaded = true;
                            MultiThreadingUtil.runAsync(() -> {
                                try (InputStream inputStream = HttpUtils.downloadStream(CloudMusic.profile.getAvatarUrl() + "?param=32y32")) {
                                    if (inputStream != null) {
                                        BufferedImage img = ImageIO.read(inputStream);
                                        AsyncGLContext.submit(() -> {
                                            if (textureManager.getTexture(avatarLoc) != null) {
                                                textureManager.deleteTexture(avatarLoc);
                                            }
                                            Textures.loadTexture(avatarLoc, img);
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        return null;
                    }

                    return avatarLoc;
                }, 0, 0, 16, 16);

                avatar.setBeforeRenderCallback(() -> {
                    avatar.setPosition(avatar.getParentWidth() - nickname.getWidth() - 4 - avatar.getWidth(), 4);
                });

                base.addChild(avatar);
            }
        }
    }

    private void genPlayListsPanel() {
        AbstractWidget<?> base = this.genBaseContainer();

        base.setMargin(4);
        base.setHeight(300 - 32 - 24 - 24);

        base.setPosition(base.getRelativeX(), 32);

        ScrollPanel playListsPanel = new ScrollPanel();

        base.addChild(playListsPanel);
        playListsPanel.setMargin(0);
        playListsPanel.setSpacing(0);

        if (CloudMusic.playLists == null) {
            return;
        }

        List<PlayList> playLists = CloudMusic.playLists;
        for (int i = 0; i < playLists.size(); i++) {
            PlayList playList = playLists.get(i);
            Supplier<Integer> colorSupl = i % 2 == 0 ? () -> ClickGui.getColor(11) : () -> ClickGui.getColor(12);

            PlaylistRect rect = new PlaylistRect(playList, colorSupl);
            playListsPanel.addChild(rect);
            rect.setWidth(playListsPanel.getWidth());
        }

    }

    private void genControlsPanel() {

        AbstractWidget<?> base = this.genBaseContainer();

        base.setBeforeRenderCallback(() -> {
            base.setColor(ClickGui.getColor(11));
        });

        base.setMargin(4);

        base.setHeight(40);

        base.setPosition(base.getRelativeX(), base.getParentHeight() - 4 - base.getHeight());

        RectWidget progressBarBg = new RectWidget() {

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
                        CloudMusic.player.setPlaybackTime((float) (percent * CloudMusic.player.getTotalTimeMillis()));
                        MusicLyricsWidget.quickResetProgress((float) (percent * CloudMusic.player.getTotalTimeMillis()));
                    }
                }
            }
        };

        base.addChild(progressBarBg);

        progressBarBg.setMargin(0);
        progressBarBg.setColor(Color.GRAY);
        progressBarBg.setHeight(3);

        RectWidget progressBar = new RectWidget();

        progressBarBg.addChild(progressBar);
        progressBar.setMargin(0);
        progressBar.setColor(0xff0090ff);
        progressBar.setWidth(0);

        progressBar.setBeforeRenderCallback(() -> {
            AudioPlayer player = CloudMusic.player;
            if (player == null)
                return;

            progressBar.setWidth((player.getCurrentTimeMillis() / player.getTotalTimeMillis()) * progressBarBg.getWidth());
        });

        progressBar.setClickable(false);

//        RectWidget coverBg = new RectWidget();
//
//        base.addChild(coverBg);
//        coverBg.setMargin(4);
//        coverBg.setPosition(2, 5);
//        coverBg.setWidth(coverBg.getHeight() + .5);
//        coverBg.setHeight(coverBg.getWidth());
//        coverBg.setBounds(coverBg.getHeight(), coverBg.getHeight());
//        coverBg.setColor(Color.GRAY);
//
//        ImageWidget cover = new ImageWidget(() -> {
//            Music currentlyPlaying = CloudMusic.currentlyPlaying;
//
//            if (currentlyPlaying == null)
//                return null;
//
//            return MusicInfoWidget.getMusicCover(currentlyPlaying);
//        }, 0, 0, 0, 0);
//
//        coverBg.addChild(cover);
//        cover.setMargin(0);


//        Localizable lNotPlaying = Localizable.of("panel.music.notplaying");
//        LabelWidget musicName = new LabelWidget(() -> {
//            if (CloudMusic.currentlyPlaying == null)
//                return lNotPlaying.get();
//
//            return CloudMusic.currentlyPlaying.getName();
//        }, FontManager.pf16);
//
//        base.addChild(musicName);
//        musicName.setClickable(false);
//        musicName.setMaxWidth(base.getWidth() * .5);
//        musicName.setBeforeRenderCallback(() -> {
////            musicName.setPosition(coverBg.getRelativeX() + coverBg.getWidth() + 4, coverBg.getRelativeY());
//            musicName.centerHorizontally();
//            musicName.setPosition(musicName.getRelativeX(), base.getHeight() - musicName.getHeight() - 2);
//            musicName.setColor(ClickGui.getColor(9));
//        });

        IconWidget playPause = new IconWidget("B", FontManager.icon30, 0, 0, 20, 20);

        base.addChild(playPause);
        playPause.center();
        playPause.setPosition(playPause.getRelativeX(), playPause.getRelativeY() + progressBar.getHeight() * .5);

        playPause.setBeforeRenderCallback(() -> {
            if (CloudMusic.player == null || CloudMusic.player.isPausing()) {
                playPause.setIcon("B");
            } else {
                playPause.setIcon("A");
            }

            playPause.setColor(ClickGui.getColor(9));
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

        base.addChild(prev);
        prev.center();
        prev.setPosition(prev.getRelativeX() - 20 - prev.getWidth() * .5, prev.getRelativeY() + progressBar.getHeight() * .5);
        prev.setOnClickCallback((x, y, i) -> {
            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                CloudMusic.prev();

            return true;
        });

        prev.setBeforeRenderCallback(() -> {
            prev.setColor(ClickGui.getColor(9));
        });

        IconWidget next = new IconWidget("E", FontManager.icon30, 0, 0, 20, 20);
        base.addChild(next);
        next.center();
        next.setPosition(next.getRelativeX() + next.getWidth() * .5 + 20, next.getRelativeY() + progressBar.getHeight() * .5);
        next.setOnClickCallback((x, y, i) -> {
            if (CloudMusic.player != null && CloudMusic.currentlyPlaying != null)
                CloudMusic.next();

            return true;
        });

        next.setBeforeRenderCallback(() -> {
            next.setColor(ClickGui.getColor(9));
        });

    }

    @Override
    public void render(double mouseX, double mouseY) {
        if (ClickGui.getInstance().getCategoriesWindow().getSelectedCategoryIndex() != 2) {
            return;
        }

        this.baseRect.renderWidget(mouseX, mouseY, this.getDWheel());

        boolean loggedIn = !OptionsUtil.getCookie().isEmpty();

        if (!loggedIn && this.loginRenderer == null) {
            this.loginRenderer = new LoginRenderer();
        }

        if (this.loginRenderer != null) {
            this.doRenderLoginRenderer(mouseX, mouseY, this.baseRect.getX(), this.baseRect.getY(), this.baseRect.getWidth(), this.baseRect.getHeight());
        }

    }

    private void doRenderLoginRenderer(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        this.loginRenderer.render(mouseX, mouseY, posX, posY, width, height, this.baseRect.getAlpha());

        if (this.loginRenderer.canClose() && !OptionsUtil.getCookie().isEmpty()) {
            this.loginRenderer = null;
            CloudMusic.loadNCM(OptionsUtil.getCookie());

            this.init();
        }
    }

    @Override
    public void setAlpha(float alpha) {
        this.baseRect.setAlpha(alpha);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.baseRect.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_SPACE && CloudMusic.currentlyPlaying != null && CloudMusic.player != null && !CloudMusic.player.isFinished()) {

            if (CloudMusic.player.isPausing())
                CloudMusic.player.unpause();
            else
                CloudMusic.player.pause();
        }
    }
}
