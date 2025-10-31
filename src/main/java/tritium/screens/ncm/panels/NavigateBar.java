package tritium.screens.ncm.panels;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.container.Panel;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.rendering.ui.widgets.RoundedImageWidget;
import tritium.rendering.ui.widgets.RoundedRectWidget;
import tritium.screens.ncm.NCMPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/16 22:00
 */
public class NavigateBar extends NCMPanel {

    RoundedRectWidget searchBar = new RoundedRectWidget();
    ScrollPanel playlistPanel = new ScrollPanel();

    public NavigateBar() {
        this.layout();
    }

    private void layout() {
        RectWidget bg = new RectWidget();
        this.addChild(bg);

        this.setBeforeRenderCallback(() -> {
            this.setBounds(NCMScreen.getInstance().getPanelWidth() * .15, NCMScreen.getInstance().getPanelHeight());
            this.setPosition(0, 0);

            bg.setMargin(0);
            bg.setColor(this.getColor(NCMScreen.ColorType.GENERIC_BACKGROUND));
            bg.setAlpha(0.9f);
        });

        this.addChild(searchBar);

        this.searchBar.setBeforeRenderCallback(() -> {
            searchBar.setAlpha(.2f);
            searchBar.setColor(0xFFFF1010);
            searchBar.setMargin(8);
            searchBar.setHeight(16);
            searchBar.setRadius(4);
        });

        this.addChild(playlistPanel);
        this.playlistPanel.setBeforeRenderCallback(() -> {
            this.playlistPanel.setMargin(0);
            this.playlistPanel.setPosition(this.playlistPanel.getRelativeX(), searchBar.getRelativeY() + searchBar.getHeight() + 8);
            this.playlistPanel.setBounds(this.playlistPanel.getWidth(), this.playlistPanel.getHeight() - searchBar.getHeight() - 16 - 32);
        });

        this.playlistPanel.setSpacing(4);

        LabelWidget lbl = new LabelWidget("Tritium Music", FontManager.pf14bold);
        lbl.setBeforeRenderCallback(() -> {
            lbl.setColor(Color.GRAY);
            lbl.setPosition(6, lbl.getRelativeY());
        });

        this.playlistPanel.addChild(lbl);

        List<PlayList> pl = CloudMusic.playLists;

        if (pl != null) {
            List<PlayList> playLists = pl.stream().filter(playList -> !playList.subscribed).collect(Collectors.toList());
            for (int i = 0; i < playLists.size(); i++) {
                PlayList playList = playLists.get(i);
                PlaylistItem item = new PlaylistItem(i == 0 ? "C" : "D", () -> Color.GRAY.getRGB(), () -> playList.name, () -> {
                    NCMScreen.getInstance().setCurrentPanel(new PlaylistPanel(playList));
                });

                this.playlistPanel.addChild(item);
            }
        }

        LabelWidget lblSubscribed = new LabelWidget("收藏歌单", FontManager.pf14bold);
        lblSubscribed.setBeforeRenderCallback(() -> {
            lblSubscribed.setColor(Color.GRAY);
            lblSubscribed.setPosition(6, lblSubscribed.getRelativeY());
        });

        this.playlistPanel.addChild(lblSubscribed);

        if (pl != null) {
            pl.stream().filter(playList -> playList.subscribed).forEach(playList -> {
                PlaylistItem item = new PlaylistItem("D", () -> Color.GRAY.getRGB(), () -> playList.name, () -> {
                    NCMScreen.getInstance().setCurrentPanel(new PlaylistPanel(playList));
                });

                this.playlistPanel.addChild(item);
            });
        }

        RoundedImageWidget creatorAvatar = new RoundedImageWidget(this.getUserAvatarLocation(), 0, 0, 0, 0);
        this.addChild(creatorAvatar);
        creatorAvatar.fadeIn();
        creatorAvatar.setLinearFilter(true);

        this.loadAvatar();

        creatorAvatar.setBeforeRenderCallback(() -> {
            creatorAvatar.setBounds(16, 16);
            creatorAvatar.setPosition(12, this.getHeight() - 8 - creatorAvatar.getHeight());
            creatorAvatar.setRadius(7.25);
        });

        LabelWidget lblCreator = new LabelWidget(() -> CloudMusic.profile == null ? "未登录" : CloudMusic.profile.name, FontManager.pf16bold);
        this.addChild(lblCreator);

        lblCreator.setBeforeRenderCallback(() -> {
            lblCreator.setPosition(creatorAvatar.getRelativeX() + creatorAvatar.getWidth() + 4, creatorAvatar.getRelativeY() + creatorAvatar.getHeight() * .5 - lblCreator.getHeight() * .5);
            lblCreator.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });
    }

