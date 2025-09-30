package tritium.event.events;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import tritium.event.eventapi.EventCancellable;

import java.util.List;

@Getter
public class ChatComponentEvent extends EventCancellable {

    private final List<ChatLine> chatLines;
    @Setter
    private IChatComponent component;

    public ChatComponentEvent(IChatComponent p_i1496_1_, List<ChatLine> p_i1496_2_) {
        this.component = p_i1496_1_;
        this.chatLines = p_i1496_2_;
    }

}
