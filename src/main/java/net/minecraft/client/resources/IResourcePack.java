package net.minecraft.client.resources;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.Location;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface IResourcePack {
    InputStream getInputStream(Location location) throws IOException;

    boolean resourceExists(Location location);

    Set<String> getResourceDomains();

    <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer metadataSerializer, String metadataSectionName) throws IOException;

    BufferedImage getPackImage() throws IOException;

    String getPackName();

    boolean hasAnimations();

    boolean hasSounds();
}
