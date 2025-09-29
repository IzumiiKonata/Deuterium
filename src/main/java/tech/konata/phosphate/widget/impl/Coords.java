package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import tech.konata.phosphate.utils.i18n.Localizable;
import tech.konata.phosphate.interfaces.IFontRenderer;
import tech.konata.phosphate.management.FontManager;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.widget.Widget;

import java.text.DecimalFormat;

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
        boolean bFlagVanilla = GlobalSettings.HUD_STYLE.getValue() == GlobalSettings.HudStyle.Vanilla;
        IFontRenderer fr = bFlagVanilla ? mc.fontRendererObj : FontManager.pf25;

        String[] text = this.getText();

        double spacing = 6;


        NORMAL.add(() -> {

            this.renderStyledBackground(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 8);

            GlStateManager.pushMatrix();
            this.doScale();

            double textWidth = 100;
            double offsetY = this.getY() + spacing + (bFlagVanilla ? 1 : 0);

            for (String s : text) {
                fr.drawString(s, this.getX() + spacing, offsetY, -1);
                textWidth = Math.max(textWidth, fr.getStringWidth(s));
                offsetY += fr.getHeight() + 2;
            }

            this.setWidth(spacing * 2 + textWidth);
            this.setHeight(spacing * 2 + (fr.getHeight() + 2) * text.length - 2);

            GlStateManager.popMatrix();
        });
    }

    public String[] getText() {
        Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(new BlockPos(mc.thePlayer));
        String biome = chunk.getBiome(new BlockPos(mc.thePlayer), this.mc.theWorld.getWorldChunkManager()).lBiomeName.get();
        return new String[] { "X: " + df.format(mc.thePlayer.posX), "Y: " + df.format(mc.thePlayer.posY), "Z: " + df.format(mc.thePlayer.posZ), lBiome.get() + ": " + biome };
    }
}
