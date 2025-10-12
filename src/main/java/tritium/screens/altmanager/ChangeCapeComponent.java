package tritium.screens.altmanager;

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
import org.lwjglx.input.Mouse;
import tritium.Tritium;
import tritium.rendering.StencilClipManager;
import tritium.rendering.animation.Interpolations;
import tritium.utils.alt.Alt;
import tritium.utils.i18n.Localizable;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.rendering.entities.impl.Image;
import tritium.rendering.entities.impl.Rect;
import tritium.rendering.font.CFontRenderer;
import tritium.rendering.texture.Textures;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.network.HttpUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author IzumiiKonata
 * @since 2024/8/25 22:19
 */
public class ChangeCapeComponent implements SharedRenderingConstants {

    final String accessToken;

    final List<Cape> capes = new CopyOnWriteArrayList<>();

    final Alt alt;

    public ChangeCapeComponent(Alt altIn) {
        this.alt = altIn;
        this.accessToken = altIn.getAccessToken();
        this.refreshCapes();
    }

    boolean lmbPressed = false;

    Localizable selected = Localizable.of("changecape.selected");

    double realOffset = 0, targetOffset = 0;

    @SneakyThrows
    public void render(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {

        GlStateManager.pushMatrix();

        double startX = posX + 12;
        double startY = posY + 12 - realOffset;
        double capeWidth = 74;
        double capeHeight = 138;

        double spacing = 9.5;

        handleScrolling(mouseX, mouseY, posX, posY, width, height, dWheel);

        realOffset = Interpolations.interpBezier(realOffset, targetOffset, 0.2f);

        if (!this.capes.isEmpty()) {

            StencilClipManager.beginClip(() -> Rect.draw(posX, posY, width, height, -1));

            for (Cape cape : this.capes) {

                if (startX - posX + capeWidth + spacing > width) {
                    startX = posX + 12;
                    startY += capeHeight + spacing;
                }

                drawCapeBox(startX, startY, capeWidth, capeHeight);
                drawCapeImage(startX, startY, capeWidth, capeHeight, cape);
                drawCapeText(startX, startY, capeWidth, capeHeight, cape);

                handleCapeSelection(mouseX, mouseY, startX, startY, capeWidth, capeHeight, cape);

                startX += capeWidth + spacing;
            }

            StencilClipManager.endClip();
        }

        GlStateManager.popMatrix();

        updateMouseState();
    }

    private void handleScrolling(double mouseX, double mouseY, double posX, double posY, double width, double height, int dWheel) {
        if (dWheel != 0 && isHovered(mouseX, mouseY, posX, posY, width, height)) {

            double delta = 24;

            targetOffset += delta * (dWheel > 0 ? -1 : 1);

            targetOffset = Math.max(0, targetOffset);
        }
    }

    private void drawCapeBox(double startX, double startY, double capeWidth, double capeHeight) {
        Rect.draw(startX - 1, startY - 1, capeWidth + 2, capeHeight + 2, AltScreen.getInstance().getColor(AltScreen.ColorType.CONTAINER_OUTLINE));
        Rect.draw(startX, startY, capeWidth, capeHeight, AltScreen.getInstance().getColor(AltScreen.ColorType.CONTAINER_ELEMENT_BACKGROUND));
    }

    private void drawCapeImage(double startX, double startY, double capeWidth, double capeHeight, Cape cape) {
        double imgWidth = 60;
        double imgHeight = 96;
        if (!cape.id.isEmpty()) {
            Location capeLocation = this.getCapeLocation(cape.id);

            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(capeLocation);
            if (texture != null && texture != TextureUtil.missingTexture) {
                GlStateManager.color(1, 1, 1, 1f);
                Image.draw(capeLocation, startX + capeWidth * 0.5 - imgWidth * 0.5, startY + 6, imgWidth, imgHeight, Image.Type.NoColor);
            }
        } else {
            GlStateManager.color(1, 1, 1, 1f);
            Image.draw(Location.of(Tritium.NAME + "/textures/no_cape.png"), startX + capeWidth * 0.5 - imgWidth * 0.5, startY + 6, imgWidth, imgHeight, Image.Type.NoColor);
        }
    }

    private void drawCapeText(double startX, double startY, double capeWidth, double capeHeight, Cape cape) {
        CFontRenderer fr = FontManager.pf18;

        fr.drawCenteredString(cape.alias, startX + capeWidth * 0.5, startY + capeHeight - fr.getHeight() - 16, AltScreen.getInstance().getColor(AltScreen.ColorType.PRIMARY_TEXT));

        if (cape.state) {
            FontManager.pf16.drawCenteredString(selected.get(), startX + capeWidth * 0.5, startY + capeHeight - FontManager.pf18.getHeight() - 3, hexColor(0, 255, 0, 255));
        }
    }

    private void handleCapeSelection(double mouseX, double mouseY, double startX, double startY, double capeWidth, double capeHeight, Cape cape) {
        if (isHovered(mouseX, mouseY, startX, startY, capeWidth, capeHeight) && Mouse.isButtonDown(0) && !lmbPressed) {
            lmbPressed = true;

            MultiThreadingUtil.runAsync(() -> {
                Map<String, String> checkProductHeaders = new HashMap<>();
                checkProductHeaders.put("Authorization", "Bearer " + this.accessToken);
                String s;
                try {

                    if (cape.id.isEmpty()) {
                        s = HttpUtils.deleteString("https://api.minecraftservices.com/minecraft/profile/capes/active", null, checkProductHeaders);
                    } else {
                        s = HttpUtils.readString(HttpUtils.request("https://api.minecraftservices.com/minecraft/profile/capes/active", "{\"capeId\": \"" + cape.id + "\"}", checkProductHeaders, "PUT"));
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.parse(s);
            });
        }
    }

    private void updateMouseState() {
        if (!Mouse.isButtonDown(0) && lmbPressed) {
            lmbPressed = false;
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

        updatePlayerCape();
    }

    private void updatePlayerCape() {
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
        return Location.of(Tritium.NAME + "/textures/Cape" + uuid);
    }

    private Location getCapeLocationFull(String uuid) {
        return Location.of(Tritium.NAME + "/textures/CapeFull" + uuid);
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