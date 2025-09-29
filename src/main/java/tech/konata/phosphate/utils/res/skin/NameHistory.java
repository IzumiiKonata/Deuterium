/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package tech.konata.phosphate.utils.res.skin;


import org.apache.commons.lang3.ArrayUtils;

import java.util.UUID;

public class NameHistory {
    private final UUID uuid;
    private final UUIDFetcher[] changes;

    public NameHistory(UUID uuid, UUIDFetcher[] changes) {
        this.uuid = uuid;
        this.changes = changes;
        ArrayUtils.reverse(this.changes);
    }

    public UUIDFetcher[] getChanges() {
        return this.changes;
    }

    public UUID getUUID() {
        return this.uuid;
    }
}

