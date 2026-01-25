package tritium.event.events.rendering;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tritium.event.eventapi.Event;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DisplayResizedEvent extends Event {

    private static final DisplayResizedEvent INSTANCE = new DisplayResizedEvent(0, 0, 0, 0);

    private int beforeWidth, beforeHeight, nowWidth, nowHeight;

    public static DisplayResizedEvent of(int beforeWidth, int beforeHeight, int nowWidth, int nowHeight) {
        INSTANCE.beforeWidth = beforeWidth;
        INSTANCE.beforeHeight = beforeHeight;
        INSTANCE.nowWidth = nowWidth;
        INSTANCE.nowHeight = nowHeight;
        return INSTANCE;
    }
}