    private void loadAvatar() {

        if (CloudMusic.profile == null) {
            return;
        }

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        Location avatarLoc = this.getUserAvatarLocation();
        if (textureManager.getTexture(avatarLoc) != null)
            return;

        MultiThreadingUtil.runAsync(() -> {
            try (InputStream inputStream = HttpUtils.downloadStream(CloudMusic.profile.avatarUrl + "?param=32y32")) {
                if (inputStream != null) {
                    NativeBackedImage img = NativeBackedImage.make(inputStream);
                    AsyncGLContext.submit(() -> {
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

    private Location getUserAvatarLocation() {

        if (CloudMusic.profile == null) {
            return null;
        }

        return Location.of("tritium/textures/users/" + CloudMusic.profile.id + "/avatar.png");
    }

    @Override
    public void onInit() {

    }

    private static class PlaylistItem extends Panel {

        String icon;
        Supplier<Integer> iconColorSupplier;
        Supplier<String> label;
        Runnable onClick;
        RoundedRectWidget bg = new RoundedRectWidget();

        @Getter
        @Setter
        boolean selected = false;

        public PlaylistItem(String icon, Supplier<Integer> iconColorSupplier, Supplier<String> label, Runnable onClick) {
            this.icon = icon;
            this.iconColorSupplier = iconColorSupplier;
            this.label = label;
            this.onClick = onClick;

            this.setBeforeRenderCallback(() -> {
                this.setBounds(this.getParentWidth(), 16);
                this.setPosition(4, this.getRelativeY());
            });

            bg.setClickable(false);

            this.addChild(bg);
            this.bg.setBeforeRenderCallback(() -> {
                bg.setMargin(0);
                bg.setHidden(!selected);
                bg.setColor(Color.BLACK);
                bg.setAlpha(selected ? 0.2f : 0f);
                bg.setRadius(4);
            });

            LabelWidget lblIcon = new LabelWidget(icon, FontManager.music16);
            this.addChild(lblIcon);
            lblIcon.setBeforeRenderCallback(() -> {
                lblIcon.setColor(iconColorSupplier.get());
                lblIcon.centerVertically();
                lblIcon.setPosition(8, lblIcon.getRelativeY() + .5);
            });

            lblIcon.setClickable(false);

            LabelWidget lbl = new LabelWidget(label, FontManager.pf14bold);
            this.addChild(lbl);

            lbl.setBeforeRenderCallback(() -> {
                lbl.centerVertically();
                lbl.setPosition(lblIcon.getRelativeX() + lblIcon.getWidth() + 4, lbl.getRelativeY());
                lbl.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
                lbl.setMaxWidth(this.getWidth() - 8 - lblIcon.getWidth() - 12);
            });

            lbl.setClickable(false);

            this.setOnClickCallback(((relativeX, relativeY, mouseButton) -> {

                if (mouseButton == 0) {
                    this.selected = true;
                    bg.setHidden(false);

                    this.onClick.run();

                    NCMScreen.getInstance().getPlaylistsPanel().playlistPanel.getChildren().stream()
                            .filter(it -> it instanceof PlaylistItem && it != this)
                            .forEach(it -> ((PlaylistItem) it).setSelected(false));
                }

                return true;
            }));
        }

        @Override
        public void onRender(double mouseX, double mouseY, int dWheel) {

        }

    }
}
