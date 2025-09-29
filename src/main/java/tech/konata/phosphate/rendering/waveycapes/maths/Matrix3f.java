package tech.konata.phosphate.rendering.waveycapes.maths;

import java.nio.FloatBuffer;

public final class Matrix3f {
    private float m00;
    private float m01;
    private float m02;
    private float m10;
    private float m11;
    private float m12;
    private float m20;
    private float m21;
    private float m22;

    public Matrix3f() {
    }

    public Matrix3f(final Quaternion quaternion) {
        final float f = quaternion.i();
        final float g = quaternion.j();
        final float h = quaternion.k();
        final float i = quaternion.r();
        final float j = 2.0f * f * f;
        final float k = 2.0f * g * g;
        final float l = 2.0f * h * h;
        this.m00 = 1.0f - k - l;
        this.m11 = 1.0f - l - j;
        this.m22 = 1.0f - j - k;
        final float m = f * g;
        final float n = g * h;
        final float o = h * f;
        final float p = f * i;
        final float q = g * i;
        final float r = h * i;
        this.m10 = 2.0f * (m + r);
        this.m01 = 2.0f * (m - r);
        this.m20 = 2.0f * (o - q);
        this.m02 = 2.0f * (o + q);
        this.m21 = 2.0f * (n + p);
        this.m12 = 2.0f * (n - p);
    }

    public Matrix3f(final Matrix4f matrix4f) {
        this.m00 = matrix4f.m00;
        this.m01 = matrix4f.m01;
        this.m02 = matrix4f.m02;
        this.m10 = matrix4f.m10;
        this.m11 = matrix4f.m11;
        this.m12 = matrix4f.m12;
        this.m20 = matrix4f.m20;
        this.m21 = matrix4f.m21;
        this.m22 = matrix4f.m22;
    }

    public Matrix3f(final Matrix3f matrix3f) {
        this.m00 = matrix3f.m00;
        this.m01 = matrix3f.m01;
        this.m02 = matrix3f.m02;
        this.m10 = matrix3f.m10;
        this.m11 = matrix3f.m11;
        this.m12 = matrix3f.m12;
        this.m20 = matrix3f.m20;
        this.m21 = matrix3f.m21;
        this.m22 = matrix3f.m22;
    }

    public static Matrix3f createScaleMatrix(final float f, final float g, final float h) {
        final Matrix3f matrix3f = new Matrix3f();
        matrix3f.m00 = f;
        matrix3f.m11 = g;
        matrix3f.m22 = h;
        return matrix3f;
    }

    private static int bufferIndex(final int i, final int j) {
        return j * 3 + i;
    }

    public void transpose() {
        float f = this.m01;
        this.m01 = this.m10;
        this.m10 = f;
        f = this.m02;
        this.m02 = this.m20;
        this.m20 = f;
        f = this.m12;
        this.m12 = this.m21;
        this.m21 = f;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final Matrix3f matrix3f = (Matrix3f) object;
        return Float.compare(matrix3f.m00, this.m00) == 0 && Float.compare(matrix3f.m01, this.m01) == 0 && Float.compare(matrix3f.m02, this.m02) == 0 && Float.compare(matrix3f.m10, this.m10) == 0 && Float.compare(matrix3f.m11, this.m11) == 0 && Float.compare(matrix3f.m12, this.m12) == 0 && Float.compare(matrix3f.m20, this.m20) == 0 && Float.compare(matrix3f.m21, this.m21) == 0 && Float.compare(matrix3f.m22, this.m22) == 0;
    }

    @Override
    public int hashCode() {
        int i = (this.m00 != 0.0f) ? Float.floatToIntBits(this.m00) : 0;
        i = 31 * i + ((this.m01 != 0.0f) ? Float.floatToIntBits(this.m01) : 0);
        i = 31 * i + ((this.m02 != 0.0f) ? Float.floatToIntBits(this.m02) : 0);
        i = 31 * i + ((this.m10 != 0.0f) ? Float.floatToIntBits(this.m10) : 0);
        i = 31 * i + ((this.m11 != 0.0f) ? Float.floatToIntBits(this.m11) : 0);
        i = 31 * i + ((this.m12 != 0.0f) ? Float.floatToIntBits(this.m12) : 0);
        i = 31 * i + ((this.m20 != 0.0f) ? Float.floatToIntBits(this.m20) : 0);
        i = 31 * i + ((this.m21 != 0.0f) ? Float.floatToIntBits(this.m21) : 0);
        i = 31 * i + ((this.m22 != 0.0f) ? Float.floatToIntBits(this.m22) : 0);
        return i;
    }

