package tech.konata.phosphate.screens.altmanager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.Location;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.utils.alt.Alt;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.SharedRenderingConstants;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.rendering.animation.Animation;
import tech.konata.phosphate.rendering.animation.Easing;
import tech.konata.phosphate.rendering.entities.impl.Image;
import tech.konata.phosphate.rendering.entities.impl.Rect;
import tech.konata.phosphate.rendering.font.CFontRenderer;
import tech.konata.phosphate.rendering.shader.ShaderRenderType;
import tech.konata.phosphate.rendering.shader.Shaders;
import tech.konata.phosphate.rendering.texture.Textures;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;
import tech.konata.phosphate.utils.network.HttpUtils;
import tech.konata.phosphate.utils.timing.Timer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.*;

/**
 * @author IzumiiKonata
 * @since 2024/8/25 22:19
 */
public class ChangeCapeComponent implements SharedRenderingConstants {

    final String accessToken;

    final List<Cape> capes = new ArrayList<>();

    final Alt alt;

    public ChangeCapeComponent(Alt altIn) {
        this.alt = altIn;
        this.accessToken = altIn.getAccessToken();
        this.refreshCapes();
    }

    boolean closing = false;

    boolean lmbPressed = false;

    Animation scaleAnimation = new Animation(Easing.EASE_IN_OUT_CUBIC, Duration.ofMillis(500));

    Localizable selected = Localizable.of("changecape.selected");

    Timer clickResistTimer = new Timer();

