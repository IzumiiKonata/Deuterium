package net.optifine;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Location;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.Matches;
import net.optifine.config.NbtTagValue;
import net.optifine.config.RangeListInt;
import net.optifine.config.VillagerProfession;

import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;

public class CustomGuiProperties {
    private String fileName = null;
    private String basePath = null;
    private EnumContainer container = null;
    private Map<Location, Location> textureLocations = null;
    private NbtTagValue nbtName = null;
    private BiomeGenBase[] biomes = null;
    private RangeListInt heights = null;
    private Boolean large = null;
    private Boolean trapped = null;
    private Boolean christmas = null;
    private Boolean ender = null;
    private RangeListInt levels = null;
    private VillagerProfession[] professions = null;
    private EnumVariant[] variants = null;
    private EnumDyeColor[] colors = null;
    private static final EnumVariant[] VARIANTS_HORSE = new EnumVariant[]{EnumVariant.HORSE, EnumVariant.DONKEY, EnumVariant.MULE,
            EnumVariant.LLAMA};
    private static final EnumVariant[] VARIANTS_DISPENSER = new EnumVariant[]{EnumVariant.DISPENSER, EnumVariant.DROPPER};
    private static final EnumVariant[] VARIANTS_INVALID = new EnumVariant[0];
    private static final EnumDyeColor[] COLORS_INVALID = new EnumDyeColor[0];
    private static final Location ANVIL_GUI_TEXTURE = Location.of("textures/gui/container/anvil.png");
    private static final Location BEACON_GUI_TEXTURE = Location.of("textures/gui/container/beacon.png");
    private static final Location BREWING_STAND_GUI_TEXTURE = Location.of("textures/gui/container/brewing_stand.png");
    private static final Location CHEST_GUI_TEXTURE = Location.of("textures/gui/container/generic_54.png");
    private static final Location CRAFTING_TABLE_GUI_TEXTURE = Location.of("textures/gui/container/crafting_table.png");
    private static final Location HORSE_GUI_TEXTURE = Location.of("textures/gui/container/horse.png");
    private static final Location DISPENSER_GUI_TEXTURE = Location.of("textures/gui/container/dispenser.png");
    private static final Location ENCHANTMENT_TABLE_GUI_TEXTURE = Location.of("textures/gui/container/enchanting_table.png");
    private static final Location FURNACE_GUI_TEXTURE = Location.of("textures/gui/container/furnace.png");
    private static final Location HOPPER_GUI_TEXTURE = Location.of("textures/gui/container/hopper.png");
    private static final Location INVENTORY_GUI_TEXTURE = Location.of("textures/gui/container/inventory.png");
    private static final Location SHULKER_BOX_GUI_TEXTURE = Location.of("textures/gui/container/shulker_box.png");
    private static final Location VILLAGER_GUI_TEXTURE = Location.of("textures/gui/container/villager.png");

    public CustomGuiProperties(final Properties props, final String path) {
        final ConnectedParser connectedparser = new ConnectedParser("CustomGuis");
        this.fileName = connectedparser.parseName(path);
        this.basePath = connectedparser.parseBasePath(path);
        this.container = (EnumContainer) connectedparser.parseEnum(props.getProperty("container"), EnumContainer.values(), "container");
        this.textureLocations = parseTextureLocations(props, "texture", this.container, "textures/gui/", this.basePath);
        this.nbtName = connectedparser.parseNbtTagValue("name", props.getProperty("name"));
        this.biomes = connectedparser.parseBiomes(props.getProperty("biomes"));
        this.heights = connectedparser.parseRangeListInt(props.getProperty("heights"));
        this.large = connectedparser.parseBooleanObject(props.getProperty("large"));
        this.trapped = connectedparser.parseBooleanObject(props.getProperty("trapped"));
        this.christmas = connectedparser.parseBooleanObject(props.getProperty("christmas"));
        this.ender = connectedparser.parseBooleanObject(props.getProperty("ender"));
        this.levels = connectedparser.parseRangeListInt(props.getProperty("levels"));
        this.professions = connectedparser.parseProfessions(props.getProperty("professions"));
        final EnumVariant[] acustomguiproperties$enumvariant = getContainerVariants(this.container);
        this.variants = (EnumVariant[]) connectedparser.parseEnums(props.getProperty("variants"), acustomguiproperties$enumvariant, "variants", VARIANTS_INVALID);
        this.colors = parseEnumDyeColors(props.getProperty("colors"));
    }

