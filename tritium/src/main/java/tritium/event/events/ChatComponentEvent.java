package tritium.event.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import tritium.event.eventapi.EventCancellable;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatComponentEvent extends EventCancellable {

    private static final ChatComponentEvent INSTANCE = new ChatComponentEvent(null, null);

    private List<ChatLine> chatLines;
    @Setter
    private IChatComponent component;

    public static ChatComponentEvent of(IChatComponent component, List<ChatLine> chatLines) {
        INSTANCE.component = component;
        INSTANCE.chatLines = chatLines;
        return INSTANCE;
    }

}
