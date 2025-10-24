package tritium.bridge.game.data;

import today.opai.api.interfaces.game.world.Block;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:33
 */
public class BlockWrapper implements Block {

    private net.minecraft.block.Block mcBlock;

    public BlockWrapper(net.minecraft.block.Block mcBlock) {
        this.mcBlock = mcBlock;
    }

    @Override
    public double getHeight() {
        return mcBlock.maxY - mcBlock.minY;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getName() {
        return mcBlock.getUnlocalizedName();
    }

    @Override
    public int getVariant() {
        throw new UnsupportedOperationException("Not implemented");
//        return mcBlock.;
    }

    @Override
    public double getWidth() {
        return Math.max(mcBlock.maxX - mcBlock.minX, mcBlock.maxZ - mcBlock.minZ);
    }

    @Override
    public boolean isLiquid() {
        return mcBlock.getMaterial().isLiquid();
    }

    @Override
    public boolean isSolid() {
        return mcBlock.isSolidFullCube();
    }

    @Override
    public boolean isOpaque() {
        return mcBlock.isOpaqueCube();
    }

    @Override
    public double getLength() {
        throw new UnsupportedOperationException("Not implemented");
//        return ;
    }

    @Override
    public boolean isTranslucent() {
        return mcBlock.isTranslucent();
    }
}
