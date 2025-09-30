package tritium.command.impl;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tritium.command.Command;

public class GetSelfHead extends Command {

    public GetSelfHead() {
        super("selfhead", "Get Player Head", "sh", "sh");
    }

    @Override
    public void execute(String[] args) {
        if (!mc.thePlayer.capabilities.isCreativeMode) {
            this.print("[ERR] You need to be at creative mode to execute this command!");
            return;
        }

        String playerName = mc.getSession().getUsername();

        if (args.length > 0)
            playerName = args[0];

        InventoryPlayer inventoryplayer = mc.thePlayer.inventory;

        ItemStack itemstack = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound nbttagcompound3 = new NBTTagCompound();
        NBTTagCompound nbttagcompound2 = new NBTTagCompound();

        String u = mc.getSession().getPlayerID();

//        nbttagcompound2.setString("Id", u.substring(0, 8) + "-" + u.substring(8, 12) + "-" + u.substring(12, 16) + "-" + u.substring(16, 20) + "-" + u.substring(20));
        nbttagcompound2.setString("Name", playerName);
        nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
        itemstack.setTagCompound(nbttagcompound3);

        System.out.println(itemstack.getTagCompound());

        inventoryplayer.setInventorySlotContents(inventoryplayer.currentItem, itemstack);

        int j = mc.thePlayer.inventoryContainer.inventorySlots.size() - 9 + inventoryplayer.currentItem;
        mc.playerController.sendSlotPacket(inventoryplayer.getStackInSlot(inventoryplayer.currentItem), j);

    }
}
