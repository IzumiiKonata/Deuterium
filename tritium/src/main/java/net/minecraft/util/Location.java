package net.minecraft.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import tritium.utils.optimization.IdentifierCaches;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY
)
public class Location {

    @EqualsAndHashCode.Include
    protected final String resourceDomain;

    @EqualsAndHashCode.Include
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

    public String toString() {
        return this.resourceDomain + ':' + this.resourcePath;
    }

//    public boolean equals(Object p_equals_1_) {
//        if (this == p_equals_1_) {
//            return true;
//        } else if (!(p_equals_1_ instanceof Location resourcelocation)) {
//            return false;
//        } else {
//            return this.resourceDomain.equals(resourcelocation.resourceDomain) && this.resourcePath.equals(resourcelocation.resourcePath);
//        }
//    }
//
//    public int hashCode() {
//        return 31 * this.resourceDomain.hashCode() + this.resourcePath.hashCode();
//    }

    private static final Map<String, Location> locationCache = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Location>> twoDimensionsCache = new ConcurrentHashMap<>();

    public static Location of(String path) {
        return locationCache.computeIfAbsent(path, p -> {
            String[] strings = splitObjectName(p);
            return new Location(strings[0], strings[1]);
        });
    }

    public static Location of(String resourceDomainIn, String resourcePathIn) {
        return twoDimensionsCache
                .computeIfAbsent(resourceDomainIn, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(resourcePathIn, k -> new Location(resourceDomainIn, resourcePathIn));
    }

}
