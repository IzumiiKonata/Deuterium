package tritium.event.events.player;

import lombok.Getter;
import lombok.Setter;
import tritium.event.eventapi.EventStateCancellable;

@Getter
public class PlayerUpdateEvent extends EventStateCancellable {

    @Setter
    private double x, y, z;

    private float rotationYaw, rotationPitch;

    @Setter
    private boolean onGround;

    @Getter
    private boolean modified = false;

    public PlayerUpdateEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
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
