package tritium.bridge.game.item;

import lombok.Getter;
import net.minecraft.item.Item;
import today.opai.api.interfaces.game.item.ItemStack;
import today.opai.api.interfaces.render.ItemUtil;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 18:46
 */
public class ItemUtilImpl implements ItemUtil {

    @Getter
    private static final ItemUtilImpl instance = new ItemUtilImpl();

    @Override
    public ItemStack fromData(String name, int count, int meta) {
        return new net.minecraft.item.ItemStack(Item.getFromUnlocalizedName(name), count, meta).getWrapper();
    }

    @Override
    public ItemStack fromName(String name) {
        return new net.minecraft.item.ItemStack(Item.getFromUnlocalizedName(name)).getWrapper();
    }

    @Override
    public ItemStack fromData(String name, int count, int meta, String nbt) {
        net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(Item.getFromUnlocalizedName(name), count, meta);
        return itemStack.getWrapper();
    }
}
