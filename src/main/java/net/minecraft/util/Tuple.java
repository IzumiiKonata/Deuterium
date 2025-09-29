package net.minecraft.util;

public class Tuple<A, B> {
    private final A a;
    private final B b;

    public Tuple(A aIn, B bIn) {
        this.a = aIn;
        this.b = bIn;
    }

    public static <X, Y> Tuple<X, Y> of(X x, Y y) {
        return new Tuple<>(x, y);
    }

    /**
     * Get the first Object in the Tuple
     */
    public A getFirst() {
        return this.a;
    }

    /**
     * Get the second Object in the Tuple
     */
    public B getSecond() {
        return this.b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }
}
