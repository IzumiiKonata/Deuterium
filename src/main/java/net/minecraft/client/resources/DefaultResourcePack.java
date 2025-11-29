package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.Location;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

public class DefaultResourcePack implements IResourcePack {
    public static final Set<String> defaultResourceDomains = ImmutableSet.of("minecraft", "realms");
    private final Map<String, File> mapAssets;

    public DefaultResourcePack(Map<String, File> mapAssetsIn) {
        this.mapAssets = mapAssetsIn;
    }

    public InputStream getInputStream(Location location) throws IOException {
        InputStream inputstream = this.getResourceStream(location);

        if (inputstream != null) {
            return inputstream;
        } else {
            InputStream inputstream1 = this.getInputStreamAssets(location);

            if (inputstream1 != null) {
                return inputstream1;
            } else {
                throw new FileNotFoundException(location.getResourcePath());
            }
        }
    }

    public InputStream getInputStreamAssets(Location location) throws IOException {
        File file1 = this.mapAssets.get(location.toString());
        return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
    }

    private InputStream getResourceStream(Location location) {
        String s = "/assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
        return DefaultResourcePack.class.getResourceAsStream(s);
    }

    public boolean resourceExists(Location location) {
        InputStream stream = this.getResourceStream(location);
        boolean result = stream != null || this.mapAssets.containsKey(location.toString());

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public Set<String> getResourceDomains() {
        return defaultResourceDomains;
    }

    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        try {
            InputStream inputstream = new FileInputStream(this.mapAssets.get("pack.mcmeta"));
            return AbstractResourcePack.readMetadata(metadataSerializer, inputstream, metadataSectionName);
        } catch (RuntimeException var4) {
            return null;
        } catch (FileNotFoundException var5) {
            return null;
        }
    }

    public BufferedImage getPackImage() throws IOException {
        return TextureUtil.readBufferedImage(DefaultResourcePack.class.getResourceAsStream("/" + (Location.of("pack.png")).getResourcePath()));
    }

    public String getPackName() {
        return "Default";
    }

    @Override
    public boolean hasAnimations() {
        return false;
    }

    @Override
    public boolean hasSounds() {
        return true;
    }
}
