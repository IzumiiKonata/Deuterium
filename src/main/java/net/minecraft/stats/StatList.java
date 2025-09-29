package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;
import java.util.Map;

public class StatList {
    protected static Map<String, StatBase> oneShotStats = Maps.newHashMap();
    public static List<StatBase> allStats = Lists.newArrayList();
    public static List<StatBase> generalStats = Lists.newArrayList();

    public static void init() {
    }

    public static StatBase getStatKillEntity(EntityList.EntityEggInfo eggInfo) {
        String s = EntityList.getStringFromID(eggInfo.spawnedID);
        return s == null ? null : (new StatBase("stat.killEntity." + s, new ChatComponentTranslation("stat.entityKill", new ChatComponentTranslation("entity." + s + ".name")))).registerStat();
    }

    public static StatBase getStatEntityKilledBy(EntityList.EntityEggInfo eggInfo) {
        String s = EntityList.getStringFromID(eggInfo.spawnedID);
        return s == null ? null : (new StatBase("stat.entityKilledBy." + s, new ChatComponentTranslation("stat.entityKilledBy", new ChatComponentTranslation("entity." + s + ".name")))).registerStat();
    }

    public static StatBase getOneShotStat(String p_151177_0_) {
        return oneShotStats.get(p_151177_0_);
    }
}
