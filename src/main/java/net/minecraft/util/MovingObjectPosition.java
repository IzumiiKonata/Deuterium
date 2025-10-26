package net.minecraft.util;

import lombok.Getter;
import net.minecraft.entity.Entity;
import today.opai.api.interfaces.game.entity.raytrace.EmptyRaytraceResult;
import today.opai.api.interfaces.game.entity.raytrace.RaytraceResult;
import tritium.bridge.game.data.raytrace.BlockRaytraceResultWrapper;
import tritium.bridge.game.data.raytrace.EntityRaytraceResultWrapper;

public class MovingObjectPosition {
    private BlockPos blockPos;

    /**
     * What type of ray trace hit was this? 0 = block, 1 = entity
     */
    public MovingObjectPosition.MovingObjectType typeOfHit;
    public EnumFacing sideHit;

    /**
     * The vector position of the hit
     */
    public Vec3 hitVec;

    /**
     * The hit entity
     */
    public Entity entityHit;

    public MovingObjectPosition(Vec3 hitVecIn, EnumFacing facing, BlockPos blockPosIn) {
        this(MovingObjectPosition.MovingObjectType.BLOCK, hitVecIn, facing, blockPosIn);
    }

    public MovingObjectPosition(Vec3 p_i45552_1_, EnumFacing facing) {
        this(MovingObjectPosition.MovingObjectType.BLOCK, p_i45552_1_, facing, BlockPos.ORIGIN);
    }

    public MovingObjectPosition(Entity entityIn) {
        this(entityIn, new Vec3(entityIn.posX, entityIn.posY, entityIn.posZ));
    }

    @Getter
    private RaytraceResult raytraceResult;

    public MovingObjectPosition(MovingObjectPosition.MovingObjectType typeOfHitIn, Vec3 hitVecIn, EnumFacing sideHitIn, BlockPos blockPosIn) {
        this.typeOfHit = typeOfHitIn;
        this.blockPos = blockPosIn;
        this.sideHit = sideHitIn;
        this.hitVec = new Vec3(hitVecIn.xCoord, hitVecIn.yCoord, hitVecIn.zCoord);
        this.createRaytraceResult();
    }

    public MovingObjectPosition(Entity entityHitIn, Vec3 hitVecIn) {
        this.typeOfHit = MovingObjectPosition.MovingObjectType.ENTITY;
        this.entityHit = entityHitIn;
        this.hitVec = hitVecIn;
        this.createRaytraceResult();
    }

    private void createRaytraceResult() {
        switch (this.typeOfHit) {
            case BLOCK:
                this.raytraceResult = new BlockRaytraceResultWrapper(this);
                break;

            case ENTITY:
                this.raytraceResult = new EntityRaytraceResultWrapper(this);
                break;

            case MISS:
            default:
                this.raytraceResult = new EmptyRaytraceResult() {
                };
                break;
        }
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public String toString() {
        return "HitResult{type=" + this.typeOfHit + ", blockpos=" + this.blockPos + ", f=" + this.sideHit + ", pos=" + this.hitVec + ", entity=" + this.entityHit + '}';
    }

    public enum MovingObjectType {
        MISS,
        BLOCK,
        ENTITY
    }

    public Vec3 getHitVec() {
        return this.hitVec;
    }
}
