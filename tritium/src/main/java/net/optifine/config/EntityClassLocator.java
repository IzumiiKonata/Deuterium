package net.optifine.config;

import net.minecraft.util.Location;
import net.optifine.util.EntityUtils;

public class EntityClassLocator implements IObjectLocator {
    public Object getObject(Location loc) {
        return EntityUtils.getEntityClassByName(loc.getResourcePath());
    }
}
