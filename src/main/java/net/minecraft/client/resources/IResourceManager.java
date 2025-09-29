package net.minecraft.client.resources;

import net.minecraft.util.Location;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface IResourceManager {
    Set<String> getResourceDomains();

    IResource getResource(Location location) throws IOException;

    List<IResource> getAllResources(Location location) throws IOException;
}
