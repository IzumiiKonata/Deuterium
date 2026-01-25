package tritium.bridge.game.data.raytrace;

import today.opai.api.dataset.Vec3Data;
import today.opai.api.interfaces.game.entity.Entity;
import today.opai.api.interfaces.game.entity.raytrace.EntityRaytraceResult;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:23
 */
public class EntityRaytraceResultWrapper implements EntityRaytraceResult {

    private final net.minecraft.util.MovingObjectPosition mcRaytraceResult;

    public EntityRaytraceResultWrapper(net.minecraft.util.MovingObjectPosition mcRaytraceResult) {
        this.mcRaytraceResult = mcRaytraceResult;
    }

    @Override
    public Entity getEntity() {
        return this.mcRaytraceResult.entityHit.getWrapper();
    }

    @Override
    public Vec3Data getHitVector() {
        return new Vec3Data(this.mcRaytraceResult.hitVec.xCoord, this.mcRaytraceResult.hitVec.yCoord, this.mcRaytraceResult.hitVec.zCoord);
    }
}
