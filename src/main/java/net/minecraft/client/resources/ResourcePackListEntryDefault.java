package net.minecraft.client.resources;

import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Location;
import tritium.utils.logging.LogManager;
import tritium.utils.logging.Logger;

import java.io.IOException;

public class ResourcePackListEntryDefault extends ResourcePackListEntry {
    private static final Logger logger = LogManager.getLogger("ResourcePackListEntryDefault");
    private final IResourcePack field_148320_d;
    private final Location resourcePackIcon;

    public ResourcePackListEntryDefault(GuiScreenResourcePacks resourcePacksGUIIn) {
        super(resourcePacksGUIIn);
        this.field_148320_d = this.mc.getResourcePackRepository().rprDefaultResourcePack;
        DynamicTexture dynamictexture;

        try {
            dynamictexture = new DynamicTexture(this.field_148320_d.getPackImage());
        } catch (IOException var4) {
            dynamictexture = TextureUtil.missingTexture;
        }

        this.resourcePackIcon = this.mc.getTextureManager().getDynamicTextureLocation("texturepackicon", dynamictexture);
    }

    protected int getPackFormat() {
        return 1;
    }

    protected String getDescription() {
        try {
            PackMetadataSection packmetadatasection = this.field_148320_d.getPackMetadata(this.mc.getResourcePackRepository().rprMetadataSerializer, "pack");

            if (packmetadatasection != null) {
                return packmetadatasection.getPackDescription().getFormattedText();
            }
        } catch (JsonParseException jsonparseexception) {
            logger.error("Couldn't load metadata info", jsonparseexception);
        } catch (IOException ioexception) {
            logger.error("Couldn't load metadata info", ioexception);
        }

        return EnumChatFormatting.RED + "Missing " + "pack.mcmeta" + " :(";
    }

    protected boolean canBeSelected() {
        return false;
    }

    protected boolean canBeUnselected() {
        return false;
    }

    protected boolean canMoveUp() {
        return false;
    }

    protected boolean canMoveDown() {
        return false;
    }

    protected String getName() {
        return "Default";
    }

    protected void bindResourcePackIcon() {
        this.mc.getTextureManager().bindTexture(this.resourcePackIcon);
    }

    protected boolean canBeMoved() {
        return false;
    }
}
