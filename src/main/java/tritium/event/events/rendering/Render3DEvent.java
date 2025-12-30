package tritium.event.events.rendering;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Render3DEvent extends Event {

    private static final Render3DEvent INSTANCE = new Render3DEvent(0.0F);

    public float partialTicks;

    public static Render3DEvent of(float partialTicks) {
        INSTANCE.partialTicks = partialTicks;
        return INSTANCE;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Render3DBeforeEntityPassEvent extends Event {

        private static final Render3DBeforeEntityPassEvent INSTANCE = new Render3DBeforeEntityPassEvent(0.0F);

        public float partialTicks;

        public static Render3DBeforeEntityPassEvent of(float partialTicks) {
            INSTANCE.partialTicks = partialTicks;
            return INSTANCE;
        }
    }
}
