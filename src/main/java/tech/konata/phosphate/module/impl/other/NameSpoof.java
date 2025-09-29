package tech.konata.phosphate.module.impl.other;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.rendering.RenderTextEvent;
import tech.konata.phosphate.module.Module;

public class NameSpoof extends Module {

    public NameSpoof() {
        super("Name Spoof", Category.OTHER);
    }

    public String name = "You";

    @Handler
    public void onRenderString(RenderTextEvent event) {
        event.setText(event.getText().replaceAll(mc.getSession().getUsername(), name));
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
