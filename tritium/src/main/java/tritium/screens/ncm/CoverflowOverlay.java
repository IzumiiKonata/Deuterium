package tritium.screens.ncm;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.glu.Project;
import tritium.management.FontManager;
import tritium.management.ThemeManager;
import tritium.ncm.music.CloudMusic;
import tritium.ncm.music.dto.Album;
import tritium.ncm.music.dto.Music;
import tritium.ncm.music.dto.PlayList;
import tritium.rendering.Image;
import tritium.rendering.RGBA;
import tritium.rendering.Rect;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.entities.impl.TextField;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.shader.Shaders;
import tritium.rendering.texture.Textures;
import tritium.screens.BaseScreen;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.timing.Timer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/3/14 20:31
 */
public class CoverflowOverlay extends BaseScreen {

    @Getter
    private static final CoverflowOverlay instance = new CoverflowOverlay();

    public boolean closing = false;
    public float alpha = 0.0f;

    public void display() {
        closing = false;
    }

    Map<Album, List<Music>> albumList = new ConcurrentHashMap<>()/*, renderList = new CopyOnWriteArrayList<>()*/;
    Map<Album, AlbumRenderingData> albumRenderingData = new HashMap<>();
    Map<Music, MusicRenderingData> musicRenderingData = new HashMap<>();
    List<Album> renderList = new CopyOnWriteArrayList<>();

    boolean lmbPressed, rmbPressed;

    TextField textBox = new TextField(0, 0, 0, 0, 0);

    boolean reloadOnClosed = false;

    Timer clickResistTimer = new Timer();

    private class AlbumRenderingData {
        public boolean flipped, coverLoaded;
        public float rotateDeg = 0f;
        public double scale = .75, scrollTarget = 0, scrollOffset = 0;
    }

    private class MusicRenderingData {
        public float hoverAlpha = 0f;
    }

    @Override
    public void onGuiClosed() {

        if (this.reloadOnClosed) {
            this.albumList.clear();
            this.reloadOnClosed = false;
            mc.displayGuiScreen(NCMScreen.getInstance());
        }

    }

    private void loadAlbumData(List<PlayList> list) {
        MultiThreadingUtil.runAsync(() -> {

            for (PlayList pl : list) {

                if (pl == null)
                    continue;

                List<Music> musics = pl.getMusics();

                if (musics == null)
                    continue;

                for (Music m : musics) {

                    if (m == null) {
                        continue;
                    }

                    Album album = m.getAlbum();
                    if (album == null) {
                        continue;
                    }

                    List<Music> playLists = albumList.computeIfAbsent(album, k -> new CopyOnWriteArrayList<>());
                    playLists.add(m);
                }

            }

            renderList.addAll(this.albumList.keySet());
//            albumList.sort(Comparator.comparing(o -> o.name));
        });
    }

    @Override
    public void initGui() {

//        albumList.clear();

        if (!this.reloadOnClosed && this.albumList.isEmpty())
            this.loadAlbumData(CloudMusic.playLists);

    }

