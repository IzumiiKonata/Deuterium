package net.minecraft.util;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import tritium.utils.optimization.IdentifierCaches;

import java.util.HashMap;
import java.util.Map;

public class Location {
    protected final String resourceDomain;
    protected final String resourcePath;

    protected Location(String resourceDomain, String resourcePath) {
        this.resourceDomain = IdentifierCaches.NAMESPACES.deduplicate(org.apache.commons.lang3.StringUtils.isEmpty(resourceDomain) ? "minecraft" : resourceDomain.toLowerCase());
        this.resourcePath = IdentifierCaches.PATH.deduplicate(resourcePath);
        Validate.notNull(this.resourcePath);
    }

    /**
     * Splits an object name (such as minecraft:apple) into the domain and path parts and returns these as an array of
     * length 2. If no colon is present in the passed value the returned array will contain {null, toSplit}.
     */
    protected static String[] splitObjectName(String toSplit) {
        String[] astring = new String[]{null, toSplit};
        int i = toSplit.indexOf(58);

        if (i >= 0) {
            astring[1] = toSplit.substring(i + 1);

            if (i > 1) {
                astring[0] = toSplit.substring(0, i);
            }
        }

        return astring;
    }

    public String getResourcePath() {
        return this.resourcePath;
    }

    public String getResourceDomain() {
        return this.resourceDomain;
    }

    public String toString() {
        return this.resourceDomain + ':' + this.resourcePath;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof Location)) {
            return false;
        } else {
            Location resourcelocation = (Location) p_equals_1_;
            return this.resourceDomain.equals(resourcelocation.resourceDomain) && this.resourcePath.equals(resourcelocation.resourcePath);
        }
    }

    public int hashCode() {
        return 31 * this.resourceDomain.hashCode() + this.resourcePath.hashCode();
    }

    private static final Map<String, Location> locationCache = new HashMap<>();
    public static final Map<String, Map<String, Location>> twoDimensionsCache = new HashMap<>();

    public static Location of(String path) {

        if (!locationCache.containsKey(path)) {
            String[] strings = splitObjectName(path);
            Location location = new Location(strings[0], strings[1]);
            locationCache.put(path, location);
            return location;
        }

        return locationCache.get(path);

    }

    public static Location of(String resourceDomainIn, String resourcePathIn) {

        Map<String, Location> v1 = twoDimensionsCache.get(resourceDomainIn);
        if (v1 != null) {
            Location v2 = v1.get(resourcePathIn);

            if (v2 != null) {
                return v2;
            }

            Location location = new Location(resourceDomainIn, resourcePathIn);
            v1.put(resourcePathIn, location);

            return location;
        }

        Location location = new Location(resourceDomainIn, resourcePathIn);
        Map<String, Location> map = new HashMap<>();
        map.put(resourceDomainIn, location);
        twoDimensionsCache.put(resourceDomainIn, map);
        return location;
    }

}
