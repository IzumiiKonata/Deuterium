package tech.konata.phosphate.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import tech.konata.phosphate.widget.Widget;

/**
 * @author IzumiiKonata
 * @since 6/20/2023 9:42 AM
 */
public class Armor extends Widget {

    public Armor() {
        super("Armor");
    }

    @Override
    public void onRender(boolean editing) {
        GL11.glPushMatrix();
        int x = (int) this.getX();
        int y = (int) this.getY();
        for (int index = 3; index >= 0; --index) {
            ItemStack armor = mc.thePlayer.inventory.armorInventory[index];
            if (armor == null) {
                continue;
            }
            if (mc.theWorld != null) {
                this.renderItem(armor, x, y);
            }
            int damage = armor.getMaxDamage() - armor.getItemDamage();
            GlStateManager.enableAlpha();
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();
            GlStateManager.clear(256);
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(damage), x + 18, y + 5, -1);
            y += 18;
        }
        GL11.glPopMatrix();
        this.setWidth(40);
        this.setHeight(72);
    }

    private void renderItem(ItemStack itemStack, int x, int y) {
        RenderItem ir = mc.getRenderItem();
        RenderHelper.disableStandardItemLighting();
        ir.renderItemIntoGUI(itemStack, x, y);
        ir.renderItemOverlays(mc.fontRendererObj, itemStack, x, y);
        RenderHelper.enableGUIStandardItemLighting();
    }

}
