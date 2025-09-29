/*
 * Decompiled with CFR 0.150.
 */
package tech.konata.phosphate.utils.alt;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AltManager {
    @Getter
    public static final List<Alt> alts = Collections.synchronizedList(new ArrayList<Alt>());
    public static Alt lastAlt;

    public Alt getLastAlt() {
        return lastAlt;
    }

    public void setLastAlt(Alt alt) {
        lastAlt = alt;
    }
}

