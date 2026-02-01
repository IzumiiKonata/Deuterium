package tritium.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import tritium.utils.i18n.Localizable;
import tritium.interfaces.IFontRenderer;
import tritium.management.FontManager;
import tritium.settings.ClientSettings;
import tritium.widget.Widget;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * @since 2024/11/3 12:49
 */
public class Coords extends Widget {

    public Coords() {
        super("Coords");
    }

    DecimalFormat df = new DecimalFormat("##.#");

    Localizable lBiome = Localizable.of("biome.name");

    @Override
    public void onRender(boolean editing) {
        IFontRenderer fr = ClientSettings.WIDGETS_USE_VANILLA_FONT_RENDERER.getValue() ? FontManager.vanilla : FontManager.pf25;

        List<String> text = this.getText();

        double spacing = 6;

        this.renderStyledBackground(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 8);

        GlStateManager.pushMatrix();
        this.doScale();

        double textWidth = fr == FontManager.vanilla ? 80 : 100;
        double offsetY = this.getY() + spacing;

        for (String s : text) {
            fr.drawString(s, this.getX() + spacing, offsetY, -1);
            textWidth = Math.max(textWidth, fr.getStringWidth(s));
            offsetY += fr.getHeight() + 2;
        }

        this.setWidth(spacing * 2 + textWidth);
        this.setHeight(spacing * 2 + (fr.getHeight() + 2) * text.size() - 2);

        GlStateManager.popMatrix();
    }

    public List<String > getText() {
        Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(new BlockPos(mc.thePlayer));
        String biome = chunk.getBiome(new BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).lBiomeName.get();
        return Arrays.asList(
                "X: " + df.format(mc.thePlayer.posX),
                "Y: " + df.format(mc.thePlayer.posY),
                "Z: " + df.format(mc.thePlayer.posZ),
                lBiome.get() + ": " + biome
        );
    }
}
