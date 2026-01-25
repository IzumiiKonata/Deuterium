package net.minecraft.client.resources;

import net.minecraft.client.gui.GuiScreenResourcePacks;

public class ResourcePackListEntryFound extends ResourcePackListEntry {
    private final ResourcePackRepository.Entry entry;

    public ResourcePackListEntryFound(GuiScreenResourcePacks resourcePacksGUIIn, ResourcePackRepository.Entry entry) {
        super(resourcePacksGUIIn);
        this.entry = entry;
    }

    protected void bindResourcePackIcon() {
        this.entry.bindTexturePackIcon(this.mc.getTextureManager());
    }

    protected int getPackFormat() {
        return this.entry.getPackFormat();
    }

    protected String getDescription() {
        return this.entry.getTexturePackDescription();
    }

    protected String getName() {
        return this.entry.getResourcePackName();
    }

    public ResourcePackRepository.Entry getEntry() {
        return this.entry;
    }
}
