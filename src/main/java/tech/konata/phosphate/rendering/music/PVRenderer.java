package tech.konata.phosphate.rendering.music;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MipmappedDynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.Location;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.rendering.async.AsyncGLContext;
import tech.konata.phosphate.rendering.rendersystem.RenderSystem;
import tech.konata.phosphate.rendering.texture.Textures;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.utils.music.CloudMusic;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/1/22 12:28
 */
public abstract class PVRenderer implements SharedRenderingConstants {

    // for debugging
    protected double x, y;

    protected final Minecraft mc = Minecraft.getMinecraft();

    protected abstract void onInit();

    public void initRenderer() {
        this.preloadTextures.clear();

        this.onInit();

        this.preloadTextures.forEach(t -> AsyncGLContext.submit(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                IResource iresource = mc.getResourceManager().getResource(t);
                Textures.triggerLoad(t, new MipmappedDynamicTexture(TextureUtil.readBufferedImage(iresource.getInputStream())));
            }
        }));
    }

    /**
     * 渲染。
     * @param playBackTime 歌曲当前播放进度 (毫秒), 已减去 由 waitTime() 返回的空白时间。
     * @param musicID 歌曲ID.
     */
    public abstract void onRender(float playBackTime, long musicID);

    public void render() {
        float currentTimeMillis = CloudMusic.player.getCurrentTimeMillis();
        long id = CloudMusic.currentlyPlaying.getId();

        float playBackTime = currentTimeMillis - this.waitTime(id);

        if (currentTimeMillis > this.waitTime(id)) {
            this.onRender(playBackTime, id);
        }

        x = 20;
        y = 300;
        debug("Beat: " + (int) this.beatCount(playBackTime));
        debug("Beat (2x): " + (int) this.beatCount(playBackTime, this.getMillisPerBeat() * 0.5));
    }

    public void doScale() {
        GlStateManager.translate(0, RenderSystem.getHeight(), 0);
        GlStateManager.scale(ModuleManager.hud.pvScale.getValue(), ModuleManager.hud.pvScale.getValue(), 1);
        GlStateManager.translate(0, -RenderSystem.getHeight(), 0);
    }

    public abstract double getBPM();

    public double getMillisPerBeat() {
        return 60000.0 / this.getBPM();
    }

    public double beatCount(float playBackTime, double millisPerBeat) {
        return playBackTime / millisPerBeat;
    }

    public double beatCount(float playBackTime) {
        return this.beatCount(playBackTime, this.getMillisPerBeat());
    }

    public abstract boolean isApplicable(long id);

    // 获取要渲染的歌曲的开头空白时间
    // 硬编码
    public abstract long waitTime(long id);

    protected void debug(String text) {

        if (!GlobalSettings.DEBUG_MODE.getValue())
            return;

        FontManager.pf25.drawString(text, x, y, -1);
        y += FontManager.pf25.getHeight() + 2;
    }

    List<Location> preloadTextures = new ArrayList<>();

    @SneakyThrows
    public Location[] loadTextureFrom(String path, String suffix, int from, int to) {
        Location[] array = new Location[to - from + 1];

        TextureManager textureManager = mc.getTextureManager();

        for (int i = 0; i < to - from + 1; i++) {
            array[i] = Location.of("Phosphate/textures/pv/" + path + (i + from) + suffix);

            if (textureManager.getTexture(array[i]) != null)
                textureManager.deleteTexture(array[i]);

            preloadTextures.add(array[i]);
        }

        return array;
    }

    @SneakyThrows
    public Location loadTexture(String path) {

        TextureManager textureManager = mc.getTextureManager();

        Location p = Location.of("Phosphate/textures/pv/" + path);

        if (textureManager.getTexture(p) != null)
            textureManager.deleteTexture(p);

        preloadTextures.add(p);

        return p;
    }
}
