package net.minecraft.client.audio;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ITickable;
import net.minecraft.util.Location;
import net.minecraft.util.MathHelper;
import tritium.management.CommandManager;
import tritium.rendering.MusicToast;

import java.util.Random;

public class MusicTicker implements ITickable {
    private final Random rand = new Random();
    private final Minecraft mc;
    private ISound currentMusic;
    @Getter
    private Location musicLocation = null;
    @Getter
    private int timeUntilNextMusic = 100;

    public MusicTicker(Minecraft mcIn) {
        this.mc = mcIn;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update() {
        MusicTicker.MusicType type = this.mc.getAmbientMusicType();

        if (this.currentMusic != null) {
            if (!type.getMusicLocation().equals(this.currentMusic.getSoundLocation())) {
                this.mc.getSoundHandler().stopSound(this.currentMusic);
                this.timeUntilNextMusic = MathHelper.getRandomIntegerInRange(this.rand, 0, type.getMinDelay() / 2);
            }

            if (!this.mc.getSoundHandler().isSoundPlaying(this.currentMusic)) {
                this.currentMusic = null;
                this.musicLocation = null;
                this.timeUntilNextMusic = Math.min(MathHelper.getRandomIntegerInRange(this.rand, type.getMinDelay(), type.getMaxDelay()), this.timeUntilNextMusic);
            }
        }

        if (this.currentMusic == null && this.timeUntilNextMusic-- <= 0) {
            this.playNext(type);
        }
    }

    public void forcePlayNext() {
        MusicTicker.MusicType type = this.mc.getAmbientMusicType();

        this.stopCurrentPlaying();
        this.currentMusic = null;
        this.musicLocation = null;
        this.timeUntilNextMusic = 0;

        this.playNext(type);
    }

    public void playNext(MusicTicker.MusicType type) {
        this.currentMusic = PositionedSoundRecord.create(type.getMusicLocation());
        Location location = this.mc.getSoundHandler().playSound(this.currentMusic);

        if (mc.gameSettings.getSoundLevel(SoundCategory.MUSIC) > 0) {
            MusicToast.pushMusicToast(location);
        }

        musicLocation = location;
//        System.out.println("Playing: " + location);
        this.timeUntilNextMusic = Integer.MAX_VALUE;
    }

    public void stopCurrentPlaying() {
        if (this.currentMusic != null) {
            this.mc.getSoundHandler().stopSound(this.currentMusic);
            this.currentMusic = null;
            this.musicLocation = null;
            this.timeUntilNextMusic = 0;
        }
    }

    public enum MusicType {
        MENU(Location.of("minecraft:music.menu"), 20, 600),
        GAME(Location.of("minecraft:music.game"), 12000, 24000),
        CREATIVE(Location.of("minecraft:music.game.creative"), 1200, 3600),
        CREDITS(Location.of("minecraft:music.game.end.credits"), Integer.MAX_VALUE, Integer.MAX_VALUE),
        NETHER(Location.of("minecraft:music.game.nether"), 1200, 3600),
        END_BOSS(Location.of("minecraft:music.game.end.dragon"), 0, 0),
        END(Location.of("minecraft:music.game.end"), 6000, 24000);

        private final Location musicLocation;
        private final int minDelay;
        private final int maxDelay;

        MusicType(Location location, int minDelayIn, int maxDelayIn) {
            this.musicLocation = location;
            this.minDelay = minDelayIn;
            this.maxDelay = maxDelayIn;
        }

        public Location getMusicLocation() {
            return this.musicLocation;
        }

        public int getMinDelay() {
            return this.minDelay;
        }

        public int getMaxDelay() {
            return this.maxDelay;
        }
    }

    static {
        CommandManager.registerCommand("musicticker_play_next", () -> Minecraft.getMinecraft().getMusicTicker().forcePlayNext());
    }
}
