package tritium.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

@Getter
@AllArgsConstructor
public final class Render3DEvent extends Event {

    public final float partialTicks;

    @Getter
    @AllArgsConstructor
    public static final class OldRender3DEvent extends Event {
        public final float partialTicks;
    }
}
