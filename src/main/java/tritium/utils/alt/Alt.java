/*
 * Decompiled with CFR 0.150.
 */
package tritium.utils.alt;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Location;
import tritium.Tritium;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.rendering.texture.Textures;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.res.skin.SkinUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
public class Alt {
    @SerializedName("username")
    public String username;
    @SerializedName("refreshToken")
    public String refreshToken = "";

    @SerializedName("accessToken")
    public String accessToken = "";

    @SerializedName("userUUID")
    public String userUUID = "";

    @Setter
    @SerializedName("lastRefreshedTime")
    private long lastRefreshedTime = 0L;

    public transient boolean skinLoaded;

    private transient Location skinLocation = null;

    public transient float hoveredAlpha = 0.0f;

    public Alt(String crackedName) {
        this.username = crackedName;
    }

    public Alt(String userName, String refreshToken, String accessToken, String userUUID) {
        this.username = userName;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.userUUID = userUUID;
    }

    public boolean isMicrosoft() {
        return !this.refreshToken.isEmpty() && !this.accessToken.isEmpty() && !this.userUUID.isEmpty();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() / 1000L - this.getLastRefreshedTime() > 86400;
    }

    public long getLeftExpiringTime() {
        return 86400 - (System.currentTimeMillis() / 1000L - this.getLastRefreshedTime());
    }

    public Location getSkinLocation() {

        if (!this.isMicrosoft()) {

            boolean slim = DefaultPlayerSkin.isSlimSkin(EntityPlayer.getOfflineUUID(this.username));

            if (!skinLoaded) {
                skinLoaded = true;
            }

            if (slim) {
                return SkinUtils.ALEX_FACE;
            } else {
                return SkinUtils.STEVE_FACE;
            }
        }

        String uuid = isMicrosoft() ? userUUID : EntityPlayer.getOfflineUUID(username).toString().replaceAll("-", "");

        if (!skinLoaded) {
            skinLoaded = true;
            RenderSystem.playerSkinTextureCache.getSkinTextureNoCache(this.username, (l, b) -> {

                MultiThreadingUtil.runAsync(new Runnable() {
                    @Override
                    @SneakyThrows
                    public void run() {

                        BufferedImage head = crop(b, 8, 8, 8, 8, 8, 8);
                        BufferedImage layer = crop(b, 40, 8, 8, 8, 8, 8);
                        BufferedImage combined = combine(head, layer);

                        String uuid = isMicrosoft() ? userUUID : EntityPlayer.getOfflineUUID(username).toString().replaceAll("-", "");

                        Location loc = Location.of(Tritium.NAME + "/textures/skin/" + uuid);

                        Textures.loadTextureAsyncly(loc, combined, () -> skinLocation = loc);
                    }
                });
            });
        }

        if (skinLocation == null) {
            Location loc = Location.of(Tritium.NAME + "/textures/skin/" + uuid);

            if (Minecraft.getMinecraft().getTextureManager().getTexture(loc) != null) {
                skinLocation = loc;
            }

            boolean slim = DefaultPlayerSkin.isSlimSkin(EntityPlayer.getOfflineUUID(this.username));
            if (slim) {
                return SkinUtils.ALEX_FACE;
            } else {
                return SkinUtils.STEVE_FACE;
            }
        }

        return skinLocation;
    }

    private BufferedImage crop(BufferedImage in, int posX, int posY, int width, int height, int u, int v) {

        BufferedImage croped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = croped.createGraphics();

        g.drawImage(in, 0, 0, width, height, posX, posY, posX + u, posY + v, null);

        return croped;

    }

    private BufferedImage combine(BufferedImage a, BufferedImage b) {
        BufferedImage croped = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = croped.createGraphics();

        g.drawImage(a, 0, 0, null);
        g.drawImage(b, 0, 0, null);

        return croped;
    }

}

