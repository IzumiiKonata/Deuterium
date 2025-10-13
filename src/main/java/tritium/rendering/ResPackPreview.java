package tritium.rendering;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Location;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;
import tritium.rendering.async.AsyncGLContext;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.timing.Timer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/10/12 21:37
 */
public class ResPackPreview {

    private final IResourcePack pack;
    private final String path;

    int frameWidth;
    int imgHeight;

    List<Frame> frames = new ArrayList<>();
    boolean isAnimated = false;
    Location locImg;
    Timer timer = new Timer();
    int curFrame = 0;

    public ResPackPreview(IResourcePack pack, BufferedImage img, String path) {
        this.pack = pack;
        this.path = path;
        this.frameWidth = img.getWidth();
        this.imgHeight = img.getHeight();

        AsyncGLContext.submit(() -> {
            this.locImg = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("ResourcePackPreview", new DynamicTexture(img));
        });
        this.serializeMetadata(img);
    }

    public void cleanUp() {
        AsyncGLContext.submit(() -> {
            ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(locImg);
            if (textureObj != null) {
                TextureUtil.deleteTexture(textureObj.getGlTextureId());
            }
        });
    }

    public void render(double x, double y, double width, double height) {

        if (this.locImg == null)
            return;

        if (!isAnimated) {
            Image.draw(this.locImg, x, y, width, height, Image.Type.Normal);
            return;
        }

        Frame frame = this.frames.get(curFrame);
        if (frame.generated) {
            Image.drawNearest(frame.generatedLoc, x, y, width, height, Image.Type.Normal);
        } else {
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            ITextureObject textureObj = Minecraft.getMinecraft().getTextureManager().getTexture(locImg);
            TextureUtils.bindTexture(textureObj.getGlTextureId());
            RenderSystem.nearestFilter();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            int v = frame.origFrameIndex * frameWidth;
            worldrenderer.pos(x,            y + height, 0.0D).tex(0, (double) (v + frameWidth) / imgHeight).endVertex();
            worldrenderer.pos(x + width, y + height, 0.0D).tex(1, (double) (v + frameWidth) / imgHeight).endVertex();
            worldrenderer.pos(x + width,    y, 0.0D)         .tex(1, (double) (v) / imgHeight).endVertex();
            worldrenderer.pos(x,               y, 0.0D)         .tex(0, (double) (v) / imgHeight).endVertex();
            tessellator.draw();
            GlStateManager.enableAlpha();
        }

        if (timer.isDelayed((long) (frame.frameTime * 50))) {
            timer.reset();
            curFrame++;
            if (curFrame >= frames.size()) {
                curFrame = 0;
            }
        }

    }

    public static boolean metadataHasAnimationFrames(InputStream is) {
        Gson gson = new Gson();

        JsonObject jObj = gson.fromJson(new InputStreamReader(is), JsonObject.class);

        if (!jObj.isJsonObject())
            return false;

        if (!jObj.has("animation")) {
            return false;
        }

        JsonElement animationElement = jObj.get("animation");

        if (!animationElement.isJsonObject()) {
            return false;
        }

        JsonObject animationObject = animationElement.getAsJsonObject();

        int frameTime = JsonUtils.getInt(animationObject, "frametime", 1);

        if (frameTime != 1) {
            if (frameTime < 1)
                frameTime = 1;
        }

        if (animationObject.has("frames")) {
            try {
                JsonArray framesArray = JsonUtils.getJsonArray(animationObject, "frames");

                for (int j = 0; j < framesArray.size(); ++j) {
                    JsonElement frameElement = framesArray.get(j);
                    Frame animationframe = parseAnimationFrame(j, frameTime, frameElement);

                    if (animationframe != null) {
                        return true;
                    }
                }
            } catch (ClassCastException classcastexception) {
                throw new JsonParseException("Invalid animation->frames: expected array, was " + animationObject.get("frames"), classcastexception);
            }
        }

        return false;
    }