    private void setupProjectionTransformation() {
        double aspectRatio = RenderSystem.getWidth() / RenderSystem.getHeight();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(45.0f, (float) aspectRatio, 1.0F, 3000.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        // translate z
        GlStateManager.translate(0, 0, -200.0f);
        GlStateManager.scale(1, -1, 1);
    }

    public void stopProjectionTransformation() {
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    int index = 0;
    double scrollOffset = 0;

    @Override
    @SneakyThrows
    public void drawScreen(double mouseX, double mouseY) {

        alpha = Interpolations.interpBezier(alpha, closing ? .0f : 1f, 0.2f);

        if (alpha <= 0.05)
            return;

        if (!Mouse.isButtonDown(0) && lmbPressed)
            lmbPressed = false;

        if (!Mouse.isButtonDown(1) && rmbPressed)
            rmbPressed = false;

        Rect.draw(0, 0, RenderSystem.getWidth(), RenderSystem.getHeight(), RGBA.color(0, 0, 0, alpha * 0.5f));

        if (albumList.isEmpty())
            return;

        textBox.setPosition(8, 8);
        textBox.width = 240;
        textBox.height = 34;
        textBox.setFontRenderer(FontManager.pf40bold);
        textBox.setTextColor(ThemeManager.get(ThemeManager.ThemeColor.Text));
        textBox.setDisabledTextColour(Color.GRAY.getRGB());
        textBox.setPlaceholder("Search (Ctrl + F)");
        textBox.setCallback(text -> {
            renderList.clear();

            if (text.isEmpty()) {
                renderList.addAll(albumList.keySet());
            } else {
                for (Album album : albumList.keySet()) {
                    if (album.getName().toLowerCase().contains(text.toLowerCase()))
                        renderList.add(album);
                }

                for (List<Music> m : albumList.values()) {
                    for (Music music : m) {
                        if (music.getName().toLowerCase().contains(text.toLowerCase())) {
                            renderList.add(music.getAlbum());
                            break;
                        }
                        if (music.getTranslatedNames() != null) {
                            if (music.getTranslatedNames().toLowerCase().contains(text.toLowerCase())) {
                                renderList.add(music.getAlbum());
                            }
                        }
                    }
                }

                // distinct the list
                renderList = renderList.stream().distinct().collect(Collectors.toList());
            }
        });
//        textBox.yOffset = -4f;

        textBox.drawTextBox((int) mouseX, (int) mouseY);

        this.setupProjectionTransformation();

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.depthMask(false);
//        GlStateManager.disableDepth();
//        GlStateManager.clearDepth(1.0D);
//        GlStateManager.disableCull();

        double coverSize = 96;
        double spacing = 24;

        float rotDegTarget = 45;

        scrollOffset = Interpolations.interpBezier(scrollOffset, index * (coverSize * 0.5 + spacing), 0.2f);

        double offsetX = -coverSize * 0.5 - scrollOffset;

        int dWheel = Mouse.getDWheel();

        if (dWheel != 0 && !renderList.isEmpty() && !albumRenderingData.computeIfAbsent(renderList.get(index), k -> new AlbumRenderingData()).flipped) {

            int amount = 1;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                amount *= 5;

            if (dWheel > 0) {
                index -= amount;
            } else {
                index += amount;
            }

        }

        index = Math.max(0, Math.min(renderList.size() - 1, index));

        // 渲染 index 左边的
        for (int i = 0; i < index; i++) {

            Album al = renderList.get(i);

            if (index - i <= 7) {
                this.renderCoverImage(al, offsetX, coverSize, rotDegTarget, i, true, mouseX, mouseY, dWheel);
            }

            offsetX += coverSize * 0.5 + spacing;
        }

        offsetX = -coverSize * 0.5 + ((coverSize * 0.5 + spacing) * (renderList.size() - 1)) - scrollOffset;

        // 渲染 index 右边的
        for (int i = renderList.size() - 1; i >= index; i--) {
            Album al = renderList.get(i);

            if (i - index <= 7) {
                this.renderCoverImage(al, offsetX, coverSize, rotDegTarget, i, false, mouseX, mouseY, dWheel);
            }

            offsetX -= coverSize * 0.5 + spacing;
        }

//        offsetX = -coverSize * 0.5;
//        Album al = list.get(index);
//        this.renderCoverImage(al, offsetX, coverSize, rotDegTarget, index, false, mouseX, mouseY, dWheel);
//        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);

        this.stopProjectionTransformation();

    }

    private void renderCoverImage(Album al, double offsetX, double coverSize, float rotDegTarget, int i, boolean left, double mouseX, double mouseY, int dWheel) {

        TextureManager textureManager = mc.getTextureManager();

        ITextureObject texture = textureManager.getTexture(al.getCoverLocation());

        AlbumRenderingData renderingData = albumRenderingData.computeIfAbsent(al, k -> new AlbumRenderingData());

        if (texture == null && !renderingData.coverLoaded) {
            renderingData.coverLoaded = true;
            MultiThreadingUtil.runAsync(() -> {

                try {

                    int cSize = 512;
                    InputStream is = HttpUtils.get(al.getPicUrl() + "?param=" + cSize + "y" + cSize, null);

                    if (is == null)
                        return;

                    Textures.loadTextureAsyncly(al.getCoverLocation(), NativeBackedImage.make(is), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }

        double fovy = 45.0f;
        double aspectRatio = (RenderSystem.getWidth() / RenderSystem.getHeight());
        double translateZ = 200.0f; // 对应视图矩阵中的 offset z

        // 焦点
        double f = (1.0 / Math.tan(Math.toRadians(fovy) / 2.0));

        double paneWidth = (translateZ * aspectRatio) / f * 2;
        double paneHeight = translateZ / f * 2;

        mouseX = mouseX / RenderSystem.getWidth() * paneWidth - paneWidth * 0.5;
        mouseY = mouseY / RenderSystem.getHeight() * paneHeight - paneHeight * 0.5;

        // 喜欢我的魔法数字吗
        double fontScale = (1 / aspectRatio) * 0.618;

        GlStateManager.pushMatrix();

        GlStateManager.translate(offsetX + coverSize * 0.5, coverSize * 0.5, 0);

        if (index != i) {
            renderingData.rotateDeg = Interpolations.interpBezier(renderingData.rotateDeg, rotDegTarget * (left ? 1 : -1), 0.2f);
            renderingData.scale = Interpolations.interpBezier(renderingData.scale, 0.75, 0.2f);
            renderingData.flipped = false;
        } else {
            renderingData.rotateDeg = Interpolations.interpBezier(renderingData.rotateDeg, renderingData.flipped ? -180 : 0, renderingData.flipped ? 0.1f : 0.2f);
            renderingData.scale = Interpolations.interpBezier(renderingData.scale, 1.0, 0.2f);
        }

        // rotate
        GlStateManager.rotate(renderingData.rotateDeg, 0, 1, 0);
        GlStateManager.translate(-(offsetX + coverSize * 0.5), -coverSize * 0.5, 0);

        // scale
        GlStateManager.translate(offsetX + coverSize * 0.5, 0, 0);
        GlStateManager.scale(renderingData.scale, renderingData.scale, 1);
        GlStateManager.translate(-(offsetX + coverSize * 0.5), 0, 0);

        Rect.draw(offsetX, -coverSize * 0.5f, coverSize, coverSize, RGBA.color(128, 128, 128, 128));

        if (texture != null) {
            GlStateManager.bindTexture(texture.getGlTextureId());
            texture.linearFilter();
            Image.draw(offsetX, -coverSize * 0.5, coverSize, coverSize, Image.Type.Normal);

            // reflection
            Shaders.VF_FADEOUT.draw(offsetX, coverSize * 0.5, coverSize, coverSize, 0.5, 0.85f);

            if (renderingData.flipped || (renderingData.rotateDeg < -5 && index == i)) {
                // flip it
                GlStateManager.translate(offsetX + coverSize * 0.5, coverSize * 0.5, 0);
                GlStateManager.rotate(-180, 0, 1, 0);
                GlStateManager.translate(-(offsetX + coverSize * 0.5), -coverSize * 0.5, 0);

                double x = offsetX;
                double y = -coverSize * 0.5f;
                double width = coverSize;
                double height = coverSize;

                Image.drawLinearFlippedX(al.getCoverLocation(), x, y, width, height, Image.Type.Normal);
                Rect.draw(x, y, width, height, RGBA.color(0, 0, 0, 200));

                double imgSpacing = 1;
                double imgSize = 16;
                Image.draw(x + width - imgSize - imgSpacing, y + imgSpacing, imgSize, imgSize, Image.Type.Normal);

                CFontRenderer fr = FontManager.pf28bold;
                fr.drawString(fr.trim(al.getName(), (width - imgSpacing * 2 - 2 - imgSize) / fontScale), x + 2, y + 2, fontScale, -1);

                double contentSpacing = 2;

                double contentPaneX = x + contentSpacing;
                double contentPaneY = y + imgSpacing * 2 + imgSize + contentSpacing;
                double contentPaneWidth = width - contentSpacing * 2;
                double contentPaneHeight = height - (imgSpacing * 2 + imgSize + contentSpacing * 2);

                StencilClipManager.beginClip(() -> Rect.draw(contentPaneX, contentPaneY, contentPaneWidth, contentPaneHeight, -1));
                Rect.draw(contentPaneX, contentPaneY, contentPaneWidth, contentPaneHeight, RGBA.color(255, 255, 255, 20));

                double yAdd = 5;

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    yAdd *= 2;

                if (RenderSystem.isHovered(mouseX, mouseY, contentPaneX, contentPaneY, contentPaneWidth, contentPaneHeight) && dWheel != 0) {
                    if (dWheel > 0)
                        renderingData.scrollTarget -= yAdd;
                    else
                        renderingData.scrollTarget += yAdd;
                }

                renderingData.scrollTarget = Interpolations.interpBezier(renderingData.scrollTarget, 0, 0.4f);
                renderingData.scrollOffset = Interpolations.interpBezier(renderingData.scrollOffset, renderingData.scrollTarget, 1f);

                if (renderingData.scrollTarget < 0)
                    renderingData.scrollTarget = Interpolations.interpBezier(renderingData.scrollTarget, 0, 0.2f);

                double yOffset = contentPaneY - renderingData.scrollOffset;
                double entryHeight = fr.getHeight() * fontScale + 4;
                List<Music> musics = albumList.get(al);

                if (renderingData.scrollTarget > (musics.size() - 1) * entryHeight)
                    renderingData.scrollTarget = Interpolations.interpBezier(renderingData.scrollTarget, (musics.size() - 1) * entryHeight, 0.2f);

                fr = FontManager.pf25;

                for (int j = 0; j < musics.size(); j++) {
                    Music music = musics.get(j);

                    if ((j + 1) % 2 == 0) {
                        Rect.draw(contentPaneX, yOffset, contentPaneWidth, entryHeight, RGBA.color(0, 0, 0, 60));
                    }

                    fr.drawString((j + 1) + ".", contentPaneX + 2, yOffset + entryHeight * 0.5 - fr.getHeight() * 0.5 * fontScale, fontScale, -1);
                    fr.drawString(fr.trim(music.getName(), 58 / fontScale), contentPaneX + 4 + 12, yOffset + entryHeight * 0.5 - fr.getHeight() * 0.5 * fontScale, fontScale, -1);

                    long tMin = (music.getDuration() / 1000) / 60;
                    long tSec = ((music.getDuration() / 1000) - ((music.getDuration() / 1000) / 60) * 60);
                    String duration = (tMin < 10 ? "0" + tMin : tMin) + ":" + (tSec < 10 ? "0" + tSec : tSec);

                    fr.drawString(duration, contentPaneX + 4 + 74, yOffset + entryHeight * 0.5 - fr.getHeight() * 0.5 * fontScale, fontScale, -1);

                    MusicRenderingData musicData = musicRenderingData.computeIfAbsent(music, m -> new MusicRenderingData());

                    if (musicData.hoverAlpha > 0.05f) {
                        Rect.draw(contentPaneX, yOffset, contentPaneWidth, entryHeight, RGBA.color(1, 1, 1, musicData.hoverAlpha));
                    }

                    boolean hovered = RenderSystem.isHovered(mouseX, mouseY, contentPaneX, yOffset, contentPaneWidth, entryHeight);

                    musicData.hoverAlpha = Interpolations.interpBezier(musicData.hoverAlpha, hovered ? 0.2f : 0.0f, 0.2f);

                    if (hovered && Mouse.isButtonDown(0) && !lmbPressed && renderingData.rotateDeg < -145 && renderingData.flipped) {
                        lmbPressed = true;
                        CloudMusic.play(musics, musics.indexOf(music));
                    }

                    yOffset += entryHeight;
                }

                StencilClipManager.endClip();

            }
        }

        GlStateManager.popMatrix();

        if (index == i) {

            if (Mouse.isButtonDown(0) && !lmbPressed && this.clickResistTimer.isDelayed(250)) {

                boolean hovered = RenderSystem.isHovered(mouseX, mouseY, offsetX, -coverSize * 0.5, coverSize, coverSize);

                if (hovered && !renderingData.flipped) {
                    renderingData.flipped = true;
                }

                if (!hovered && renderingData.flipped) {
                    renderingData.flipped = false;
                }

                lmbPressed = true;
            }

//            OpenApiInstance.api.getRenderUtil().drawRect(mouseX, mouseY, 10, 10, Color.WHITE);

            this.stopProjectionTransformation();

            // mouseY = mouseY / RenderSystem.getHeight() * paneHeight - paneHeight * 0.5;

            CFontRenderer fr = FontManager.pf50bold;

            fr.drawCenteredStringWithShadow(al.getName(), RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5 + (coverSize - paneHeight * 0.225) / paneHeight * RenderSystem.getHeight(), -1);
//            fr.drawCenteredString(al.getA, RenderSystem.getWidth() * 0.5, RenderSystem.getHeight() * 0.5 + (coverSize - paneHeight * 0.25) / paneHeight * RenderSystem.getHeight() + fr.getHeight(), -1);

            this.setupProjectionTransformation();
        }

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        this.textBox.mouseClicked(mouseX, mouseY, button);
    }

    public void onKeyTyped(char typedChar, int keyCode) {

        if (keyCode == Keyboard.KEY_ESCAPE) {

            if (!renderList.isEmpty() && albumRenderingData.computeIfAbsent(renderList.get(index), a -> new AlbumRenderingData()).flipped) {
                albumRenderingData.get(renderList.get(index)).flipped = false;
                return;
            }

            if (this.textBox.isFocused()) {
                this.textBox.setFocused(false);
                return;
            }

            mc.displayGuiScreen(NCMScreen.getInstance());
            return;
        }

        if (keyCode == Keyboard.KEY_LEFT && !textBox.isFocused()) {

            if (index > 0)
                index --;

            return;
        }

        if (keyCode == Keyboard.KEY_RIGHT && !textBox.isFocused()) {

            if (index < renderList.size() - 1)
                index ++;

            return;
        }

        if (textBox.isFocused()) {
            this.textBox.textboxKeyTyped(typedChar, keyCode);
            return;
        }

        if (GuiScreen.isKeyComboCtrl(keyCode, Keyboard.KEY_F)) {
            this.textBox.setFocused(true);
            this.textBox.setCursorPositionEnd();
            this.textBox.setSelectionPos(0);
        }

    }

    public static CoverflowOverlay byPlaylist(PlayList playList) {

        CoverflowOverlay screen = getInstance();

        screen.reloadOnClosed = true;
        screen.albumList.clear();
        screen.renderList.clear();
        screen.loadAlbumData(Collections.singletonList(playList));

        screen.clickResistTimer.reset();

        return screen;
    }

}