    public void load(final FloatBuffer floatBuffer) {
        this.m00 = floatBuffer.get(bufferIndex(0, 0));
        this.m01 = floatBuffer.get(bufferIndex(0, 1));
        this.m02 = floatBuffer.get(bufferIndex(0, 2));
        this.m10 = floatBuffer.get(bufferIndex(1, 0));
        this.m11 = floatBuffer.get(bufferIndex(1, 1));
        this.m12 = floatBuffer.get(bufferIndex(1, 2));
        this.m20 = floatBuffer.get(bufferIndex(2, 0));
        this.m21 = floatBuffer.get(bufferIndex(2, 1));
        this.m22 = floatBuffer.get(bufferIndex(2, 2));
    }

    public void loadTransposed(final FloatBuffer floatBuffer) {
        this.m00 = floatBuffer.get(bufferIndex(0, 0));
        this.m01 = floatBuffer.get(bufferIndex(1, 0));
        this.m02 = floatBuffer.get(bufferIndex(2, 0));
        this.m10 = floatBuffer.get(bufferIndex(0, 1));
        this.m11 = floatBuffer.get(bufferIndex(1, 1));
        this.m12 = floatBuffer.get(bufferIndex(2, 1));
        this.m20 = floatBuffer.get(bufferIndex(0, 2));
        this.m21 = floatBuffer.get(bufferIndex(1, 2));
        this.m22 = floatBuffer.get(bufferIndex(2, 2));
    }

    public void load(final FloatBuffer floatBuffer, final boolean bl) {
        if (bl) {
            this.loadTransposed(floatBuffer);
        } else {
            this.load(floatBuffer);
        }
    }

    public void load(final Matrix3f matrix3f) {
        this.m00 = matrix3f.m00;
        this.m01 = matrix3f.m01;
        this.m02 = matrix3f.m02;
        this.m10 = matrix3f.m10;
        this.m11 = matrix3f.m11;
        this.m12 = matrix3f.m12;
        this.m20 = matrix3f.m20;
        this.m21 = matrix3f.m21;
        this.m22 = matrix3f.m22;
    }

    @Override
    public String toString() {
        String stringBuilder = "Matrix3f:\n" +
                this.m00 +
                " " +
                this.m01 +
                " " +
                this.m02 +
                "\n" +
                this.m10 +
                " " +
                this.m11 +
                " " +
                this.m12 +
                "\n" +
                this.m20 +
                " " +
                this.m21 +
                " " +
                this.m22 +
                "\n";
        return stringBuilder;
    }

    public void store(final FloatBuffer floatBuffer) {
        floatBuffer.put(bufferIndex(0, 0), this.m00);
        floatBuffer.put(bufferIndex(0, 1), this.m01);
        floatBuffer.put(bufferIndex(0, 2), this.m02);
        floatBuffer.put(bufferIndex(1, 0), this.m10);
        floatBuffer.put(bufferIndex(1, 1), this.m11);
        floatBuffer.put(bufferIndex(1, 2), this.m12);
        floatBuffer.put(bufferIndex(2, 0), this.m20);
        floatBuffer.put(bufferIndex(2, 1), this.m21);
        floatBuffer.put(bufferIndex(2, 2), this.m22);
    }

    public void storeTransposed(final FloatBuffer floatBuffer) {
        floatBuffer.put(bufferIndex(0, 0), this.m00);
        floatBuffer.put(bufferIndex(1, 0), this.m01);
        floatBuffer.put(bufferIndex(2, 0), this.m02);
        floatBuffer.put(bufferIndex(0, 1), this.m10);
        floatBuffer.put(bufferIndex(1, 1), this.m11);
        floatBuffer.put(bufferIndex(2, 1), this.m12);
        floatBuffer.put(bufferIndex(0, 2), this.m20);
        floatBuffer.put(bufferIndex(1, 2), this.m21);
        floatBuffer.put(bufferIndex(2, 2), this.m22);
    }

    public void store(final FloatBuffer floatBuffer, final boolean bl) {
        if (bl) {
            this.storeTransposed(floatBuffer);
        } else {
            this.store(floatBuffer);
        }
    }

    public void setIdentity() {
        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
    }

