package tritium.screens.clickgui.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.ui.widgets.ImageWidget;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RectWidget;
import tritium.screens.ClickGui;
import tritium.widget.impl.MusicInfoWidget;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author IzumiiKonata
 * Date 2025/10/4 9:18
 */
public class MusicRect extends RectWidget {

    private PlayList playlist;
    private Music music;

    public MusicRect(PlayList list, Music music, Supplier<Integer> colorSupplier) {
        this.playlist = list;
        this.music = music;

        this.setBounds(0, 24);

        this.setBeforeRenderCallback(() -> {
            this.setColor(isHovering() ? ClickGui.getColor(22) : colorSupplier.get());
        });

        this.setOnClickCallback((rx, ry, i) -> {

            if (i == 0) {
                List<Music> musics = playlist.getMusics();
                CloudMusic.play(musics, musics.indexOf(music));
            }

            return true;
        });

        RectWidget imgBg = new RectWidget();
        this.addChild(imgBg);
        imgBg.setMargin(2);
        imgBg.setBounds(imgBg.getHeight(), imgBg.getHeight());
        imgBg.setColor(Color.GRAY);
        imgBg.setClickable(false);

        ImageWidget cover = new ImageWidget(() -> {
            if (CloudMusic.profile == null)
                return null;

            Location coverLoc = MusicInfoWidget.getMusicCover(music);

            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            if (textureManager.getTexture(coverLoc) == null) {
                CloudMusic.loadMusicCover(music, true);
                return null;
            }

            return coverLoc;
        }, 0, 0, imgBg.getWidth(), imgBg.getHeight()) {

            float alpha = .0f;

            @Override
            public void onRender(double mouseX, double mouseY, int dWheel) {
                super.onRender(mouseX, mouseY, dWheel);
                this.setAlpha(alpha);

                alpha = Interpolations.interpBezier(alpha, this.getLocImg().get() != null ? 1f : 0f, 0.2f);
            }
        };
        imgBg.addChild(cover);
        cover.setClickable(false);

        LabelWidget nameLabel = new LabelWidget(() -> this.music.getName(), FontManager.pf16);
        this.addChild(nameLabel);
        nameLabel.setClickable(false);
        nameLabel.setBeforeRenderCallback(() -> {
            nameLabel.setColor(ClickGui.getColor(9));
            nameLabel.setPosition(6 + imgBg.getWidth(), 0);
            nameLabel.centerVertically();
            nameLabel.setMaxWidth(this.getWidth() - 6 - imgBg.getWidth() - 4);
        });
    }

    @Override
    public void onRender(double mouseX, double mouseY, int dWheel) {
        super.onRender(mouseX, mouseY, dWheel);
    }
}
