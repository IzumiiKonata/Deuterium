package tritium.rendering.waveycapes.maths;

import com.google.common.collect.Queues;
import tritium.rendering.waveycapes.Mth;

import java.util.Deque;

public class PoseStack {
    private final Deque<Pose> poseStack;

    public PoseStack() {
        this.poseStack = Queues.newArrayDeque();
        final Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        final Matrix3f matrix3f = new Matrix3f();
        matrix3f.setIdentity();
        this.poseStack.add(new Pose(matrix4f, matrix3f));
    }

    public void translate(final double d, final double e, final double f) {
        final Pose pose = this.poseStack.getLast();
        pose.pose.multiplyWithTranslation((float) d, (float) e, (float) f);
    }

    public void scale(final float f, final float g, final float h) {
        final Pose pose = this.poseStack.getLast();
        pose.pose.multiply(Matrix4f.createScaleMatrix(f, g, h));
        if (f == g && g == h) {
            if (f > 0.0f) {
                return;
            }
            pose.normal.mul(-1.0f);
        }
        final float i = 1.0f / f;
        final float j = 1.0f / g;
        final float k = 1.0f / h;
        final float l = Mth.fastInvCubeRoot(i * j * k);
        pose.normal.mul(Matrix3f.createScaleMatrix(l * i, l * j, l * k));
    }

    public void mulPose(final Quaternion quaternion) {
        final Pose pose = this.poseStack.getLast();
        pose.pose.multiply(quaternion);
        pose.normal.mul(quaternion);
    }

    public void pushPose() {
        final Pose pose = this.poseStack.getLast();
        this.poseStack.addLast(new Pose(pose.pose.copy(), pose.normal.copy()));
    }

    public void popPose() {
        this.poseStack.removeLast();
    }

    public Pose last() {
        return this.poseStack.getLast();
    }

    public boolean clear() {
        return this.poseStack.size() == 1;
    }

    public void setIdentity() {
        final Pose pose = this.poseStack.getLast();
        pose.pose.setIdentity();
        pose.normal.setIdentity();
    }

    public void mulPoseMatrix(final Matrix4f matrix4f) {
        this.poseStack.getLast().pose.multiply(matrix4f);
    }

    public static final class Pose {
        final Matrix4f pose;
        final Matrix3f normal;

        Pose(final Matrix4f matrix4f, final Matrix3f matrix3f) {
            this.pose = matrix4f;
            this.normal = matrix3f;
        }

        public Matrix4f pose() {
            return this.pose;
        }

        public Matrix3f normal() {
            return this.normal;
        }
    }
}