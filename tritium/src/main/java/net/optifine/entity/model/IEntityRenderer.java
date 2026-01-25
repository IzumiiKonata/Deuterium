package net.optifine.entity.model;

import net.minecraft.util.Location;

public interface IEntityRenderer {
    Class getEntityClass();

    void setEntityClass(Class var1);

    Location getLocationTextureCustom();

    void setLocationTextureCustom(Location var1);
}
