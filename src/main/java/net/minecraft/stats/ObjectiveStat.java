package net.minecraft.stats;

import net.minecraft.scoreboard.ScoreDummyCriteria;

public class ObjectiveStat extends ScoreDummyCriteria {

    public ObjectiveStat(StatBase statIn) {
        super(statIn.statId);
    }
}
