package tritium.bridge.entity;

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
        return new Vector3dImpl(mcEntity.motionX, mcEntity.motionY, mcEntity.motionZ);
    }

    @Override
    public void setMotion(Vec3Data motion) {
        mcEntity.motionX = motion.getX();
        mcEntity.motionY = motion.getY();
        mcEntity.motionZ = motion.getZ();
    }

    @Override
    public PositionData getPosition() {
        return new PositionData(mcEntity.posX, mcEntity.posY, mcEntity.posZ);
    }

    @Override
    public PositionData getLastTickPosition() {
        return new PositionData(mcEntity.lastTickPosX, mcEntity.lastTickPosY, mcEntity.lastTickPosZ);
    }

    @Override
    public void setPosition(PositionData position) {
        mcEntity.setPosition(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public RotationData getRotation() {
        return new RotationData(mcEntity.rotationYaw, mcEntity.rotationPitch);
    }

    @Override
    public void setRotation(RotationData rotation) {
        mcEntity.setRotation(rotation.getYaw(), rotation.getPitch());
    }

    @Override
    public float getFallDistance() {
        return mcEntity.getFallDistance();
    }

    @Override
    public void setFallDistance(float distance) {
        mcEntity.fallDistance = distance;
    }

    @Override
    public boolean isOnGround() {
        return mcEntity.isOnGround();
    }

    @Override
    public void setOnGround(boolean onGround) {
        mcEntity.onGround = onGround;
    }

    @Override
    public boolean isMoving() {
        return mcEntity.isMoving();
    }

    @Override
    public boolean isCollidedHorizontally() {
        return mcEntity.isCollidedHorizontally;
    }

    @Override
    public boolean isCollidedVertically() {
        return mcEntity.isCollidedVertically;
    }

    @Override
    public int getTicksExisted() {
        return mcEntity.getTicksExisted();
    }

    @Override
    public String getName() {
        return mcEntity.getName();
    }

    @Override
    public String getDisplayName() {
        return mcEntity.getDisplayName().getUnformattedText();
    }

    @Override
    public boolean isInvisible() {
        return mcEntity.isInvisible();
    }

    @Override
    public boolean inRange(double range) {
        return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(mcEntity) <= range;
    }

    @Override
    public double getDistanceToPosition(PositionData position) {
        return mcEntity.getDistance(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public UUID getUUID() {
        return mcEntity.getUUID();
    }

    @Override
    public EnumDirection getDirection() {
        return mcEntity.getHorizontalFacing().toEnumDirection();
    }

    @Override
    public int getEntityId() {
        return mcEntity.getEntityId();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(new Vec3Data(mcEntity.boundingBox.minX, mcEntity.boundingBox.minY, mcEntity.boundingBox.minZ), new Vec3Data(mcEntity.boundingBox.maxX, mcEntity.boundingBox.maxY, mcEntity.boundingBox.maxZ));
    }
}
