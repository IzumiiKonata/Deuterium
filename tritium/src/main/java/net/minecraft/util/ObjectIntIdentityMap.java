package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Iterator;

public class ObjectIntIdentityMap<T> implements IObjectIntIterable<T> {
    private final Reference2IntOpenHashMap<T> identityMap;
    private final Int2ObjectOpenHashMap<T> valueMap;

    public ObjectIntIdentityMap() {
        this.identityMap = new Reference2IntOpenHashMap<>(512);
        this.valueMap = new Int2ObjectOpenHashMap<>(512);
        this.identityMap.defaultReturnValue(-1);
    }

    public void put(T key, int value) {
        this.identityMap.put(key, value);
        this.valueMap.put(value, key);
    }

    public int get(T key) {
        return this.identityMap.getInt(key);
    }

    public final T getByValue(int value) {
        return this.valueMap.get(value);
    }

    public Iterator<T> iterator() {
        return this.valueMap.values().iterator();
    }
}