    private static EnumVariant[] getContainerVariants(final EnumContainer cont) {
        return cont == EnumContainer.HORSE ? VARIANTS_HORSE : cont == EnumContainer.DISPENSER ? VARIANTS_DISPENSER : new EnumVariant[0];
    }

    private static EnumDyeColor[] parseEnumDyeColors(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.toLowerCase();
            final String[] astring = Config.tokenize(str, " ");
            final EnumDyeColor[] aenumdyecolor = new EnumDyeColor[astring.length];
            for (int i = 0; i < astring.length; ++i) {
                final String s = astring[i];
                final EnumDyeColor enumdyecolor = parseEnumDyeColor(s);
                if (enumdyecolor == null) {
                    warn("Invalid color: " + s);
                    return COLORS_INVALID;
                }
                aenumdyecolor[i] = enumdyecolor;
            }
            return aenumdyecolor;
        }
    }

    private static EnumDyeColor parseEnumDyeColor(final String str) {
        if (str != null) {
            final EnumDyeColor[] aenumdyecolor = EnumDyeColor.values();
            for (final EnumDyeColor enumdyecolor : aenumdyecolor) {
                if (enumdyecolor.getName().equals(str)) {
                    return enumdyecolor;
                }
                if (enumdyecolor.getUnlocalizedName().equals(str)) {
                    return enumdyecolor;
                }
            }
        }
        return null;
    }

    private static Location parseTextureLocation(String str, final String basePath) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            String s = TextureUtils.fixResourcePath(str, basePath);
            if (!s.endsWith(".png")) {
                s = s + ".png";
            }
            return Location.of(basePath + "/" + s);
        }
    }

    private static Map<Location, Location> parseTextureLocations(final Properties props, final String property, final EnumContainer container, final String pathPrefix, final String basePath) {
        final Map<Location, Location> map = new HashMap<>();
        final String s = props.getProperty(property);
        if (s != null) {
            final Location resourcelocation = getGuiTextureLocation(container);
            final Location resourcelocation1 = parseTextureLocation(s, basePath);
            if (resourcelocation != null && resourcelocation1 != null) {
                map.put(resourcelocation, resourcelocation1);
            }
        }
        final String s5 = property + ".";
        for (final String s1 : (Set<String>) (Set<?>) props.keySet()) {
            if (s1.startsWith(s5)) {
                String s2 = s1.substring(s5.length());
                s2 = s2.replace('\\', '/');
                s2 = StrUtils.removePrefixSuffix(s2, "/", ".png");
                final String s3 = pathPrefix + s2 + ".png";
                final String s4 = props.getProperty(s1);
                final Location resourcelocation2 = Location.of(s3);
                final Location resourcelocation3 = parseTextureLocation(s4, basePath);
                map.put(resourcelocation2, resourcelocation3);
            }
        }
        return map;
    }

    private static Location getGuiTextureLocation(final EnumContainer container) {
        if (container == null) {
            return null;
        } else {
            return switch (container) {
                case ANVIL -> ANVIL_GUI_TEXTURE;
                case BEACON -> BEACON_GUI_TEXTURE;
                case BREWING_STAND -> BREWING_STAND_GUI_TEXTURE;
                case CHEST -> CHEST_GUI_TEXTURE;
                case CRAFTING -> CRAFTING_TABLE_GUI_TEXTURE;
                case CREATIVE -> null;
                case DISPENSER -> DISPENSER_GUI_TEXTURE;
                case ENCHANTMENT -> ENCHANTMENT_TABLE_GUI_TEXTURE;
                case FURNACE -> FURNACE_GUI_TEXTURE;
                case HOPPER -> HOPPER_GUI_TEXTURE;
                case HORSE -> HORSE_GUI_TEXTURE;
                case INVENTORY -> INVENTORY_GUI_TEXTURE;
                case SHULKER_BOX -> SHULKER_BOX_GUI_TEXTURE;
                case VILLAGER -> VILLAGER_GUI_TEXTURE;
                default -> null;
            };
        }
    }

    public boolean isValid(final String path) {
        if (this.fileName != null && !this.fileName.isEmpty()) {
            if (this.basePath == null) {
                warn("No base path found: " + path);
                return false;
            } else if (this.container == null) {
                warn("No container found: " + path);
                return false;
            } else if (this.textureLocations.isEmpty()) {
                warn("No texture found: " + path);
                return false;
            } else if (this.professions == ConnectedParser.PROFESSIONS_INVALID) {
                warn("Invalid professions or careers: " + path);
                return false;
            } else if (this.variants == VARIANTS_INVALID) {
                warn("Invalid variants: " + path);
                return false;
            } else if (this.colors == COLORS_INVALID) {
                warn("Invalid colors: " + path);
                return false;
            } else {
                return true;
            }
        } else {
            warn("No name found: " + path);
            return false;
        }
    }

    private static void warn(final String str) {
        Config.warn("[CustomGuis] " + str);
    }

    private boolean matchesGeneral(final EnumContainer ec, final BlockPos pos, final IBlockAccess blockAccess) {
        if (this.container != ec) {
            return false;
        } else {
            if (this.biomes != null) {
                final BiomeGenBase biomegenbase = blockAccess.getBiomeGenForCoords(pos);
                if (!Matches.biome(biomegenbase, this.biomes)) {
                    return false;
                }
            }
            return this.heights == null || this.heights.isInRange(pos.getY());
        }
    }

    public boolean matchesPos(final EnumContainer ec, final BlockPos pos, final IBlockAccess blockAccess, final GuiScreen screen) {
        if (!this.matchesGeneral(ec, pos, blockAccess)) {
            return false;
        } else {
            if (this.nbtName != null) {
                final String s = getName(screen);
                if (!this.nbtName.matchesValue(s)) {
                    return false;
                }
            }
            return switch (ec) {
                case BEACON -> this.matchesBeacon(pos, blockAccess);
                case CHEST -> this.matchesChest(pos, blockAccess);
                case DISPENSER -> this.matchesDispenser(pos, blockAccess);
                default -> true;
            };
        }
    }

    public static String getName(final GuiScreen screen) {
        final IWorldNameable iworldnameable = getWorldNameable(screen);
        return iworldnameable == null ? null : iworldnameable.getDisplayName().getUnformattedText();
    }

    private static IWorldNameable getWorldNameable(GuiScreen screen) {
        if (screen instanceof GuiBeacon) return ((GuiBeacon) screen).tileBeacon;
        if (screen instanceof GuiBrewingStand) return ((GuiBrewingStand) screen).tileBrewingStand;
        if (screen instanceof GuiChest) return ((GuiChest) screen).lowerChestInventory;
        if (screen instanceof GuiDispenser) return ((GuiDispenser) screen).dispenserInventory;
        if (screen instanceof GuiEnchantment) return ((GuiEnchantment) screen).field_175380_I;
        if (screen instanceof GuiFurnace) return ((GuiFurnace) screen).tileFurnace;
        if (screen instanceof GuiHopper) return ((GuiHopper) screen).hopperInventory;
        return null;
    }

    private boolean matchesBeacon(final BlockPos pos, final IBlockAccess blockAccess) {
        final TileEntity tileentity = blockAccess.getTileEntity(pos);
        if (!(tileentity instanceof TileEntityBeacon)) {
            return false;
        } else {
            final TileEntityBeacon tileentitybeacon = (TileEntityBeacon) tileentity;
            if (this.levels != null) {
                final NBTTagCompound nbttagcompound = new NBTTagCompound();
                tileentitybeacon.writeToNBT(nbttagcompound);
                final int i = nbttagcompound.getInteger("Levels");
                return this.levels.isInRange(i);
            }
            return true;
        }
    }

    private boolean matchesChest(final BlockPos pos, final IBlockAccess blockAccess) {
        final TileEntity tileentity = blockAccess.getTileEntity(pos);
        if (tileentity instanceof TileEntityChest) {
            final TileEntityChest tileentitychest = (TileEntityChest) tileentity;
            return this.matchesChest(tileentitychest, pos, blockAccess);
        } else if (tileentity instanceof TileEntityEnderChest) {
            final TileEntityEnderChest tileentityenderchest = (TileEntityEnderChest) tileentity;
            return this.matchesEnderChest(tileentityenderchest, pos, blockAccess);
        } else {
            return false;
        }
    }

    private boolean matchesChest(final TileEntityChest tec, final BlockPos pos, final IBlockAccess blockAccess) {
        final boolean flag = tec.adjacentChestXNeg != null || tec.adjacentChestXPos != null || tec.adjacentChestZNeg != null || tec.adjacentChestZPos != null;
        final boolean flag1 = tec.getChestType() == 1;
        final boolean flag2 = CustomGuis.isChristmas;
        final boolean flag3 = false;
        return this.matchesChest(flag, flag1, flag2, flag3);
    }

    private boolean matchesEnderChest(final TileEntityEnderChest teec, final BlockPos pos, final IBlockAccess blockAccess) {
        return this.matchesChest(false, false, false, true);
    }

    private boolean matchesChest(final boolean isLarge, final boolean isTrapped, final boolean isChristmas, final boolean isEnder) {
        return (this.large == null || this.large == isLarge) && (this.trapped == null || this.trapped == isTrapped) && (this.christmas == null || this.christmas == isChristmas) && (this.ender == null || this.ender == isEnder);
    }

    private boolean matchesDispenser(final BlockPos pos, final IBlockAccess blockAccess) {
        final TileEntity tileentity = blockAccess.getTileEntity(pos);
        if (!(tileentity instanceof TileEntityDispenser)) {
            return false;
        } else {
            final TileEntityDispenser tileentitydispenser = (TileEntityDispenser) tileentity;
            if (this.variants != null) {
                final EnumVariant customguiproperties$enumvariant = this.getDispenserVariant(tileentitydispenser);
                return Config.equalsOne(customguiproperties$enumvariant, this.variants);
            }
            return true;
        }
    }

    private EnumVariant getDispenserVariant(final TileEntityDispenser ted) {
        return ted instanceof TileEntityDropper ? EnumVariant.DROPPER : EnumVariant.DISPENSER;
    }

    public boolean matchesEntity(final EnumContainer ec, final Entity entity, final IBlockAccess blockAccess) {
        if (!this.matchesGeneral(ec, entity.getPosition(), blockAccess)) {
            return false;
        } else {
            if (this.nbtName != null) {
                final String s = entity.getName();
                if (!this.nbtName.matchesValue(s)) {
                    return false;
                }
            }
            return switch (ec) {
                case HORSE -> this.matchesHorse(entity, blockAccess);
                case VILLAGER -> this.matchesVillager(entity, blockAccess);
                default -> true;
            };
        }
    }

    private boolean matchesVillager(final Entity entity, final IBlockAccess blockAccess) {
        if (!(entity instanceof EntityVillager)) {
            return false;
        } else {
            final EntityVillager entityvillager = (EntityVillager) entity;
            if (this.professions != null) {
                final int i = entityvillager.getProfession();
                final int j = entityvillager.careerId;
                if (j < 0) {
                    return false;
                }
                boolean flag = false;
                for (final VillagerProfession villagerprofession : this.professions) {
                    if (villagerprofession.matches(i, j)) {
                        flag = true;
                        break;
                    }
                }
                return flag;
            }
            return true;
        }
    }

    private boolean matchesHorse(final Entity entity, final IBlockAccess blockAccess) {
        if (!(entity instanceof EntityHorse)) {
            return false;
        } else {
            final EntityHorse entityhorse = (EntityHorse) entity;
            if (this.variants != null) {
                final EnumVariant customguiproperties$enumvariant = this.getHorseVariant(entityhorse);
                return Config.equalsOne(customguiproperties$enumvariant, this.variants);
            }
            return true;
        }
    }

    private EnumVariant getHorseVariant(final EntityHorse entity) {
        final int i = entity.getHorseType();
        return switch (i) {
            case 0 -> EnumVariant.HORSE;
            case 1 -> EnumVariant.DONKEY;
            case 2 -> EnumVariant.MULE;
            default -> null;
        };
    }

    public EnumContainer getContainer() {
        return this.container;
    }

    public Location getTextureLocation(final Location loc) {
        final Location resourcelocation = this.textureLocations.get(loc);
        return resourcelocation == null ? loc : resourcelocation;
    }

    @Override
    public String toString() {
        return "name: " + this.fileName + ", container: " + this.container + ", textures: " + this.textureLocations;
    }

    public enum EnumContainer {
        ANVIL, BEACON, BREWING_STAND, CHEST, CRAFTING, DISPENSER, ENCHANTMENT, FURNACE, HOPPER, HORSE, VILLAGER, SHULKER_BOX, CREATIVE, INVENTORY;
        public static final EnumContainer[] VALUES = values();
    }

    private enum EnumVariant {HORSE, DONKEY, MULE, LLAMA, DISPENSER, DROPPER}
}
