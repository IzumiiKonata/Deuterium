package net.minecraft.util;

import java.util.function.Supplier;

public abstract class LazyLoadBase<T> {
    private T value;
    private volatile boolean isLoaded = false;

    public T getValue() {
        if (!this.isLoaded) {
            synchronized (this) {
                if (!this.isLoaded) {
                    this.value = this.load();
                    this.isLoaded = true;
                }
            }
        }

        return this.value;
    }

    public void forceReload() {
        synchronized (this) {
            this.value = this.load();
            this.isLoaded = true;
        }
    }

    protected abstract T load();

    public static <T> LazyLoadBase<T> of(Supplier<T> supplier) {
        return new LazyLoadBase<>() {
            @Override
            protected T load() {
                return supplier.get();
            }
        };
    }

}
