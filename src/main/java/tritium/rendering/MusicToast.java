package tritium.rendering;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * @author IzumiiKonata
 * Date: 2025/11/16 12:04
 */
@UtilityClass
public class MusicToast {

    private final Map<String, String> locationToName = new HashMap<>();

    static {
        locationToName.put("minecraft:sounds/music/game/calm1.ogg",                 "C418 - Minecraft");
        locationToName.put("minecraft:sounds/music/game/calm2.ogg",                 "C418 - Clark");
        locationToName.put("minecraft:sounds/music/game/calm3.ogg",                 "C418 - Sweden");
        locationToName.put("minecraft:sounds/music/game/creative/creative1.ogg",    "C418 - Biome Fest");
        locationToName.put("minecraft:sounds/music/game/creative/creative2.ogg",    "C418 - Blind Spots");
        locationToName.put("minecraft:sounds/music/game/creative/creative3.ogg",    "C418 - Haunt Muskie");
        locationToName.put("minecraft:sounds/music/game/creative/creative4.ogg",    "C418 - Aria Math");
        locationToName.put("minecraft:sounds/music/game/creative/creative5.ogg",    "C418 - Dreiton");
        locationToName.put("minecraft:sounds/music/game/creative/creative6.ogg",    "C418 - Taswell");
        locationToName.put("minecraft:sounds/music/game/end/boss.ogg",              "C418 - Boss");
        locationToName.put("minecraft:sounds/music/game/end/credits.ogg",           "C418 - Alpha");
        locationToName.put("minecraft:sounds/music/game/end/end.ogg",               "C418 - The End");
        locationToName.put("minecraft:sounds/music/game/hal1.ogg",                  "C418 - Subwoofer Lullaby");
        locationToName.put("minecraft:sounds/music/game/hal2.ogg",                  "C418 - Living Mice");
        locationToName.put("minecraft:sounds/music/game/hal3.ogg",                  "C418 - Haggstrom");
        locationToName.put("minecraft:sounds/music/game/hal4.ogg",                  "C418 - Danny");
        locationToName.put("minecraft:sounds/music/game/nether/nether1.ogg",        "C418 - Concrete Halls");
        locationToName.put("minecraft:sounds/music/game/nether/nether2.ogg",        "C418 - Dead Voxel");
        locationToName.put("minecraft:sounds/music/game/nether/nether3.ogg",        "C418 - Warmth");
        locationToName.put("minecraft:sounds/music/game/nether/nether4.ogg",        "C418 - Ballad of the Cats");
        locationToName.put("minecraft:sounds/music/game/nuance1.ogg",               "C418 - Key");
        locationToName.put("minecraft:sounds/music/game/nuance2.ogg",               "C418 - Oxygène");
        locationToName.put("minecraft:sounds/music/game/piano1.ogg",                "C418 - Dry Hands");
        locationToName.put("minecraft:sounds/music/game/piano2.ogg",                "C418 - Wet Hands");
        locationToName.put("minecraft:sounds/music/game/piano3.ogg",                "C418 - Mice on Venus");
        locationToName.put("minecraft:sounds/music/menu/menu1.ogg",                 "C418 - Mutation");
        locationToName.put("minecraft:sounds/music/menu/menu2.ogg",                 "C418 - Moog City 2");
        locationToName.put("minecraft:sounds/music/menu/menu3.ogg",                 "C418 - Beginning 2");
        locationToName.put("minecraft:sounds/music/menu/menu4.ogg",                 "C418 - Floating Trees");
    }

}