    @SneakyThrows
    public void render(double mouseX, double mouseY) {

        float alpha = (float) scaleAnimation.getValue();
        int iAlpha = (int) (alpha * 255);

        Rect.draw(0, 0, this.getWidth(), this.getHeight(), hexColor(0, 0, 0, iAlpha / 4), Rect.RectType.EXPAND);

        GlStateManager.pushMatrix();

        double width = 640, height = 293;

        double centerX = this.getWidth() * 0.5;
        double centerY = this.getHeight() * 0.5;

        double posX = centerX - width * 0.5;
        double posY = centerY - height * 0.5;

        scaleAtPos(centerX, centerY, scaleAnimation.getValue());

        Shaders.GAUSSIAN_BLUR_SHADER.runNoCaching(ShaderRenderType.OVERLAY, Collections.singletonList(() -> {
            roundedRect(posX, posY, width, height, 12, Color.WHITE);
        }));

        roundedRect(posX, posY, width, height, 12, new Color(0, 0, 0, alpha * 0.5f));

        if (!isHovered(mouseX, mouseY, posX, posY, width, height) && Mouse.isButtonDown(0) && clickResistTimer.isDelayed(1000)) {
            this.closing = true;
            lmbPressed = true;
        }

        scaleAnimation.run(closing ? 0 : 1);

        double startX = posX + 12;
        double startY = posY + 12;
        double capeWidth = 80;
        double capeHeight = 130;

        double spacing = 9.5;

        if (!this.capes.isEmpty()) {
            try {
                for (Cape cape : this.capes) {

                    if (startX - posX + capeWidth + spacing > width) {
                        startX = posX + 12;
                        startY += capeHeight + spacing;
                    }

                    roundedRect(startX, startY, capeWidth, capeHeight, 8, new Color(188, 188, 188, (int) (iAlpha * 0.3)));

                    double imgWidth = 60;
                    double imgHeight = 96;
                    if (!cape.id.isEmpty()) {
                        Location capeLocation = this.getCapeLocation(cape.id);

                        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(capeLocation);
                        if (texture != null && texture != TextureUtil.missingTexture) {
                            GlStateManager.color(1, 1, 1, alpha);
                            Image.draw(capeLocation, startX + capeWidth * 0.5 - imgWidth * 0.5, startY + 6, imgWidth, imgHeight, Image.Type.NoColor);
                        }
                    } else {
                        GlStateManager.color(1, 1, 1, alpha);
                        Image.draw(Location.of(Phosphate.NAME + "/textures/no_cape.png"), startX + capeWidth * 0.5 - imgWidth * 0.5, startY + 6, imgWidth, imgHeight, Image.Type.NoColor);
                    }

                    CFontRenderer fr = FontManager.baloo18;

                    fr.drawCenteredString(cape.alias, startX + capeWidth * 0.5, startY + capeHeight - fr.getHeight() - 16, hexColor(233, 233, 233, iAlpha));

                    if (cape.state) {
                        FontManager.pf18.drawCenteredString(selected.get(), startX + capeWidth * 0.5, startY + capeHeight - FontManager.pf18.getHeight() - 6, hexColor(0, 255, 0, iAlpha));
                    }

                    if (isHovered(mouseX, mouseY, startX, startY, capeWidth, capeHeight) && Mouse.isButtonDown(0) && !lmbPressed) {
                        lmbPressed = true;

                        if (cape.id.isEmpty()) {
                            MultiThreadingUtil.runAsync(() -> {
                                Map<String, String> checkProductHeaders = new HashMap<>();
                                checkProductHeaders.put("Authorization", "Bearer " + this.accessToken);
                                String s = null;
                                try {
                                    s = HttpUtils.deleteString("https://api.minecraftservices.com/minecraft/profile/capes/active", null, checkProductHeaders);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                this.parse(s);
                            });
                            break;
                        } else {
                            MultiThreadingUtil.runAsync(() -> {
                                Map<String, String> checkProductHeaders = new HashMap<>();
                                checkProductHeaders.put("Authorization", "Bearer " + this.accessToken);

                                ///*"https://api.minecraftservices.com/minecraft/profile/capes/active", putParams, checkProductHeaders*/
                                String s = null;
                                try {
                                    s = HttpUtils.readString(HttpUtils.request("https://api.minecraftservices.com/minecraft/profile/capes/active", "{\"capeId\": \"" + cape.id + "\"}", checkProductHeaders, "PUT"));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                this.parse(s);
                            });
                            break;
                        }
                    }

                    startX += capeWidth + spacing;
                }
            } catch (ConcurrentModificationException ignored) {
                // I DONT GIVE A FUCK
            }
        }

        GlStateManager.popMatrix();

        if (!Mouse.isButtonDown(0) && lmbPressed) {
            lmbPressed = false;
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closing = true;
        }
    }

    @SneakyThrows
    private void parse(String input) {
        this.capes.clear();

        this.capes.add(new Cape("", false, "", "No Cape"));

        JsonObject profile = new Gson().fromJson(input, JsonObject.class);

        JsonArray capes = profile.getAsJsonArray("capes");

        for (JsonElement cape : capes) {
            JsonObject jObj = cape.getAsJsonObject();

            String id = jObj.get("id").getAsString();
            String state = jObj.get("state").getAsString();
            String url = jObj.get("url").getAsString();
            String alias = jObj.get("alias").getAsString();

            Location capeLocation = this.getCapeLocation(id);
            Location capeLocationFull = this.getCapeLocationFull(id);

            if (Minecraft.getMinecraft().getTextureManager().getTexture(capeLocation) == null || Minecraft.getMinecraft().getTextureManager().getTexture(capeLocation) == TextureUtil.missingTexture) {
                MultiThreadingUtil.runAsync(new Runnable() {
                    @Override
                    @SneakyThrows
                    public void run() {
                        InputStream is = HttpUtils.get(url, null);
                        BufferedImage full = ImageIO.read(is);
                        BufferedImage img = crop(full, 1, 1, 10, 16, 10, 16);

                        Textures.loadTextureAsyncly(capeLocation, img);
                        Textures.loadTextureAsyncly(capeLocationFull, full);

                    }
                });
            }

            this.capes.add(new Cape(id, state.equals("ACTIVE"), url, alias));
        }

        if (this.capes.size() > 1) {
            boolean hasActiveCape = false;
            for (int i = 1; i < this.capes.size(); i++) {
                Cape cape = this.capes.get(i);

                if (cape.state) {
                    hasActiveCape = true;

                    if (Objects.equals(Minecraft.getMinecraft().getSession().getProfile().getName(), alt.getUsername())) {
                        AltScreen.getInstance().player.playerInfo.locationCape = getCapeLocationFull(cape.id);

                        Collection<Property> textures = Minecraft.getMinecraft().profileProperties.get("textures");

                        Gson gson = new Gson();
                        JsonObject jObj = null;

                        for (Iterator<Property> iterator = textures.iterator(); iterator.hasNext(); ) {
                            Property t = iterator.next();
                            JsonObject jObjOld = gson.fromJson(new String(Base64.getDecoder().decode(t.getValue())), JsonObject.class);

                            if (!jObjOld.get("profileName").getAsString().equals(Minecraft.getMinecraft().getSession().getProfile().getName()))
                                continue;

                            jObj = new JsonObject();

                            jObj.addProperty("timestamp", jObjOld.get("timestamp").getAsString());
                            jObj.addProperty("profileId", jObjOld.get("profileId").getAsString());
                            jObj.addProperty("profileName", jObjOld.get("profileName").getAsString());

                            JsonObject texOld = jObjOld.getAsJsonObject("textures");
                            JsonObject tex = new JsonObject();

                            tex.add("SKIN", texOld.get("SKIN").getAsJsonObject());

                            JsonObject CAPE = new JsonObject();

                            CAPE.addProperty("url", cape.url);

                            tex.add("CAPE", CAPE);

                            jObj.add("textures", tex);

                            iterator.remove();
                        }

                        if (jObj != null)
                            textures.add(new Property("textures", Base64.getEncoder().encodeToString(gson.toJson(jObj).getBytes(StandardCharsets.UTF_8))));

                    }

                    if (Objects.equals(AltScreen.getInstance().player.playerInfo.gameProfile.getName(), alt.getUsername())) {
                        int ModelParts = 0;
                        for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                            ModelParts |= enumplayermodelparts.getPartMask();
                        }
                        AltScreen.getInstance().player.getDataWatcher().updateObject(10, ModelParts);
                    }

                    break;
                }
            }

            if (!hasActiveCape) {
                if (Objects.equals(AltScreen.getInstance().player.playerInfo.gameProfile.getName(), alt.getUsername())) {
                    int ModelParts = 0;
                    for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                        if (enumplayermodelparts != EnumPlayerModelParts.CAPE)
                            ModelParts |= enumplayermodelparts.getPartMask();
                    }
                    AltScreen.getInstance().player.getDataWatcher().updateObject(10, ModelParts);
                }
                this.capes.get(0).state = true;
            }
        }
    }

    @SneakyThrows
    private void refreshCapes() {

        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                Map<String, String> checkProductHeaders = new HashMap<>();
                checkProductHeaders.put("Authorization", "Bearer " + ChangeCapeComponent.this.accessToken);
                String profileJson = HttpUtils.getString("https://api.minecraftservices.com/minecraft/profile", null, checkProductHeaders);
                ChangeCapeComponent.this.parse(profileJson);
            }
        });

    }

    private BufferedImage crop(BufferedImage in, int posX, int posY, int width, int height, int u, int v) {

        BufferedImage croped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = croped.createGraphics();

        g.drawImage(in, 0, 0, width, height, posX, posY, posX + u, posY + v, null);

        return croped;

    }

    private Location getCapeLocation(String uuid) {
        return Location.of(Phosphate.NAME + "/textures/Cape" + uuid);
    }

    private Location getCapeLocationFull(String uuid) {
        return Location.of(Phosphate.NAME + "/textures/CapeFull" + uuid);
    }

    private static class Cape {
        public final String id;
        public boolean state;
        public final String url;
        public final String alias;

        public Cape(String id, boolean state, String url, String alias) {
            this.id = id;
            this.state = state;
            this.url = url;
            this.alias = alias;
        }
    }
}
