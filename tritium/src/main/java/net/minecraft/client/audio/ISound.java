package net.minecraft.client.audio;

import net.minecraft.util.Location;

public interface ISound {
    Location getSoundLocation();

    boolean canRepeat();

    int getRepeatDelay();

    float getVolume();

    float getPitch();

    float getXPosF();

    float getYPosF();

    float getZPosF();

    ISound.AttenuationType getAttenuationType();

    enum AttenuationType {
        NONE(0),
        LINEAR(2);

        private final int type;

        AttenuationType(int typeIn) {
            this.type = typeIn;
        }

        public int getTypeInt() {
            return this.type;
        }
    }
}
