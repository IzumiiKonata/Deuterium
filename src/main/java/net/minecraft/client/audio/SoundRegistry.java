package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import net.minecraft.util.Location;
import net.minecraft.util.RegistrySimple;

import java.util.Map;

public class SoundRegistry extends RegistrySimple<Location, SoundEventAccessorComposite> {
    private Map<Location, SoundEventAccessorComposite> soundRegistry;

    protected Map<Location, SoundEventAccessorComposite> createUnderlyingMap() {
        this.soundRegistry = Maps.newHashMap();
        return this.soundRegistry;
    }

    public void registerSound(SoundEventAccessorComposite p_148762_1_) {
        this.putObject(p_148762_1_.getSoundEventLocation(), p_148762_1_);
    }

    /**
     * Reset the underlying sound map (Called on resource manager reload)
     */
    public void clearMap() {
        this.soundRegistry.clear();
    }
}
