package tech.konata.phosphate.utils.player;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.Objects;

/**
 * @author IzumiiKonata
 * @since 2022/7/20 10:56
 */
public class BlockUtils {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isOnLiquid() {
        AxisAlignedBB axisAlignedBB = mc.thePlayer.getEntityBoundingBox().offset(0.0, -0.1, 0.0).contract(0.001D, 0.0D, 0.001D);

        int xMin = MathHelper.floor_double(axisAlignedBB.minX);
        int xMax = MathHelper.floor_double(axisAlignedBB.maxX + 1.0);
        int yMin = MathHelper.floor_double(axisAlignedBB.minY);
        int yMax = MathHelper.floor_double(axisAlignedBB.maxY + 1.0);
        int zMin = MathHelper.floor_double(axisAlignedBB.minZ);
        int zMax = MathHelper.floor_double(axisAlignedBB.maxZ + 1.0);

        boolean gotcha = false;

        for (int y = yMin; y < yMax; y++) {
            for (int x = xMin; x < xMax; x++) {
                for (int z = zMin; z < zMax; z++) {
                    Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();

                    if (block instanceof BlockLiquid)
                        gotcha = true;

                    if (!(block instanceof BlockLiquid) && block.getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z), mc.theWorld.getBlockState(new BlockPos(x, y, z))) != null) {
                        return false;
                    }
                }
            }
        }

        return gotcha;
    }

    public static boolean isInLiquid() {
        final AxisAlignedBB par1AxisAlignedBB = mc.thePlayer.getEntityBoundingBox().contract(0.001, 0.001,
                0.001);
        final int var4 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        final int var5 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0);
        final int var6 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        final int var7 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0);
        final int var8 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        final int var9 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0);
        for (int var11 = var4; var11 < var5; ++var11) {
            for (int var12 = var6; var12 < var7; ++var12) {
                for (int var13 = var8; var13 < var9; ++var13) {
                    final BlockPos pos = new BlockPos(var11, var12, var13);
                    final Block var14 = mc.theWorld.getBlockState(pos).getBlock();
                    if (var14 instanceof BlockLiquid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isOnLadder() {
        boolean onLadder = false;
        final int y = (int) mc.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0).minY;
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; ++z) {
                final Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (Objects.nonNull(block) && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLadder) && !(block instanceof BlockVine)) {
                        return false;
                    }
                    onLadder = true;
                }
            }
        }
        return onLadder || mc.thePlayer.isOnLadder();
    }
}
