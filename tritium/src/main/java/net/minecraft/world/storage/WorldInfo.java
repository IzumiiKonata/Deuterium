package net.minecraft.world.storage;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class WorldInfo {
    public static final EnumDifficulty DEFAULT_DIFFICULTY = EnumDifficulty.NORMAL;

    /**
     * Holds the seed of the currently world.
     */
    private long randomSeed;
    @Setter
    @Getter
    private WorldType terrainType = WorldType.DEFAULT;
    @Getter
    private String generatorOptions = "";

    /**
     * The spawn zone position X coordinate.
     * -- GETTER --
     * Returns the x spawn position
     * -- SETTER --
     * Set the x spawn position to the passed in value
     */
    @Setter
    @Getter
    private int spawnX;

    /**
     * The spawn zone position Y coordinate.
     * -- GETTER --
     * Return the Y axis spawning point of the player.
     * -- SETTER --
     * Sets the y spawn position
     */
    @Setter
    @Getter
    private int spawnY;

    /**
     * The spawn zone position Z coordinate.
     * -- GETTER --
     * Returns the z spawn position
     * -- SETTER --
     * Set the z spawn position to the passed in value
     */
    @Setter
    @Getter
    private int spawnZ;

    /**
     * Total time for this world.
     */
    private long totalTime;

    /**
     * The current world time in ticks, ranging from 0 to 23999.
     * -- GETTER --
     * Get current world time
     * -- SETTER --
     * Set current world time
     */
    @Setter
    @Getter
    private long worldTime;

    /**
     * The last time the player was in this world.
     * -- GETTER --
     * Return the last time the player was in this world.
     */
    @Getter
    private long lastTimePlayed;

    /**
     * The size of entire save of current world on the disk, isn't exactly.
     */
    @Getter
    private long sizeOnDisk;
    private NBTTagCompound playerTag;
    private int dimension;

    /**
     * The name of the save defined at world creation.
     */
    private String levelName;

    /**
     * Introduced in beta 1.3, is the save version for future control.
     * -- GETTER --
     * Returns the save version of this world
     * -- SETTER --
     * Sets the save version of the world
     */
    @Setter
    @Getter
    private int saveVersion;
    @Setter
    @Getter
    private int cleanWeatherTime;

    /**
     * True if it's raining, false otherwise.
     * -- GETTER --
     * Returns true if it is raining, false otherwise.
     * -- SETTER --
     * Sets whether it is raining or not.
     */
    @Setter
    @Getter
    private boolean raining;

    /**
     * Number of ticks until next rain.
     * -- GETTER --
     * Return the number of ticks until rain.
     * -- SETTER --
     * Sets the number of ticks until rain.
     */
    @Setter
    @Getter
    private int rainTime;

    /**
     * Is thunderbolts failing now?
     * -- GETTER --
     * Returns true if it is thundering, false otherwise.
     * -- SETTER --
     * Sets whether it is thundering or not.
     */
    @Setter
    @Getter
    private boolean thundering;

    /**
     * Number of ticks untils next thunderbolt.
     * -- GETTER --
     * Returns the number of ticks until next thunderbolt.
     * -- SETTER --
     * Defines the number of ticks until next thunderbolt.
     */
    @Setter
    @Getter
    private int thunderTime;

    /**
     * The Game Type.
     */
    private WorldSettings.GameType theGameType;

    /**
     * Whether the map features (e.g. strongholds) generation is enabled or disabled.
     * -- GETTER --
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    @Setter
    @Getter
    private boolean mapFeaturesEnabled;

    /**
     * Hardcore mode flag
     */
    @Setter
    private boolean hardcore;
    @Setter
    private boolean allowCommands;
    /**
     * -- GETTER --
     * Returns true if the World is initialized.
     */
    @Getter
    private boolean initialized;
    @Setter
    @Getter
    private EnumDifficulty difficulty;
    @Setter
    @Getter
    private boolean difficultyLocked;
    /**
     * -- GETTER --
     * Returns the border center X position
     */
    @Getter
    private double borderCenterX = 0.0D;
    /**
     * -- GETTER --
     * Returns the border center Z position
     */
    @Getter
    private double borderCenterZ = 0.0D;
    /**
     * -- SETTER --
     * Sets the border size
     */
    @Setter
    @Getter
    private double borderSize = 6.0E7D;
    private long borderSizeLerpTime = 0L;
    private double borderSizeLerpTarget = 0.0D;
    /**
     * -- GETTER --
     * Returns the border safe zone
     * -- SETTER --
     * Sets the border safe zone
     */
    @Setter
    @Getter
    private double borderSafeZone = 5.0D;
    /**
     * -- GETTER --
     * Returns the border damage per block
     * -- SETTER --
     * Sets the border damage per block
     */
    @Setter
    @Getter
    private double borderDamagePerBlock = 0.2D;
    /**
     * -- GETTER --
     * Returns the border warning distance
     * -- SETTER --
     * Sets the border warning distance
     */
    @Setter
    @Getter
    private int borderWarningDistance = 5;
    /**
     * -- GETTER --
     * Returns the border warning time
     * -- SETTER --
     * Sets the border warning time
     */
    @Setter
    @Getter
    private int borderWarningTime = 15;
    private GameRules theGameRules = new GameRules();

    protected WorldInfo() {
    }

    public WorldInfo(NBTTagCompound nbt) {
        this.randomSeed = nbt.getLong("RandomSeed");

        if (nbt.hasKey("generatorName", 8)) {
            String s = nbt.getString("generatorName");
            this.terrainType = WorldType.parseWorldType(s);

            if (this.terrainType == null) {
                this.terrainType = WorldType.DEFAULT;
            } else if (this.terrainType.isVersioned()) {
                int i = 0;

                if (nbt.hasKey("generatorVersion", 99)) {
                    i = nbt.getInteger("generatorVersion");
                }

                this.terrainType = this.terrainType.getWorldTypeForGeneratorVersion(i);
            }

            if (nbt.hasKey("generatorOptions", 8)) {
                this.generatorOptions = nbt.getString("generatorOptions");
            }
        }

        this.theGameType = WorldSettings.GameType.getByID(nbt.getInteger("GameType"));

        if (nbt.hasKey("MapFeatures", 99)) {
            this.mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
        } else {
            this.mapFeaturesEnabled = true;
        }

        this.spawnX = nbt.getInteger("SpawnX");
        this.spawnY = nbt.getInteger("SpawnY");
        this.spawnZ = nbt.getInteger("SpawnZ");
        this.totalTime = nbt.getLong("Time");

        if (nbt.hasKey("DayTime", 99)) {
            this.worldTime = nbt.getLong("DayTime");
        } else {
            this.worldTime = this.totalTime;
        }

        this.lastTimePlayed = nbt.getLong("LastPlayed");
        this.sizeOnDisk = nbt.getLong("SizeOnDisk");
        this.levelName = nbt.getString("LevelName");
        this.saveVersion = nbt.getInteger("version");
        this.cleanWeatherTime = nbt.getInteger("clearWeatherTime");
        this.rainTime = nbt.getInteger("rainTime");
        this.raining = nbt.getBoolean("raining");
        this.thunderTime = nbt.getInteger("thunderTime");
        this.thundering = nbt.getBoolean("thundering");
        this.hardcore = nbt.getBoolean("hardcore");

        if (nbt.hasKey("initialized", 99)) {
            this.initialized = nbt.getBoolean("initialized");
        } else {
            this.initialized = true;
        }

        if (nbt.hasKey("allowCommands", 99)) {
            this.allowCommands = nbt.getBoolean("allowCommands");
        } else {
            this.allowCommands = this.theGameType == WorldSettings.GameType.CREATIVE;
        }

        if (nbt.hasKey("Player", 10)) {
            this.playerTag = nbt.getCompoundTag("Player");
            this.dimension = this.playerTag.getInteger("Dimension");
        }

        if (nbt.hasKey("GameRules", 10)) {
            this.theGameRules.readFromNBT(nbt.getCompoundTag("GameRules"));
        }

        if (nbt.hasKey("Difficulty", 99)) {
            this.difficulty = EnumDifficulty.getDifficultyEnum(nbt.getByte("Difficulty"));
        }

        if (nbt.hasKey("DifficultyLocked", 1)) {
            this.difficultyLocked = nbt.getBoolean("DifficultyLocked");
        }

        if (nbt.hasKey("BorderCenterX", 99)) {
            this.borderCenterX = nbt.getDouble("BorderCenterX");
        }

        if (nbt.hasKey("BorderCenterZ", 99)) {
            this.borderCenterZ = nbt.getDouble("BorderCenterZ");
        }

        if (nbt.hasKey("BorderSize", 99)) {
            this.borderSize = nbt.getDouble("BorderSize");
        }

        if (nbt.hasKey("BorderSizeLerpTime", 99)) {
            this.borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime");
        }

        if (nbt.hasKey("BorderSizeLerpTarget", 99)) {
            this.borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget");
        }

        if (nbt.hasKey("BorderSafeZone", 99)) {
            this.borderSafeZone = nbt.getDouble("BorderSafeZone");
        }

        if (nbt.hasKey("BorderDamagePerBlock", 99)) {
            this.borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock");
        }

        if (nbt.hasKey("BorderWarningBlocks", 99)) {
            this.borderWarningDistance = nbt.getInteger("BorderWarningBlocks");
        }

        if (nbt.hasKey("BorderWarningTime", 99)) {
            this.borderWarningTime = nbt.getInteger("BorderWarningTime");
        }
    }

    public WorldInfo(WorldSettings settings, String name) {
        this.populateFromWorldSettings(settings);
        this.levelName = name;
        this.difficulty = DEFAULT_DIFFICULTY;
        this.initialized = false;
    }

    public void populateFromWorldSettings(WorldSettings settings) {
        this.randomSeed = settings.getSeed();
        this.theGameType = settings.getGameType();
        this.mapFeaturesEnabled = settings.isMapFeaturesEnabled();
        this.hardcore = settings.getHardcoreEnabled();
        this.terrainType = settings.getTerrainType();
        this.generatorOptions = settings.getWorldName();
        this.allowCommands = settings.areCommandsAllowed();
    }

    public WorldInfo(WorldInfo worldInformation) {
        this.randomSeed = worldInformation.randomSeed;
        this.terrainType = worldInformation.terrainType;
        this.generatorOptions = worldInformation.generatorOptions;
        this.theGameType = worldInformation.theGameType;
        this.mapFeaturesEnabled = worldInformation.mapFeaturesEnabled;
        this.spawnX = worldInformation.spawnX;
        this.spawnY = worldInformation.spawnY;
        this.spawnZ = worldInformation.spawnZ;
        this.totalTime = worldInformation.totalTime;
        this.worldTime = worldInformation.worldTime;
        this.lastTimePlayed = worldInformation.lastTimePlayed;
        this.sizeOnDisk = worldInformation.sizeOnDisk;
        this.playerTag = worldInformation.playerTag;
        this.dimension = worldInformation.dimension;
        this.levelName = worldInformation.levelName;
        this.saveVersion = worldInformation.saveVersion;
        this.rainTime = worldInformation.rainTime;
        this.raining = worldInformation.raining;
        this.thunderTime = worldInformation.thunderTime;
        this.thundering = worldInformation.thundering;
        this.hardcore = worldInformation.hardcore;
        this.allowCommands = worldInformation.allowCommands;
        this.initialized = worldInformation.initialized;
        this.theGameRules = worldInformation.theGameRules;
        this.difficulty = worldInformation.difficulty;
        this.difficultyLocked = worldInformation.difficultyLocked;
        this.borderCenterX = worldInformation.borderCenterX;
        this.borderCenterZ = worldInformation.borderCenterZ;
        this.borderSize = worldInformation.borderSize;
        this.borderSizeLerpTime = worldInformation.borderSizeLerpTime;
        this.borderSizeLerpTarget = worldInformation.borderSizeLerpTarget;
        this.borderSafeZone = worldInformation.borderSafeZone;
        this.borderDamagePerBlock = worldInformation.borderDamagePerBlock;
        this.borderWarningTime = worldInformation.borderWarningTime;
        this.borderWarningDistance = worldInformation.borderWarningDistance;
    }

    /**
     * Gets the NBTTagCompound for the worldInfo
     */
    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.updateTagCompound(nbttagcompound, this.playerTag);
        return nbttagcompound;
    }

    /**
     * Creates a new NBTTagCompound for the world, with the given NBTTag as the "Player"
     */
    public NBTTagCompound cloneNBTCompound(NBTTagCompound nbt) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.updateTagCompound(nbttagcompound, nbt);
        return nbttagcompound;
    }

    private void updateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt) {
        nbt.setLong("RandomSeed", this.randomSeed);
        nbt.setString("generatorName", this.terrainType.getWorldTypeName());
        nbt.setInteger("generatorVersion", this.terrainType.getGeneratorVersion());
        nbt.setString("generatorOptions", this.generatorOptions);
        nbt.setInteger("GameType", this.theGameType.getID());
        nbt.setBoolean("MapFeatures", this.mapFeaturesEnabled);
        nbt.setInteger("SpawnX", this.spawnX);
        nbt.setInteger("SpawnY", this.spawnY);
        nbt.setInteger("SpawnZ", this.spawnZ);
        nbt.setLong("Time", this.totalTime);
        nbt.setLong("DayTime", this.worldTime);
        nbt.setLong("SizeOnDisk", this.sizeOnDisk);
        nbt.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
        nbt.setString("LevelName", this.levelName);
        nbt.setInteger("version", this.saveVersion);
        nbt.setInteger("clearWeatherTime", this.cleanWeatherTime);
        nbt.setInteger("rainTime", this.rainTime);
        nbt.setBoolean("raining", this.raining);
        nbt.setInteger("thunderTime", this.thunderTime);
        nbt.setBoolean("thundering", this.thundering);
        nbt.setBoolean("hardcore", this.hardcore);
        nbt.setBoolean("allowCommands", this.allowCommands);
        nbt.setBoolean("initialized", this.initialized);
        nbt.setDouble("BorderCenterX", this.borderCenterX);
        nbt.setDouble("BorderCenterZ", this.borderCenterZ);
        nbt.setDouble("BorderSize", this.borderSize);
        nbt.setLong("BorderSizeLerpTime", this.borderSizeLerpTime);
        nbt.setDouble("BorderSafeZone", this.borderSafeZone);
        nbt.setDouble("BorderDamagePerBlock", this.borderDamagePerBlock);
        nbt.setDouble("BorderSizeLerpTarget", this.borderSizeLerpTarget);
        nbt.setDouble("BorderWarningBlocks", this.borderWarningDistance);
        nbt.setDouble("BorderWarningTime", this.borderWarningTime);

        if (this.difficulty != null) {
            nbt.setByte("Difficulty", (byte) this.difficulty.getDifficultyId());
        }

        nbt.setBoolean("DifficultyLocked", this.difficultyLocked);
        nbt.setTag("GameRules", this.theGameRules.writeToNBT());

        if (playerNbt != null) {
            nbt.setTag("Player", playerNbt);
        }
    }

    /**
     * Returns the seed of current world.
     */
    public long getSeed() {
        return this.randomSeed;
    }

    public long getWorldTotalTime() {
        return this.totalTime;
    }

    /**
     * Returns the player's NBTTagCompound to be loaded
     */
    public NBTTagCompound getPlayerNBTTagCompound() {
        return this.playerTag;
    }

    public void setWorldTotalTime(long time) {
        this.totalTime = time;
    }

    public void setSpawn(BlockPos spawnPoint) {
        this.spawnX = spawnPoint.getX();
        this.spawnY = spawnPoint.getY();
        this.spawnZ = spawnPoint.getZ();
    }

    /**
     * Get current world name
     */
    public String getWorldName() {
        return this.levelName;
    }

    public void setWorldName(String worldName) {
        this.levelName = worldName;
    }

    /**
     * Gets the GameType.
     */
    public WorldSettings.GameType getGameType() {
        return this.theGameType;
    }

    /**
     * Sets the GameType.
     */
    public void setGameType(WorldSettings.GameType type) {
        this.theGameType = type;
    }

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean isHardcoreModeEnabled() {
        return this.hardcore;
    }

    /**
     * Returns true if commands are allowed on this World.
     */
    public boolean areCommandsAllowed() {
        return this.allowCommands;
    }

    /**
     * Sets the initialization status of the World.
     */
    public void setServerInitialized(boolean initializedIn) {
        this.initialized = initializedIn;
    }

    /**
     * Gets the GameRules class Instance.
     */
    public GameRules getGameRulesInstance() {
        return this.theGameRules;
    }

    /**
     * Returns the border lerp time
     */
    public long getBorderLerpTime() {
        return this.borderSizeLerpTime;
    }

    /**
     * Sets the border lerp time
     */
    public void setBorderLerpTime(long time) {
        this.borderSizeLerpTime = time;
    }

    /**
     * Returns the border lerp target
     */
    public double getBorderLerpTarget() {
        return this.borderSizeLerpTarget;
    }

    /**
     * Sets the border lerp target
     */
    public void setBorderLerpTarget(double lerpSize) {
        this.borderSizeLerpTarget = lerpSize;
    }

    /**
     * Sets the border center Z position
     */
    public void getBorderCenterZ(double posZ) {
        this.borderCenterZ = posZ;
    }

    /**
     * Sets the border center X position
     */
    public void getBorderCenterX(double posX) {
        this.borderCenterX = posX;
    }

    /**
     * Adds this WorldInfo instance to the crash report.
     */
    public void addToCrashReport(CrashReportCategory category) {
        category.addCrashSectionCallable("世界种子", () -> String.valueOf(WorldInfo.this.getSeed()));
        category.addCrashSectionCallable("世界生成器", () -> String.format("ID %02d - %s, ver %d. 启用功能: %b", WorldInfo.this.terrainType.getWorldTypeID(), WorldInfo.this.terrainType.getWorldTypeName(), WorldInfo.this.terrainType.getGeneratorVersion(), WorldInfo.this.mapFeaturesEnabled));
        category.addCrashSectionCallable("世界生成器的选项", () -> WorldInfo.this.generatorOptions);
        category.addCrashSectionCallable("世界生成位置", () -> CrashReportCategory.getCoordinateInfo(WorldInfo.this.spawnX, WorldInfo.this.spawnY, WorldInfo.this.spawnZ));
        category.addCrashSectionCallable("世界时间", () -> String.format("游戏内时间: %d, 游戏刻: %d", WorldInfo.this.totalTime, WorldInfo.this.worldTime));
        category.addCrashSectionCallable("世界维度", () -> String.valueOf(WorldInfo.this.dimension));
        category.addCrashSectionCallable("世界存储版本", () -> {
            String s = "未知?";

            try {
                s = switch (WorldInfo.this.saveVersion) {
                    case 19132 -> "McRegion";
                    case 19133 -> "Anvil";
                    default -> s;
                };
            } catch (Throwable var3) {
            }

            return String.format("0x%05X - %s", WorldInfo.this.saveVersion, s);
        });
        category.addCrashSectionCallable("世界天气", () -> String.format("下雨时间: %d (正在下雨: %b), 打雷时间: %d (正在打雷: %b)", WorldInfo.this.rainTime, WorldInfo.this.raining, WorldInfo.this.thunderTime, WorldInfo.this.thundering));
        category.addCrashSectionCallable("世界游戏模式", () -> String.format("游戏模式: %s (ID %d). 极限: %b. 作弊: %b", WorldInfo.this.theGameType.getName(), WorldInfo.this.theGameType.getID(), WorldInfo.this.hardcore, WorldInfo.this.allowCommands));
    }
}
