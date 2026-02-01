package tritium.rendering.music;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeBackedImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Location;
import tritium.management.FontManager;
import tritium.ncm.music.CloudMusic;
import tritium.rendering.texture.Textures;
import tritium.settings.ClientSettings;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/1/22 12:28
 */
public abstract class PVRenderer {

    // for debugging
    protected double x, y;

    protected abstract void onInit();

    private boolean initialized = false;

    public void initRenderer() {
        this.preloadTextures.clear();

        this.onInit();

        this.preloadTextures.forEach(t -> MultiThreadingUtil.runAsync(() -> {
            NativeBackedImage make;
            try {
                make = NativeBackedImage.make(Minecraft.getMinecraft().mcDefaultResourcePack.getInputStream(t));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            Textures.triggerLoad(t, new DynamicTexture(make));
        }));
    }

    /**
     * 渲染。
     * @param playBackTime 歌曲当前播放进度 (毫秒), 已减去 由 waitTime() 返回的空白时间。
     * @param musicID 歌曲ID.
     */
    public abstract void onRender(float playBackTime, long musicID);

    public void render() {

        if (!initialized) {
            initialized = true;
            this.initRenderer();
        }

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
        debug("Beat (4x): " + (int) this.beatCount(playBackTime, this.getMillisPerBeat() * 0.25));
    }

    public void doScale() {
//        GlStateManager.translate(0, RenderSystem.getHeight(), 0);
//        GlStateManager.scale(ModuleManager.hud.pvScale.getValue(), ModuleManager.hud.pvScale.getValue(), 1);
//        GlStateManager.translate(0, -RenderSystem.getHeight(), 0);
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

        if (!ClientSettings.DEBUG_MODE.getValue())
            return;

        FontManager.pf25.drawString(text, x, y, -1);
        y += FontManager.pf25.getHeight() + 2;
    }

    List<Location> preloadTextures = new ArrayList<>();

    @SneakyThrows
    public Location[] loadTextureFrom(String prefix, String suffix, int from, int to) {
        Location[] array = new Location[to - from + 1];

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        for (int i = 0; i < to - from + 1; i++) {
            array[i] = Location.of("tritium/textures/pv/" + prefix + (i + from) + suffix);

            if (textureManager.getTexture(array[i]) != null)
                textureManager.deleteTexture(array[i]);

            preloadTextures.add(array[i]);
        }

        return array;
    }

    @SneakyThrows
    public Location loadTexture(String path) {

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        Location p = Location.of("tritium/textures/pv/" + path);

        if (textureManager.getTexture(p) != null)
            textureManager.deleteTexture(p);

        preloadTextures.add(p);

        return p;
    }
}
