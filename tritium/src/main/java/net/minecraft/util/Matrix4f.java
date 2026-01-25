package net.minecraft.util;

import java.nio.FloatBuffer;

public class Matrix4f extends org.lwjglx.util.vector.Matrix4f {
    public Matrix4f(float[] p_i46413_1_) {
        this.m00 = p_i46413_1_[0];
        this.m01 = p_i46413_1_[1];
        this.m02 = p_i46413_1_[2];
        this.m03 = p_i46413_1_[3];
        this.m10 = p_i46413_1_[4];
        this.m11 = p_i46413_1_[5];
        this.m12 = p_i46413_1_[6];
        this.m13 = p_i46413_1_[7];
        this.m20 = p_i46413_1_[8];
        this.m21 = p_i46413_1_[9];
        this.m22 = p_i46413_1_[10];
        this.m23 = p_i46413_1_[11];
        this.m30 = p_i46413_1_[12];
        this.m31 = p_i46413_1_[13];
        this.m32 = p_i46413_1_[14];
        this.m33 = p_i46413_1_[15];
    }

    public Matrix4f() {
        this.m00 = this.m01 = this.m02 = this.m03 = this.m10 = this.m11 = this.m12 = this.m13 = this.m20 = this.m21 = this.m22 = this.m23 = this.m30 = this.m31 = this.m32 = this.m33 = 0.0F;
    }

    public static Matrix4f perspective(double fov, float aspectRatio, float nearPlane, float farPlane) {
        float f = (float) (1.0D / Math.tan(fov * (double) ((float) Math.PI / 180F) / 2.0D));
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00 = f / aspectRatio;
        matrix4f.m11 = f;
        matrix4f.m22 = (farPlane + nearPlane) / (nearPlane - farPlane);
        matrix4f.m32 = -1.0F;
        matrix4f.m23 = 2.0F * farPlane * nearPlane / (nearPlane - farPlane);
        return matrix4f;
    }

    private static int bufferIndex(int p_226594_0_, int p_226594_1_) {
        return p_226594_1_ * 4 + p_226594_0_;
    }

    public void write(FloatBuffer floatBufferIn) {
        floatBufferIn.put(bufferIndex(0, 0), this.m00);
        floatBufferIn.put(bufferIndex(0, 1), this.m01);
        floatBufferIn.put(bufferIndex(0, 2), this.m02);
        floatBufferIn.put(bufferIndex(0, 3), this.m03);
        floatBufferIn.put(bufferIndex(1, 0), this.m10);
        floatBufferIn.put(bufferIndex(1, 1), this.m11);
        floatBufferIn.put(bufferIndex(1, 2), this.m12);
        floatBufferIn.put(bufferIndex(1, 3), this.m13);
        floatBufferIn.put(bufferIndex(2, 0), this.m20);
        floatBufferIn.put(bufferIndex(2, 1), this.m21);
        floatBufferIn.put(bufferIndex(2, 2), this.m22);
        floatBufferIn.put(bufferIndex(2, 3), this.m23);
        floatBufferIn.put(bufferIndex(3, 0), this.m30);
        floatBufferIn.put(bufferIndex(3, 1), this.m31);
        floatBufferIn.put(bufferIndex(3, 2), this.m32);
        floatBufferIn.put(bufferIndex(3, 3), this.m33);
    }
}
