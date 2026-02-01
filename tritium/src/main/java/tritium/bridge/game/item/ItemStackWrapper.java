package tritium.bridge.game.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagList;
import today.opai.api.interfaces.game.item.ItemEnchantment;
import today.opai.api.interfaces.game.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 16:54
 */
public class ItemStackWrapper implements ItemStack {

    private final net.minecraft.item.ItemStack mcItemStack;

    public ItemStackWrapper(net.minecraft.item.ItemStack mcItemStack) {
        this.mcItemStack = mcItemStack;
    }

    @Override
    public String getDisplayName() {
        return this.mcItemStack.getDisplayName();
    }

    @Override
    public String getName() {
        return this.mcItemStack.getName();
    }

    @Override
    public int getStackSize() {
        return this.mcItemStack.getStackSize();
    }

    @Override
    public int getMaxStackSize() {
        return this.mcItemStack.getMaxStackSize();
    }

    @Override
    public int getDurability() {
        return this.mcItemStack.getDurability();
    }

    @Override
    public int getMaxDurability() {
        return this.mcItemStack.getMaxDurability();
    }

    @Override
    public int getMetadata() {
        return this.mcItemStack.getMetadata();
    }

    @Override
    public List<String> getLore() {
        return this.mcItemStack.getLore();
    }

    @Override
    public List<ItemEnchantment> getEnchantments() {
        List<ItemEnchantment> result = new ArrayList<>();

        if (this.mcItemStack.hasTagCompound()) {
            NBTTagList nbttaglist = this.mcItemStack.getEnchantmentTagList();

            if (nbttaglist != null) {
                for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                    int k = nbttaglist.getCompoundTagAt(j).getShort("id");
                    int l = nbttaglist.getCompoundTagAt(j).getShort("lvl");

                    Enchantment ench = Enchantment.getEnchantmentById(k);

                    if (ench != null) {
                        result.add(new ItemEnchantment() {
                            @Override
                            public String getName() {
                                return ench.getName();
                            }

                            @Override
                            public int getLevel() {
                                return l;
                            }
                        });
                    }
                }
            }
        }

        return result;
    }

}
