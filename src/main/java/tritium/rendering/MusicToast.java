package tritium.rendering;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.Location;
import tritium.interfaces.IFontRenderer;
import tritium.interfaces.SharedRenderingConstants;
import tritium.management.FontManager;
import tritium.rendering.animation.Interpolations;
import tritium.rendering.rendersystem.RenderSystem;
import tritium.utils.math.Mth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IzumiiKonata
 * Date: 2025/11/16 12:04
 */
@UtilityClass
public class MusicToast implements SharedRenderingConstants {

    @Getter
    private final Map<String, String> locationToName = new HashMap<>();

    final LazyLoadBase<AnimatedTexture> musicNotes = LazyLoadBase.of(() -> {
        try {
            return new AnimatedTexture(Location.of("tritium/textures/hud/music_notes.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    public void pushMusicToast(String name) {
        text = name;
        waitStart = -1L;
        forward = true;

        double musicNotesSize = 16;
        double spacing = 4;
        double contentWidth = musicNotesSize + spacing + FontManager.vanilla.getStringWidth(text);

        offset = -Math.max(120, contentWidth + 12) * 1.25;
    }

    private int musicNoteColorTick;
    private long lastMusicNoteColorChange;
    private int musicNoteColor;
    private String text = null;
    private boolean forward = true;
    private double offset = -240;
    private long waitStart = -1L;

    public void tickMusicNotes() {
        long now;
        if ((now = System.currentTimeMillis()) > lastMusicNoteColorChange + 25L) {
            lastMusicNoteColorChange = now;
            musicNoteColor = getLerpedColor(++musicNoteColorTick);
        }
    }


    public void render() {
        if (text != null) {
            IFontRenderer vanilla = FontManager.vanilla;

            double musicNotesSize = 16;
            double spacing = 4;
            double contentWidth = musicNotesSize + spacing + vanilla.getStringWidth(text);

            double toastWidth = Math.max(120, contentWidth + 12), toastHeight = 24;

            offset = Interpolations.interpBezier(offset, forward ? 0 : -toastWidth * 1.25, .1f);

            double offsetX = offset + 1;
            double offsetY = 1;

            if (forward && offset >= -.5) {

                if (waitStart == -1L) {
                    waitStart = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - waitStart > 5000L) {
                        forward = false;
                    }
                }

            }

            if (!forward && offset <= -toastWidth * 1.2) {
                text = null;
                return;
            }

            GlStateManager.enableTexture2D();

            if (toastWidth > 120) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(Location.of("tritium/textures/hud/now_playing.png"));

                RenderSystem.color(-1);

                GlStateManager.disableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                Gui.drawModalRectWithCustomSizedTexture(offsetX, offsetY, 0, 0, 4, toastHeight, 120, 24);
                Gui.drawModalRectWithCustomSizedTexture(offsetX + toastWidth - 4, offsetY, 116, 0, 4, toastHeight, 120, 24);

                int count = (int) ((toastWidth - 8) / 112);
                for (int i = 0; i < count; i ++) {
                    Gui.drawModalRectWithCustomSizedTexture(offsetX + 4 + i * 112, offsetY, 4, 0, 112, toastHeight, 120, 24);
                }

                Gui.drawModalRectWithCustomSizedTexture(offsetX + 4 + count * 112, offsetY, 4, 0, toastWidth - (8 + count * 112), toastHeight, 120, 24);

            } else {
                Image.draw(Location.of("tritium/textures/hud/now_playing.png"), offsetX, offsetY, toastWidth, toastHeight, Image.Type.Normal);
            }


            AnimatedTexture value = musicNotes.getValue();

            if (value != null) {
                tickMusicNotes();
                RenderSystem.color(musicNoteColor);
                value.render(offsetX + toastWidth * .5 - contentWidth * .5, offsetY + toastHeight * .5 - musicNotesSize * .5, musicNotesSize, musicNotesSize, true);
//                    Rect.draw(offset + toastWidth * .5 - contentWidth * .5, toastHeight * .5 - musicNotesSize * .5, musicNotesSize, musicNotesSize, -1);
//                    value.render(offset + toastWidth * .5 - musicNotesSize * .5, toastHeight * .5 - musicNotesSize * .5, musicNotesSize, musicNotesSize, true);
            }

            vanilla.drawString(text, offsetX + toastWidth * .5 - contentWidth * .5 + musicNotesSize + spacing, offsetY + toastHeight * .5 - vanilla.getHeight() * .5, -1);
        }
    }

    public void pushMusicToast(Location playingSound) {
        if (playingSound == null)
            return;

        String string = playingSound.toString();
//        System.out.println( string);
        pushMusicToast(locationToName.getOrDefault(string, "Unknown"));
    }


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

    private final DyeColor[] MUSIC_NOTE_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.CYAN, DyeColor.GREEN, DyeColor.LIME, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.PINK, DyeColor.RED, DyeColor.MAGENTA};

    public int getLerpedColor(float tick) {
        int colorDuration = 30;
        int tickCount = Mth.floor(tick);
        int value = tickCount / colorDuration;
        int colorCount = MUSIC_NOTE_COLORS.length;
        int c1 = value % colorCount;
        int c2 = (value + 1) % colorCount;
        float subStep = ((float)(tickCount % colorDuration) + Mth.frac(tick)) / (float)colorDuration;
        int color1 = getModifiedColor(MUSIC_NOTE_COLORS[c1], 1.25f);
        int color2 = getModifiedColor(MUSIC_NOTE_COLORS[c2], 1.25f);
        return RGBA.srgbLerp(subStep, color1, color2);
    }

    private int getModifiedColor(DyeColor color, float brightness) {
        if (color == DyeColor.WHITE) {
            return -1644826;
        }
        int src = color.getTextureDiffuseColor();
        return RGBA.color(Mth.floor((float) RGBA.red(src) * brightness), Mth.floor((float) RGBA.green(src) * brightness), Mth.floor((float) RGBA.blue(src) * brightness), 255);
    }

    public enum DyeColor
    {
        WHITE(0, "white", 0xF9FFFE, 0xF0F0F0, 0xFFFFFF),
        ORANGE(1, "orange", 16351261, 15435844, 16738335),
        MAGENTA(2, "magenta", 13061821, 12801229, 0xFF00FF),
        LIGHT_BLUE(3, "light_blue", 3847130, 6719955, 10141901),
        YELLOW(4, "yellow", 16701501, 14602026, 0xFFFF00),
        LIME(5, "lime", 8439583, 4312372, 0xBFFF00),
        PINK(6, "pink", 15961002, 14188952, 16738740),
        GRAY(7, "gray", 4673362, 0x434343, 0x808080),
        LIGHT_GRAY(8, "light_gray", 0x9D9D97, 0xABABAB, 0xD3D3D3),
        CYAN(9, "cyan", 1481884, 2651799, 65535),
        PURPLE(10, "purple", 8991416, 8073150, 10494192),
        BLUE(11, "blue", 3949738,  2437522, 255),
        BROWN(12, "brown", 8606770, 5320730, 9127187),
        GREEN(13, "green", 6192150, 3887386, 65280),
        RED(14, "red", 11546150, 11743532, 0xFF0000),
        BLACK(15, "black", 0x1D1D21, 0x1E1B1B, 0);

        private final int id;
        private final String name;
        private final int textureDiffuseColor;
        private final int fireworkColor;
        private final int textColor;

        DyeColor(int id, String name, int textureDiffuseColor, int fireworkColor, int textColor) {
            this.id = id;
            this.name = name;
            this.textColor = RGBA.opaque(textColor);
            this.textureDiffuseColor = RGBA.opaque(textureDiffuseColor);
            this.fireworkColor = fireworkColor;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public int getTextureDiffuseColor() {
            return this.textureDiffuseColor;
        }

        public int getFireworkColor() {
            return this.fireworkColor;
        }

        public int getTextColor() {
            return this.textColor;
        }


        public String toString() {
            return this.name;
        }

        public String getSerializedName() {
            return this.name;
        }

    }

}
