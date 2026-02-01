package net.optifine.config;

import net.minecraft.item.Item;
import net.minecraft.util.Location;

public class ItemLocator implements IObjectLocator {
    public Object getObject(Location loc) {
        return Item.getByNameOrId(loc.toString());
    }
}
