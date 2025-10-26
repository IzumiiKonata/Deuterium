package tritium.bridge.game.data.raytrace;

import today.opai.api.dataset.BlockPosition;
import today.opai.api.dataset.Vec3Data;
import today.opai.api.enums.EnumDirection;
import today.opai.api.interfaces.game.entity.raytrace.BlockRaytraceResult;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:19
 */
public class BlockRaytraceResultWrapper implements BlockRaytraceResult {

    private final net.minecraft.util.MovingObjectPosition mcBlockRaytraceResult;

    public BlockRaytraceResultWrapper(net.minecraft.util.MovingObjectPosition mcBlockRaytraceResult) {
        this.mcBlockRaytraceResult = mcBlockRaytraceResult;
    }

    @Override
    public BlockPosition getBlockPosition() {
        return new BlockPosition(this.mcBlockRaytraceResult.getBlockPos().getX(), this.mcBlockRaytraceResult.getBlockPos().getY(), this.mcBlockRaytraceResult.getBlockPos().getZ());
    }

    @Override
    public EnumDirection getDirection() {
        return mcBlockRaytraceResult.sideHit.toEnumDirection();
    }

    @Override
    public Vec3Data getHitVector() {
        return new Vec3Data(this.mcBlockRaytraceResult.hitVec.xCoord, this.mcBlockRaytraceResult.hitVec.yCoord, this.mcBlockRaytraceResult.hitVec.zCoord);
    }
}
