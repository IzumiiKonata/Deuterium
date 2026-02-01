package tritium.bridge.game.item;

import net.minecraft.entity.player.InventoryPlayer;
import today.opai.api.interfaces.game.Inventory;
import today.opai.api.interfaces.game.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:11
 */
public class InventoryWrapper implements Inventory {

    InventoryPlayer mcInventory;

    public InventoryWrapper(InventoryPlayer mcInventory) {
        this.mcInventory = mcInventory;
    }

    @Override
    public List<ItemStack> getMainInventory() {
        return Arrays.stream(this.mcInventory.mainInventory).map(net.minecraft.item.ItemStack::getWrapper).collect(Collectors.toList());
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.mcInventory.getStackInSlot(slot).getWrapper();
    }

    @Override
    public int getSize() {
        return this.mcInventory.getSizeInventory();
    }
}
