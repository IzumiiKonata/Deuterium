package tech.konata.phosphate.utils.audio;

import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjgl.system.MemoryUtil;
import processing.sound.*;
import tech.konata.phosphate.Phosphate;
import tech.konata.phosphate.management.ModuleManager;
import tech.konata.phosphate.management.WidgetsManager;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.settings.GlobalSettings;
import tech.konata.phosphate.widget.impl.MusicSpectrum;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 播放视频里面的音频
 */
public class AudioPlayer {
    public SoundFile player;
    public Runnable afterPlayed;

    @Getter
    public float volume = 0.25f;

    public int getThreshold() {
        return 0;
    }

    public AudioPlayer(String musicPath) {
        finished = false;

        this.player = new SoundFile(musicPath);
        this.setListeners();
        this.setEffects();
    }


    public AudioPlayer(File file) {
        finished = false;

        this.player = new SoundFile(file.getAbsolutePath());
        this.setListeners();
        this.setEffects();
    }

    public void setAudio(File file) {
        this.close();

        this.player = new SoundFile(file.getAbsolutePath());
        this.setListeners();
        this.setEffects();
        finished = false;

    }

    @Getter
    FFT fft = new FFT(128, WidgetsManager.musicSpectrum.callback);

    public final Object lock = new Object();

//    @Getter
//    Amplitude amp = new Amplitude();

    public void setListeners() {
        fft.removeInput();
        fft.input(this.player);

        player.setOnFinished(() -> finished = true);
    }

    @Getter
    Delay delayEffect = new Delay();
    @Getter
    Reverb reverbEffect = new Reverb();

    public void setEffects() {

        delayEffect.setMaxDelayTime(4f);
        delayEffect.set(GlobalSettings.DELAY_TIME.getValue(), GlobalSettings.DELAY_FEEDBACK.getValue());
        reverbEffect.set(GlobalSettings.REVERB_ROOM.getValue(), GlobalSettings.REVERB_DAMP.getValue(), GlobalSettings.REVERB_WET.getValue());

        switch (GlobalSettings.EFFECT_TYPE.getValue()) {
            case None: {
                delayEffect.stop();
                reverbEffect.stop();
                break;
            }

            case Delay: {
                delayEffect.process(this.player);
                reverbEffect.stop();
                break;
            }

            case Reverb: {
                delayEffect.stop();
                reverbEffect.process(this.player);
                break;
            }
        }

    }

    public void play() {
        finished = false;
        this.player.play();

        this.player.amp(volume);
        this.player.rate(GlobalSettings.PLAYBACK_SPEED.getValue().floatValue());
    }

    /**
     * @param progress 毫秒
     */
    @SneakyThrows
    public void setProgress(float progress) {
        this.player.jump(progress / 1000F);
        this.player.amp(volume);
        this.player.rate(GlobalSettings.PLAYBACK_SPEED.getValue().floatValue());
    }

    @SneakyThrows
    public void close() {
        player.stop();
        player.cleanUp();
    }

    @Getter
    private boolean finished = false;

    public void setAfterPlayed(Runnable runnable) {
        this.afterPlayed = runnable;
        this.player.setOnFinished(() -> {
            finished = true;
            runnable.run();
        });
    }

    public int getTotalTimeSeconds() {
        return (int) this.player.duration();
    }

    public int getCurrentTimeSeconds() {
        return (int) (getCurrentTimeMillis() / 1000);
    }

    public int getTotalTimeMillis() {
        return getTotalTimeSeconds() * 1000;
    }

    public float getCurrentTimeMillis() {
        return this.player.position() * 1000;
    }

    public boolean isPausing() {
        return !this.player.isPlaying();
    }

    public void setVolume(float volume) {

        if (!Phosphate.getInstance().isClientLoaded()) {
            volume = 0.5f;
        }

        this.volume = volume;
        this.player.amp(this.getVolume());
    }

    public void pause() {
        this.player.pause();
    }

    public void unpause() {
        this.play();
    }

}