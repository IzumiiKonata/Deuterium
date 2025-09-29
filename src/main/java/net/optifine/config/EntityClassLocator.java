package net.optifine.config;

import net.minecraft.util.Location;
import net.optifine.util.EntityUtils;

public class EntityClassLocator implements IObjectLocator {
    public Object getObject(Location loc) {
        Class oclass = EntityUtils.getEntityClassByName(loc.getResourcePath());
        return oclass;
    }
}
