package tritium.module.impl.other;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import tritium.event.eventapi.Handler;
import tritium.event.events.ChatComponentEvent;
import tritium.event.events.rendering.RenderTextEvent;
import tritium.module.Module;

public class NameSpoof extends Module {

    public NameSpoof() {
        super("Name Spoof", Category.OTHER);
    }

    public String name = "You";

    @Handler
    public void onRenderString(RenderTextEvent event) {
//        event.setText(event.getText().replaceAll(mc.getSession().getUsername(), name));
    }

    private void replaceChatComponentText(ChatComponentText text) {
        text.setText(text.getChatComponentText_TextValue().replace(mc.getSession().getUsername(), name));
    }

    @Handler
    public void onChatComponentEvent(ChatComponentEvent event) {

        this.replaceChatComponent(event.getComponent());

    }
    
    public void replaceChatComponent(IChatComponent component) {
        if (component instanceof ChatComponentText) {
            ChatComponentText chatComponentText = (ChatComponentText) component;

            replaceChatComponentText(chatComponentText);

            for (IChatComponent sibling : chatComponentText.getSiblings()) {
                if (sibling instanceof ChatComponentText)
                    replaceChatComponentText((ChatComponentText) sibling);
            }

            return;
        }

        if (component instanceof ChatComponentTranslation) {
            ChatComponentTranslation chatComponentTranslation = (ChatComponentTranslation) component;
            for (IChatComponent child : chatComponentTranslation.getChildren()) {
                if (child instanceof ChatComponentText) {
                    replaceChatComponentText((ChatComponentText) child);
                }
            }

            for (IChatComponent sibling : chatComponentTranslation.getSiblings()) {
                if (sibling instanceof ChatComponentText) {
                    replaceChatComponentText((ChatComponentText) sibling);
                }
            }
        }
    }

    @Override
    public JsonObject saveConfig() {

        JsonObject jsonObject = super.saveConfig();

        jsonObject.addProperty("Name", this.name);

        return jsonObject;
    }

    @Override
    public void loadConfig(JsonObject directory) {
        super.loadConfig(directory);

        if (directory.get("Name") instanceof JsonNull)
            return;

        this.name = directory.get("Name").getAsString();
    }

    public String getSpoofedName() {
        return this.name.replaceAll("&", "\247");
    }
}
