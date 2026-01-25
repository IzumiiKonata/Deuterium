package net.minecraft.client.resources;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.Location;

import java.io.InputStream;

public interface IResource {
    Location getResourceLocation();

    InputStream getInputStream();

    boolean hasMetadata();

    <T extends IMetadataSection> T getMetadata(String p_110526_1_);

    String getResourcePackName();
}
