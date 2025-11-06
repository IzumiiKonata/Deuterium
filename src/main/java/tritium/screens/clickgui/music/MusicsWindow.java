package tritium.screens.clickgui.music;

import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.Rect;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.screens.clickgui.Window;
import tritium.screens.clickgui.category.CategoriesWindow;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date 2025/10/4 9:11
 */
public class MusicsWindow extends Window {

    private boolean open;
    private double stencilWidth = 0;

    RectWidget baseRect = new RectWidget();
    ScrollPanel musicsPanel;

    @Override
    public void init() {
        this.baseRect.getChildren().clear();

        this.baseRect.setBounds(150, 300);
        this.baseRect.setBeforeRenderCallback(() -> {
            CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
            PlaylistsWindow playlistsWindow = ClickGui.getInstance().getPlaylistsWindow();
            this.baseRect.setPosition(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth() + playlistsWindow.getBaseRect().getWidth(), categoriesWindow.getTopRect().getY());
            this.baseRect.setColor(ClickGui.getColor(15));
        });

        LabelWidget back = new LabelWidget("-", FontManager.pf25bold);

        back.setColor(ClickGui.getColor(17));
        back.setPosition(4, 2);

        back.setBeforeRenderCallback(() -> {
            back.setColor(back.isHovering() ? ClickGui.getColor(18) : ClickGui.getColor(17));
        });

        back.setOnClickCallback((mouseX, mouseY, mouseButton) -> {

            if (mouseButton == 0) {
                PlaylistsWindow playlists = ClickGui.getInstance().getPlaylistsWindow();

                playlists.setLastOnSetting(playlists.getOnSetting());
                playlists.setOnSetting(null);
            }

            return true;
        });

        this.baseRect.addChild(back);

        LabelWidget lblLoading = new LabelWidget(() -> musicsPanel.getChildren().isEmpty() ? "Loading..." : "", FontManager.pf25);

        this.baseRect.addChild(lblLoading);

        lblLoading.setBeforeRenderCallback(() -> {
            lblLoading.center();
            lblLoading.setColor(ClickGui.getColor(17));
        });

        musicsPanel = new ScrollPanel();

        this.baseRect.addChild(musicsPanel);

        musicsPanel.setMargin(4);
        musicsPanel.setBounds(musicsPanel.getRelativeX(), musicsPanel.getRelativeY() + 16, musicsPanel.getWidth(), musicsPanel.getHeight() - 16);
        musicsPanel.setSpacing(0);
    }

    @Override
    public void render(double mouseX, double mouseY) {
        if (ClickGui.getInstance().getCategoriesWindow().getSelectedCategoryIndex() != 2) {
            return;
        }

        PlaylistsWindow playlists = ClickGui.getInstance().getPlaylistsWindow();

        open = (playlists.getOnSetting() != null) && (playlists.getOnSetting() == playlists.getLastOnSetting());

        this.stencilWidth = Interpolations.interpBezier(this.stencilWidth, open ? 150 : 0, 0.3f);


        if (!open && this.stencilWidth <= 1) {
            playlists.setLastOnSetting(playlists.getOnSetting());
            this.musicsPanel.targetScrollOffset = 0;
            this.musicsPanel.getChildren().clear();
        }

        CategoriesWindow categoriesWindow = ClickGui.getInstance().getCategoriesWindow();
        StencilClipManager.beginClip(() -> {
            Rect.draw(categoriesWindow.getTopRect().getX() + categoriesWindow.getTopRect().getWidth() + playlists.getBaseRect().getWidth(), this.baseRect.getY(), this.stencilWidth, this.baseRect.getHeight(), -1);
        });

        this.baseRect.renderWidget(mouseX, mouseY, this.getDWheel());

        if (playlists.getOnSetting() != null && this.musicsPanel.getChildren().isEmpty() && !playlists.getOnSetting().getMusics().isEmpty()) {
            this.musicsPanel.targetScrollOffset = 0;

            List<Music> musics = playlists.getOnSetting().getMusics();
            synchronized (musics) {
                for (int i = 0; i < musics.size(); i++) {
                    Music music = musics.get(i);
                    Supplier<Integer> colorSupl = i % 2 == 0 ? () -> ClickGui.getColor(11) : () -> ClickGui.getColor(12);

                    MusicRect musicRect = new MusicRect(playlists.getOnSetting(), music, colorSupl);
                    this.musicsPanel.addChild(musicRect);
                    musicRect.setWidth(this.musicsPanel.getWidth());
                }
            }
        }

        PlayList playlist = playlists.getLastOnSetting();
        if (playlist != null) {
            FontManager.pf16.drawString(playlist.name, this.baseRect.getX() + 16, this.baseRect.getY() + 7, RenderSystem.reAlpha(ClickGui.getColor(19), this.baseRect.getAlpha()));
//            StencilClipManager.endClip();
        }


        StencilClipManager.endClip();
    }

    @Override
    public void setAlpha(float alpha) {
        this.baseRect.setAlpha(alpha);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.baseRect.onMouseClickReceived(mouseX, mouseY, mouseButton);
    }
}
