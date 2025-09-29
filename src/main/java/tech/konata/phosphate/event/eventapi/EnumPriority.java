package tech.konata.phosphate.event.eventapi;


import lombok.Getter;

@Getter
public enum EnumPriority {
    HIGHEST(7),
    NORMAL(5),
    LOWEST(3);

    private final int level;

    EnumPriority(int level) {
        this.level = level;
    }
}
