package tech.konata.phosphate.event.events.player;

import lombok.Getter;
import lombok.Setter;
import tech.konata.phosphate.event.eventapi.EventStateCancellable;

@Getter
public class UpdateEvent extends EventStateCancellable {
    private float rotationYaw, rotationPitch;

    @Setter
    private double posX, posY, posZ;

    @Setter
    private boolean onGround;

    @Getter
    private boolean modified = false;

    public UpdateEvent(float yaw, float pitch, double x, double y, double z, boolean onGround) {
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.onGround = onGround;
    }


    public void setRotationYaw(float rotationYaw) {
        this.rotationYaw = rotationYaw;
        modified = true;
    }

    public void setRotationPitch(float rotationPitch) {
        this.rotationPitch = rotationPitch;
        modified = true;
    }


}
