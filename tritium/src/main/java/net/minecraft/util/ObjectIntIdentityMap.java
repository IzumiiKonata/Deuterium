package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;

import java.util.Arrays;
import java.util.Iterator;

public class ObjectIntIdentityMap<T> implements IObjectIntIterable<T> {
    private static final int DEFAULT_CAPACITY = 512;

    private final Reference2IntLinkedOpenHashMap<T> identityMap;

    private Object[] valueArray;

    public ObjectIntIdentityMap() {
        this.identityMap = new Reference2IntLinkedOpenHashMap<>(DEFAULT_CAPACITY);
        this.identityMap.defaultReturnValue(-1);
        this.valueArray = new Object[DEFAULT_CAPACITY];
    }

    public void put(T key, int value) {
        this.identityMap.put(key, value);

        if (value >= this.valueArray.length) {
            this.valueArray = Arrays.copyOf(this.valueArray, value * 2 + 1);
        }
        this.valueArray[value] = key;
    }

    public int get(T key) {
        return this.identityMap.getInt(key);
    }

    @SuppressWarnings("unchecked")
    public final T getByValue(int value) {
        if (value < 0 || value >= this.valueArray.length) {
            return null;
        }
        return (T) this.valueArray[value];
    }

    public Iterator<T> iterator() {
        return this.identityMap.keySet().iterator();
    }
}