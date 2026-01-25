package net.minecraft.client.audio;

import net.minecraft.util.Location;

public abstract class MovingSound extends PositionedSound implements ITickableSound {
    protected boolean donePlaying = false;

    protected MovingSound(Location location) {
        super(location);
    }

    public boolean isDonePlaying() {
        return this.donePlaying;
    }
}