    @SneakyThrows
    private void serializeMetadata(BufferedImage img) {
        InputStream is = null;

        try {
            Location location = Location.of(path + ".mcmeta");
            is = this.pack.getInputStream(location);
        } catch (Throwable ignored) {
            return;
        }

        if (is == null) {
            return;
        }

        Gson gson = new Gson();

        JsonObject jObj = gson.fromJson(new InputStreamReader(is), JsonObject.class);

        if (!jObj.isJsonObject())
            return;

        if (!jObj.has("animation")) {
            return;
        }

        JsonElement animationElement = jObj.get("animation");

        if (!animationElement.isJsonObject()) {
            return;
        }

        this.isAnimated = true;

        JsonObject animationObject = animationElement.getAsJsonObject();

        int frameTime = JsonUtils.getInt(animationObject, "frametime", 1);

        if (frameTime != 1) {
            if (frameTime < 1)
                frameTime = 1;
        }

        if (animationObject.has("frames")) {
            try {
                JsonArray framesArray = JsonUtils.getJsonArray(animationObject, "frames");

                for (int j = 0; j < framesArray.size(); ++j) {
                    JsonElement frameElement = framesArray.get(j);
                    Frame animationframe = this.parseAnimationFrame(j, frameTime, frameElement);

                    if (animationframe != null) {
                        frames.add(animationframe);
                    }
                }
            } catch (ClassCastException classcastexception) {
                throw new JsonParseException("Invalid animation->frames: expected array, was " + animationObject.get("frames"), classcastexception);
            }
        }

        boolean interpolate = JsonUtils.getBoolean(animationObject, "interpolate", false);
        if (interpolate) {
            this.generateInterpolatedFrames(img);
        }
    }

    private void generateInterpolatedFrames(BufferedImage image) {
        List<Frame> copy = new ArrayList<>(frames);
        this.frames.clear();

//        Graphics2D g2d = (Graphics2D) image.getGraphics();

        for (int i = 0; i < copy.size(); ++i) {
            Frame frame = copy.get(i);
            frame.frameTime *= 0.5;

            this.frames.add(frame);

            if (i < copy.size() - 1) {
                BufferedImage generated = new BufferedImage(image.getWidth(), image.getWidth(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) generated.getGraphics();

                g.setColor(new Color(255, 255, 255, 128));
                g.drawImage(
                        image,
                        0, 0,
                        image.getWidth(), image.getWidth(),
                        0, i * image.getWidth(),
                        image.getWidth(), i * image.getWidth() + image.getWidth(),
                        null
                );
                g.drawImage(
                        image,
                        0, 0,
                        image.getWidth(), image.getWidth(),
                        0, (i + 1) * image.getWidth(),
                        image.getWidth(), (i + 1) * image.getWidth() + image.getWidth(),
                        null
                );

                g.dispose();

                Frame gen = new Frame(-1, frame.frameTime);
                gen.generated = true;
                gen.generatedLoc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("ResourcePackPreviewGenerated", new DynamicTexture(generated));
                this.frames.add(gen);
            }
        }

        for (int i = 0; i < this.frames.size(); i++) {
            Frame frame = this.frames.get(i);
            frame.frameIndex = i;
        }
    }

    private static Frame parseAnimationFrame(int frameIndex, int fixedFrameTime, JsonElement frameElement) {
        if (frameElement.isJsonPrimitive()) {
            return new Frame(JsonUtils.getInt(frameElement, "frames[" + frameIndex + "]"), fixedFrameTime);
        } else if (frameElement.isJsonObject()) {
            JsonObject frameObject = JsonUtils.getJsonObject(frameElement, "frames[" + frameIndex + "]");
            int time = JsonUtils.getInt(frameObject, "time", -1);

            if (frameObject.has("time")) {
                if (time < 1)
                    time = 1;
            }

            int idx = JsonUtils.getInt(frameObject, "index");
            if (idx < 0)
                idx = 0;
            return new Frame(idx, time);
        } else {
            return null;
        }
    }

    private static class Frame {
        private int frameIndex;
        private int origFrameIndex;
        private double frameTime;

        @Getter
        @Setter
        private boolean generated = false;

        @Getter
        @Setter
        private Location generatedLoc;

        public Frame(int idx) {
            this(idx, -1);
        }

        public Frame(int idx, double time) {
            this.frameIndex = idx;
            this.origFrameIndex = idx;
            this.frameTime = time;
        }

        public boolean hasNoTime() {
            return this.frameTime == -1;
        }

        public double getFrameTime() {
            return this.frameTime;
        }

        public int getFrameIndex() {
            return this.frameIndex;
        }
    }


}
