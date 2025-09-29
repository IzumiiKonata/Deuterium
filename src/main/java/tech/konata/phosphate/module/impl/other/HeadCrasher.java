package tech.konata.phosphate.module.impl.other;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import tech.konata.phosphate.event.eventapi.Handler;
import tech.konata.phosphate.event.events.world.TickEvent;
import tech.konata.phosphate.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeadCrasher extends Module {

    public HeadCrasher() {
        super("Head Crasher", Category.OTHER);
    }

    @Override
    public void onEnable() {
//        if (!mc.thePlayer.capabilities.isCreativeMode) {
//            mc.thePlayer.addChatMessage("[ERR] You need to be at creative mode to execute this command!");
//            this.toggle();
//        }
    }

    boolean next = true;

    @Handler
    public void onTick(TickEvent event) {

        if (event.isPre()) {
            if (next) {
                this.getHead(this.randStringDistinct(3));
                next = false;
            }

        } else {

            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {

                if (mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.skull) {
                    mc.playerController.clickBlock(mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit);
                    mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit,
                            mc.objectMouseOver.hitVec)) {
                        mc.thePlayer.swingItem();
                        next = true;

                    }
                } else {
                    mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit,
                            mc.objectMouseOver.hitVec)) {
                        mc.thePlayer.swingItem();
                        next = true;
                    }
                }

            }

        }

    }

    private void getHead(String playerName) {
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

    static List<Character> characters = new ArrayList<>();

    static {
        for (int i = 'A'; i <= 'Z'; i++) {
            characters.add((char) (i));
        }

        for (int i = 'a'; i <= 'z'; i++) {
            characters.add((char) (i));
        }

        for (int i = '0'; i <= '9'; i++) {
            characters.add((char) (i));
        }
    }

    List<String> used = new ArrayList<>();

    private String randStringDistinct(int length) {

        while (used.size() >= Math.pow(characters.size(), length)) {
            length += 1;
        }

        StringBuilder result;

        while (true) {
            result = new StringBuilder();

            for (int i = 0; i < length; i++) {
                result.append(characters.get(Math.abs(new Random().nextInt()) % characters.size()));
            }

            if (!used.contains(result.toString())) {
                break;
            }
        }

        return result.toString();
    }

}