    public float adjugateAndDet() {
        final float f = this.m11 * this.m22 - this.m12 * this.m21;
        final float g = -(this.m10 * this.m22 - this.m12 * this.m20);
        final float h = this.m10 * this.m21 - this.m11 * this.m20;
        final float i = -(this.m01 * this.m22 - this.m02 * this.m21);
        final float j = this.m00 * this.m22 - this.m02 * this.m20;
        final float k = -(this.m00 * this.m21 - this.m01 * this.m20);
        final float l = this.m01 * this.m12 - this.m02 * this.m11;
        final float m = -(this.m00 * this.m12 - this.m02 * this.m10);
        final float n = this.m00 * this.m11 - this.m01 * this.m10;
        final float o = this.m00 * f + this.m01 * g + this.m02 * h;
        this.m00 = f;
        this.m10 = g;
        this.m20 = h;
        this.m01 = i;
        this.m11 = j;
        this.m21 = k;
        this.m02 = l;
        this.m12 = m;
        this.m22 = n;
        return o;
    }

    public float determinant() {
        final float f = this.m11 * this.m22 - this.m12 * this.m21;
        final float g = -(this.m10 * this.m22 - this.m12 * this.m20);
        final float h = this.m10 * this.m21 - this.m11 * this.m20;
        return this.m00 * f + this.m01 * g + this.m02 * h;
    }

    public boolean invert() {
        final float f = this.adjugateAndDet();
        if (Math.abs(f) > 1.0E-6f) {
            this.mul(f);
            return true;
        }
        return false;
    }

    public void set(final int i, final int j, final float f) {
        if (i == 0) {
            if (j == 0) {
                this.m00 = f;
            } else if (j == 1) {
                this.m01 = f;
            } else {
                this.m02 = f;
            }
        } else if (i == 1) {
            if (j == 0) {
                this.m10 = f;
            } else if (j == 1) {
                this.m11 = f;
            } else {
                this.m12 = f;
            }
        } else if (j == 0) {
            this.m20 = f;
        } else if (j == 1) {
            this.m21 = f;
        } else {
            this.m22 = f;
        }
    }

    public void mul(final Matrix3f matrix3f) {
        final float f = this.m00 * matrix3f.m00 + this.m01 * matrix3f.m10 + this.m02 * matrix3f.m20;
        final float g = this.m00 * matrix3f.m01 + this.m01 * matrix3f.m11 + this.m02 * matrix3f.m21;
        final float h = this.m00 * matrix3f.m02 + this.m01 * matrix3f.m12 + this.m02 * matrix3f.m22;
        final float i = this.m10 * matrix3f.m00 + this.m11 * matrix3f.m10 + this.m12 * matrix3f.m20;
        final float j = this.m10 * matrix3f.m01 + this.m11 * matrix3f.m11 + this.m12 * matrix3f.m21;
        final float k = this.m10 * matrix3f.m02 + this.m11 * matrix3f.m12 + this.m12 * matrix3f.m22;
        final float l = this.m20 * matrix3f.m00 + this.m21 * matrix3f.m10 + this.m22 * matrix3f.m20;
        final float m = this.m20 * matrix3f.m01 + this.m21 * matrix3f.m11 + this.m22 * matrix3f.m21;
        final float n = this.m20 * matrix3f.m02 + this.m21 * matrix3f.m12 + this.m22 * matrix3f.m22;
        this.m00 = f;
        this.m01 = g;
        this.m02 = h;
        this.m10 = i;
        this.m11 = j;
        this.m12 = k;
        this.m20 = l;
        this.m21 = m;
        this.m22 = n;
    }

    public void mul(final Quaternion quaternion) {
        this.mul(new Matrix3f(quaternion));
    }

    public void mul(final float f) {
        this.m00 *= f;
        this.m01 *= f;
        this.m02 *= f;
        this.m10 *= f;
        this.m11 *= f;
        this.m12 *= f;
        this.m20 *= f;
        this.m21 *= f;
        this.m22 *= f;
    }

    public void add(final Matrix3f matrix3f) {
        this.m00 += matrix3f.m00;
        this.m01 += matrix3f.m01;
        this.m02 += matrix3f.m02;
        this.m10 += matrix3f.m10;
        this.m11 += matrix3f.m11;
        this.m12 += matrix3f.m12;
        this.m20 += matrix3f.m20;
        this.m21 += matrix3f.m21;
        this.m22 += matrix3f.m22;
    }

    public void sub(final Matrix3f matrix3f) {
        this.m00 -= matrix3f.m00;
        this.m01 -= matrix3f.m01;
        this.m02 -= matrix3f.m02;
        this.m10 -= matrix3f.m10;
        this.m11 -= matrix3f.m11;
        this.m12 -= matrix3f.m12;
        this.m20 -= matrix3f.m20;
        this.m21 -= matrix3f.m21;
        this.m22 -= matrix3f.m22;
    }

    public float trace() {
        return this.m00 + this.m11 + this.m22;
    }

    public Matrix3f copy() {
        return new Matrix3f(this);
    }
}
