package tritium.screens.ncm.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tritium.management.FontManager;
import tritium.ncm.api.CloudMusicApi;
import tritium.ncm.music.dto.PlayList;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.texture.Textures;
import tritium.rendering.ui.AbstractWidget;
import tritium.rendering.ui.container.ScrollPanel;
import tritium.rendering.ui.widgets.LabelWidget;
import tritium.rendering.ui.widgets.RoundedImageWidget;
import tritium.screens.ncm.NCMPanel;
import tritium.screens.ncm.NCMScreen;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author IzumiiKonata
 * Date: 2025/11/4 22:30
 */
public class HomePanel extends NCMPanel {

    public HomePanel() {
        super();
    }

    static final ArrayList<PlayList> playLists = new ArrayList<>();

    @Override
    public void onInit() {

        if (!playLists.isEmpty()) {
            layout();
        } else {
            MultiThreadingUtil.runAsync(() -> {
                JsonObject jObj = CloudMusicApi.recommendResource().toJsonObject();

                if (!jObj.isJsonObject()) {
                    return;
                }

                JsonElement codeElement = jObj.get("code");

                if (codeElement == null || !codeElement.isJsonPrimitive() || codeElement.getAsDouble() != 200) {
                    return;
                }

                JsonArray recommend = jObj.getAsJsonArray("recommend");

                for (JsonElement element : recommend) {
                    if (!element.isJsonObject()) {
                        continue;
                    }

                    JsonObject playList = element.getAsJsonObject();
                    playLists.add(new PlayList(playList));
                }

                layout();
            });
        }

    }

    public ScrollPanel scrollPanel;

    private void layout() {
        LabelWidget lblWelcome = new LabelWidget("欢迎来到 Tritium Music!", FontManager.pf25bold);

        this.addChild(lblWelcome);

        int margin = 12;

        lblWelcome.setBeforeRenderCallback(() -> {
            lblWelcome.setPosition(margin, margin);
            lblWelcome.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        LabelWidget lblRecommendations = new LabelWidget("推荐歌单", FontManager.pf14bold);

        this.addChild(lblRecommendations);

        lblRecommendations.setBeforeRenderCallback(() -> {
            lblRecommendations.setPosition(margin, lblWelcome.getRelativeY() + lblWelcome.getHeight() + 2 + margin * .5 - lblRecommendations.getHeight() * .5);
            lblRecommendations.setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT));
        });

        scrollPanel = new ScrollPanel();

//        RectWidget rw = new RectWidget();
        this.addChild(scrollPanel);

        scrollPanel.setSpacing(margin);

        scrollPanel
                .setAlignment(ScrollPanel.Alignment.VERTICAL_WITH_HORIZONTAL_FILL)
                .setBeforeRenderCallback(() -> {
                    scrollPanel.setMargin(margin);
                    scrollPanel.setBounds(scrollPanel.getRelativeX(), scrollPanel.getRelativeY() + lblWelcome.getHeight() + margin, scrollPanel.getWidth(), scrollPanel.getHeight() - lblWelcome.getHeight() - margin);
                });

        playLists.forEach(pl -> scrollPanel.addChild(new PlaylistWidget(pl)));

    }

    private static class PlaylistWidget extends AbstractWidget<PlaylistWidget> {

        @Getter
        private final PlayList playList;

        double emphasizeAnim = 0;

        public PlaylistWidget(PlayList playList) {
            this.playList = playList;

            double size = 100;
            double emphasizeAnimMax = 5;

            this.setBounds(size + emphasizeAnimMax, size + emphasizeAnimMax + 8);

            RoundedImageWidget cover = new RoundedImageWidget(this::getCoverLocation, 0, 0, size, size);

            this.addChild(cover);

            cover
                    .setClickable(false)
                    .fadeIn()
                    .setLinearFilter(true)
                    .setBeforeRenderCallback(() -> {

                        this.emphasizeAnim = Interpolations.interpBezier(this.emphasizeAnim, cover.isHovering() ? emphasizeAnimMax : 0, .2f);

                        cover
                                .setBounds(size + this.emphasizeAnim)
                                .setRadius(4)
                                .center();
                    });

            this.loadCover();

            CFontRenderer pf14bold = FontManager.pf14bold;
            LabelWidget lblName = new LabelWidget(() -> String.join("\n", pf14bold.fitWidth(playList.name, size)), pf14bold);

            this.addChild(lblName);

            lblName
                    .setClickable(false)
                    .setBeforeRenderCallback(() -> {
                        lblName
                                .setColor(NCMScreen.getColor(NCMScreen.ColorType.PRIMARY_TEXT))
                                .setPosition(cover.getRelativeX(), cover.getRelativeY() + cover.getHeight() + 4);
                    });

            this.setOnClickCallback((relativeX, relativeY, mouseButton) -> {

                if (mouseButton == 0) {
                    NCMScreen.getInstance().setCurrentPanel(new PlaylistPanel(playList));
                }

                return true;
            });

        }


        @Override
        public void onRender(double mouseX, double mouseY, int dWheel) {

        }

        private void loadCover() {

            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            Location coverLoc = this.getCoverLocation();
            if (textureManager.getTexture(coverLoc) != null)
                return;

            MultiThreadingUtil.runAsync(() -> {
                try (InputStream inputStream = HttpUtils.downloadStream(playList.coverUrl + "?param=256y256")) {
                    if (inputStream != null) {
                        NativeBackedImage img = NativeBackedImage.make(inputStream);
                        AsyncGLContext.submit(() -> {
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

        private Location getCoverLocation() {
            return Location.of("tritium/textures/playlist/" + this.playList.id + "/cover.png");
        }

    }

}
