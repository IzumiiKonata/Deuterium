package net.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Location;
import net.minecraft.world.IBlockAccess;
import net.optifine.override.PlayerControllerOF;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class CustomGuis {
    private static final Minecraft mc = Config.getMinecraft();
    private static PlayerControllerOF playerControllerOF = null;
    private static CustomGuiProperties[][] guiProperties = null;
    public static boolean isChristmas = isChristmas();

    public static Location getTextureLocation(final Location loc) {
        if (guiProperties == null) {
            return loc;
        } else {
            final GuiScreen guiscreen = mc.currentScreen;
            if (!(guiscreen instanceof GuiContainer) || (!loc.getResourceDomain().equals("minecraft") || !loc.getResourcePath().startsWith("textures/gui/")) || (playerControllerOF == null)) {
                return loc;
            } else {
                final IBlockAccess iblockaccess = mc.theWorld;
                if (iblockaccess == null) {
                    return loc;
                } else if (guiscreen instanceof GuiContainerCreative) {
                    return getTexturePos(CustomGuiProperties.EnumContainer.CREATIVE, mc.thePlayer.getPosition(), iblockaccess, loc, guiscreen);
                } else if (guiscreen instanceof GuiInventory) {
                    return getTexturePos(CustomGuiProperties.EnumContainer.INVENTORY, mc.thePlayer.getPosition(), iblockaccess, loc, guiscreen);
                } else {
                    final BlockPos blockpos = playerControllerOF.getLastClickBlockPos();
                    if (blockpos != null) {
                        if (guiscreen instanceof GuiRepair) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.ANVIL, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiBeacon) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.BEACON, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiBrewingStand) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.BREWING_STAND, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiChest) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.CHEST, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiCrafting) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.CRAFTING, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiDispenser) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.DISPENSER, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiEnchantment) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.ENCHANTMENT, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiFurnace) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.FURNACE, blockpos, iblockaccess, loc, guiscreen);
                        }
                        if (guiscreen instanceof GuiHopper) {
                            return getTexturePos(CustomGuiProperties.EnumContainer.HOPPER, blockpos, iblockaccess, loc, guiscreen);
                        }
                    }
                    final Entity entity = playerControllerOF.getLastClickEntity();
                    if (entity != null) {
                        if (guiscreen instanceof GuiScreenHorseInventory) {
                            return getTextureEntity(CustomGuiProperties.EnumContainer.HORSE, entity, iblockaccess, loc);
                        }
                        if (guiscreen instanceof GuiMerchant) {
                            return getTextureEntity(CustomGuiProperties.EnumContainer.VILLAGER, entity, iblockaccess, loc);
                        }
                    }
                    return loc;
                }
            }
        }
    }

    private static Location getTexturePos(final CustomGuiProperties.EnumContainer container, final BlockPos pos, final IBlockAccess blockAccess, final Location loc, final GuiScreen screen) {
        final CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];
        if (acustomguiproperties == null) {
            return loc;
        } else {
            for (final CustomGuiProperties customguiproperties : acustomguiproperties) {
                if (customguiproperties.matchesPos(container, pos, blockAccess, screen)) {
                    return customguiproperties.getTextureLocation(loc);
                }
            }
            return loc;
        }
    }

    private static Location getTextureEntity(final CustomGuiProperties.EnumContainer container, final Entity entity, final IBlockAccess blockAccess, final Location loc) {
        final CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];
        if (acustomguiproperties == null) {
            return loc;
        } else {
            for (final CustomGuiProperties customguiproperties : acustomguiproperties) {
                if (customguiproperties.matchesEntity(container, entity, blockAccess)) {
                    return customguiproperties.getTextureLocation(loc);
                }
            }
            return loc;
        }
    }

    public static void update() {
        guiProperties = null;
        if (Config.isCustomGuis()) {
            final List<List<CustomGuiProperties>> list = new ArrayList();
            final IResourcePack[] airesourcepack = Config.getResourcePacks();
            for (int i = airesourcepack.length - 1; i >= 0; --i) {
                final IResourcePack iresourcepack = airesourcepack[i];
                update(iresourcepack, list);
            }
            guiProperties = propertyListToArray(list);
        }
    }

    private static CustomGuiProperties[][] propertyListToArray(final List<List<CustomGuiProperties>> listProps) {
        if (listProps.isEmpty()) {
            return null;
        } else {
            final CustomGuiProperties[][] acustomguiproperties = new CustomGuiProperties[CustomGuiProperties.EnumContainer.VALUES.length][];
            for (int i = 0; i < acustomguiproperties.length; ++i) {
                if (listProps.size() > i) {
                    final List<CustomGuiProperties> list = listProps.get(i);
                    if (list != null) {
                        final CustomGuiProperties[] acustomguiproperties1 = list.toArray(new CustomGuiProperties[0]);
                        acustomguiproperties[i] = acustomguiproperties1;
                    }
                }
            }
            return acustomguiproperties;
        }
    }

    private static void update(final IResourcePack rp, final List<List<CustomGuiProperties>> listProps) {
        final String[] astring = ResUtils.collectFiles(rp, "optifine/gui/container/", ".properties", null);
        Arrays.sort(astring);
        for (final String s : astring) {
            Config.dbg("CustomGuis: " + s);
            try {
                final Location resourcelocation = Location.of(s);
                final InputStream inputstream = rp.getInputStream(resourcelocation);
                if (inputstream == null) {
                    Config.warn("CustomGuis file not found: " + s);
                } else {
                    final Properties properties = new PropertiesOrdered();
                    properties.load(inputstream);
                    inputstream.close();
                    final CustomGuiProperties customguiproperties = new CustomGuiProperties(properties, s);
                    if (customguiproperties.isValid(s)) {
                        addToList(customguiproperties, listProps);
                    }
                }
            } catch (final FileNotFoundException var9) {
                Config.warn("CustomGuis file not found: " + s);
            } catch (final Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void addToList(final CustomGuiProperties cgp, final List<List<CustomGuiProperties>> listProps) {
        if (cgp.getContainer() == null) {
            warn("Invalid container: " + cgp.getContainer());
        } else {
            final int i = cgp.getContainer().ordinal();
            while (listProps.size() <= i) {
                listProps.add(null);
            }
            List<CustomGuiProperties> list = listProps.get(i);
            if (list == null) {
                list = new ArrayList();
                listProps.set(i, list);
            }
            list.add(cgp);
        }
    }

    public static PlayerControllerOF getPlayerControllerOF() {
        return playerControllerOF;
    }

    public static void setPlayerControllerOF(final PlayerControllerOF playerControllerOF) {
        CustomGuis.playerControllerOF = playerControllerOF;
    }

    private static boolean isChristmas() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26;
    }

    private static void warn(final String str) {
        Config.warn("[CustomGuis] " + str);
    }
}
