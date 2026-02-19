package tritium.bridge.entity;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import today.opai.api.dataset.BoundingBox;
import today.opai.api.dataset.PositionData;
import today.opai.api.dataset.RotationData;
import today.opai.api.dataset.Vec3Data;
import today.opai.api.enums.EnumDirection;
import today.opai.api.interfaces.dataset.Vector3d;
import today.opai.api.interfaces.game.entity.Entity;
import tritium.bridge.misc.math.Vector3dImpl;

import java.util.Objects;
import java.util.UUID;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:31
 */
public class EntityWrapper<T extends net.minecraft.entity.Entity> implements Entity {

    @Getter
    protected final T mcEntity;

    public EntityWrapper(T mcEntity) {
        this.mcEntity = mcEntity;
    }

    @Override
    public boolean equals(Entity entity) {
        return Objects.equals(entity.getEntityId(), this.getEntityId());
    }

    @Override
    public Vector3d getMotion() {
        return new Vector3dImpl(this.getMcEntity().motionX, this.getMcEntity().motionY, this.getMcEntity().motionZ);
    }

    @Override
    public void setMotion(Vec3Data motion) {
        this.getMcEntity().motionX = motion.getX();
        this.getMcEntity().motionY = motion.getY();
        this.getMcEntity().motionZ = motion.getZ();
    }

    @Override
    public PositionData getPosition() {
        return new PositionData(this.getMcEntity().posX, this.getMcEntity().posY, this.getMcEntity().posZ);
    }

    @Override
    public PositionData getLastTickPosition() {
        return new PositionData(this.getMcEntity().lastTickPosX, this.getMcEntity().lastTickPosY, this.getMcEntity().lastTickPosZ);
    }

    @Override
    public void setPosition(PositionData position) {
        this.getMcEntity().setPosition(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public RotationData getRotation() {
        return new RotationData(this.getMcEntity().rotationYaw, this.getMcEntity().rotationPitch);
    }

    @Override
    public void setRotation(RotationData rotation) {
        this.getMcEntity().setRotation(rotation.getYaw(), rotation.getPitch());
    }

    @Override
    public float getFallDistance() {
        return this.getMcEntity().getFallDistance();
    }

    @Override
    public void setFallDistance(float distance) {
        this.getMcEntity().fallDistance = distance;
    }

    @Override
    public boolean isOnGround() {
        return this.getMcEntity().isOnGround();
    }

    @Override
    public void setOnGround(boolean onGround) {
        this.getMcEntity().onGround = onGround;
    }

    @Override
    public boolean isMoving() {
        return this.getMcEntity().isMoving();
    }

    @Override
    public boolean isCollidedHorizontally() {
        return this.getMcEntity().isCollidedHorizontally;
    }

    @Override
    public boolean isCollidedVertically() {
        return this.getMcEntity().isCollidedVertically;
    }

    @Override
    public int getTicksExisted() {
        return this.getMcEntity().getTicksExisted();
    }

    @Override
    public String getName() {
        return this.getMcEntity().getName();
    }

    @Override
    public String getDisplayName() {
        return this.getMcEntity().getDisplayName().getUnformattedText();
    }

    @Override
    public boolean isInvisible() {
        return this.getMcEntity().isInvisible();
    }

    @Override
    public boolean inRange(double range) {
        return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(mcEntity) <= range;
    }

    @Override
    public double getDistanceToPosition(PositionData position) {
        return this.getMcEntity().getDistance(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public UUID getUUID() {
        return this.getMcEntity().getUUID();
    }

    @Override
    public EnumDirection getDirection() {
        return this.getMcEntity().getHorizontalFacing().toEnumDirection();
    }

    @Override
    public int getEntityId() {
        return this.getMcEntity().getEntityId();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(new Vec3Data(this.getMcEntity().boundingBox.minX, this.getMcEntity().boundingBox.minY, this.getMcEntity().boundingBox.minZ), new Vec3Data(this.getMcEntity().boundingBox.maxX, this.getMcEntity().boundingBox.maxY, this.getMcEntity().boundingBox.maxZ));
    }
}
