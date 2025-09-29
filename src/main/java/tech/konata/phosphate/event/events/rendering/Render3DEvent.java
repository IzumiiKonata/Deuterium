package tech.konata.phosphate.event.events.rendering;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.konata.phosphate.event.eventapi.Event;

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
