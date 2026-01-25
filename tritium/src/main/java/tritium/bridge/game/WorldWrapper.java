package tritium.bridge.game;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import today.opai.api.dataset.BlockPosition;
import today.opai.api.dataset.BoundingBox;
import today.opai.api.dataset.Vec3Data;
import today.opai.api.interfaces.game.entity.Entity;
import today.opai.api.interfaces.game.entity.Player;
import today.opai.api.interfaces.game.world.Block;
import today.opai.api.interfaces.game.world.World;
import tritium.bridge.entity.PlayerWrapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/24 17:29
 */
public class WorldWrapper implements World {

    private final net.minecraft.world.World mcWorld;

    public WorldWrapper(net.minecraft.world.World mcWorld) {
        this.mcWorld = mcWorld;
    }

    @Override
    public int getBlock(BlockPosition position) {
        return net.minecraft.block.Block.getIdFromBlock(mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock());
    }

    @Override
    public Entity getEntityByID(int id) {
        return mcWorld.getEntityByID(id).getWrapper();
    }

    @Override
    public List<Entity> getLoadedEntities() {
        return mcWorld.getLoadedEntityList().stream().map(net.minecraft.entity.Entity::getWrapper).collect(Collectors.toList());
    }

    @Override
    public List<Player> getLoadedPlayerEntities() {
        return mcWorld.playerEntities.stream().map(p -> (PlayerWrapper<?>) p.getWrapper()).collect(Collectors.toList());
    }

    @Override
    public void removeEntity(Entity entity) {
        mcWorld.removeEntity(mcWorld.getEntityByID(entity.getEntityId()));
    }

    @Override
    public Block getBlockFromPosition(BlockPosition position) {
        return mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock().getWrapper();
    }

    @Override
    public BoundingBox getBoundingBox(BlockPosition position) {
        net.minecraft.block.Block block = mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock();
        AxisAlignedBB collisionBoundingBox = block.getCollisionBoundingBox(mcWorld, new BlockPos(position.getX(), position.getY(), position.getZ()), block.getDefaultState());
        return new BoundingBox(new Vec3Data(collisionBoundingBox.minX, collisionBoundingBox.minY, collisionBoundingBox.minZ), new Vec3Data(collisionBoundingBox.maxX, collisionBoundingBox.maxY, collisionBoundingBox.maxZ));
    }

    @Override
    public boolean isLiquid(BlockPosition position) {
        return mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock().getMaterial().isLiquid();
    }

    @Override
    public boolean isSolid(BlockPosition position) {
        return mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock().isSolidFullCube();
    }

    @Override
    public boolean isOpaque(BlockPosition position) {
        return mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock().isOpaqueCube();
    }

    @Override
    public boolean isTranslucent(BlockPosition position) {
        return mcWorld.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getBlock().isTranslucent();
    }

    @Override
    public boolean isSingleplayer() {
        return !mcWorld.isRemote;
    }

    @Override
    public String getScreenTitle() {
        Container cont = Minecraft.getMinecraft().thePlayer.openContainer;
        return !(cont instanceof ContainerChest) ? null : ((ContainerChest) cont).getLowerChestInventory().getName();
    }

    @Override
    public String getScoreboardTitle() {
        Scoreboard scoreboard = mcWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(Minecraft.getMinecraft().thePlayer.getName());

        if (scoreplayerteam != null) {
            int i1 = scoreplayerteam.getChatFormat().getColorIndex();

            if (i1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
            }
        }

        ScoreObjective objective = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (objective != null) {
            return objective.getDisplayName();
        }

        return null;
    }

    @Override
    public List<String> getScoreboardNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getScoreboardLines() {
        return Collections.emptyList();
    }
}
