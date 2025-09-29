package tech.konata.phosphate.utils.res.skin;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Location;

import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerSkinTextureCache {
    private final Map<UUID, Location> loadedSkins = new HashMap<UUID, Location>();
    private final Map<String, Location> loadedUsernameSkins = new HashMap<String, Location>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final SkinManager skinManager;
    private final MinecraftSessionService minecraftSessionService;

    @ConstructorProperties(value = {"skinManager", "minecraftSessionService"})
    public PlayerSkinTextureCache(SkinManager skinManager, MinecraftSessionService minecraftSessionService) {
        this.skinManager = skinManager;
        this.minecraftSessionService = minecraftSessionService;
    }

    public Location getSkinTexture(GameProfile gameProfile, Callback cb) {
        if (gameProfile == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        UUID uuid = gameProfile.getId();
        Location loadedSkinResource = this.loadedSkins.get(uuid);
        if (loadedSkinResource == null) {
            loadedSkinResource = DefaultPlayerSkin.getDefaultSkinLegacy();
            this.loadedSkins.put(uuid, loadedSkinResource);
            this.requestTexture(gameProfile, cb);
        }
        return loadedSkinResource;
    }

    public Location getSkinTexture(UUID uuid, Callback cb) {
        if (uuid == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        Location loadedSkinResource = this.loadedSkins.get(uuid);
        if (loadedSkinResource == null) {
            loadedSkinResource = DefaultPlayerSkin.getDefaultSkinLegacy();
            this.loadedSkins.put(uuid, loadedSkinResource);
            this.requestTexture(new GameProfile(uuid, "Steve"), cb);
        }
        return loadedSkinResource;
    }

    public Location getSkinTexture(String username, Callback cb) {
        if (username == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        Location loadedSkinResource = this.loadedUsernameSkins.get(username);
        if (loadedSkinResource == null) {
            loadedSkinResource = DefaultPlayerSkin.getDefaultSkinLegacy();
            this.loadedUsernameSkins.put(username, loadedSkinResource);
            UUIDFetcher.getUUID(username, uuid -> {
                String username1 = UUIDFetcher.getName(uuid);
                GameProfile gameProfile = new GameProfile(uuid, username1);
                PlayerSkinTextureCache.this.requestTexture(gameProfile, cb);
            });
        }
        return loadedSkinResource;
    }

    public void getSkinTextureNoCache(String username, Callback cb) {
        if (username == null) {
            return;
        }

        UUIDFetcher.getUUID(username, uuid -> {
            String username1 = UUIDFetcher.getName(uuid);
            GameProfile gameProfile = new GameProfile(uuid, username1);
            PlayerSkinTextureCache.this.requestTexture(gameProfile, cb);
        });

    }

    public void getSkinTextureNoCache(GameProfile gameProfile, Callback cb) {
        if (gameProfile == null) {
            return;
        }
        this.requestTexture(gameProfile, cb);
    }

    public interface Callback {

        void onLoaded(Location loc, BufferedImage img);

    }

    public Location getCachedSkinTexture(GameProfile gameProfile) {
        if (gameProfile != null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(gameProfile);
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                return minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            }
            UUID uuid = EntityPlayer.getUUID(gameProfile);
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        return DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    private void requestTexture(final GameProfile gameProfile, Callback cb) {
        MinecraftProfileTexture minecraftProfileTexture = null;
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = this.skinManager.loadSkinFromCache(gameProfile);
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            minecraftProfileTexture = map.get(MinecraftProfileTexture.Type.SKIN);
        }
        if (minecraftProfileTexture == null) {
//            System.out.println("minecraftProfileTexture == null");
            this.executorService.execute(new Runnable() {

                @Override
                public void run() {
                    MinecraftProfileTexture requestedProfileTexture;
//                    if (!gameProfile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
                    PlayerSkinTextureCache.this.minecraftSessionService.fillProfileProperties(gameProfile, false);
//                    }
                    if ((requestedProfileTexture = PlayerSkinTextureCache.this.getMinecraftProfileTexture(gameProfile, MinecraftProfileTexture.Type.SKIN)) != null) {
                        Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                            @Override
                            public void run() {
                                PlayerSkinTextureCache.this.loadSkinTexture(gameProfile, requestedProfileTexture, cb);
                            }
                        });
                    }
                }
            });
        } else {
//            System.out.println("minecraftProfileTexture != null");

            this.loadSkinTexture(gameProfile, minecraftProfileTexture, cb);
        }
    }

    private void loadSkinTexture(final GameProfile gameProfile, MinecraftProfileTexture profileTexture, Callback cb) {
        this.skinManager.loadSkin(profileTexture, MinecraftProfileTexture.Type.SKIN, new SkinManager.SkinAvailableCallback() {

            @Override
            public void skinAvailable(MinecraftProfileTexture.Type typeIn, Location location, MinecraftProfileTexture profileTexture, BufferedImage img) {
                if (typeIn == MinecraftProfileTexture.Type.SKIN) {
                    PlayerSkinTextureCache.this.loadedSkins.put(gameProfile.getId(), location);
                    PlayerSkinTextureCache.this.loadedUsernameSkins.put(gameProfile.getName(), location);
                    cb.onLoaded(location, img);
                }
            }
        });
    }

    private MinecraftProfileTexture getMinecraftProfileTexture(GameProfile gameProfile, MinecraftProfileTexture.Type type) {
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();
        try {
            map.putAll(this.minecraftSessionService.getTextures(gameProfile, false));
        } catch (InsecureTextureException insecureTextureException) {
            // empty catch block
        }
        if (map.isEmpty() && gameProfile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
            gameProfile.getProperties().clear();
            gameProfile.getProperties().putAll(Minecraft.getMinecraft().getProfileProperties());
            map.putAll(this.minecraftSessionService.getTextures(gameProfile, false));
        }
        return map.get(type);
    }
}

