package net.minecraft.server.management;

import com.mojang.authlib.GameProfile;
import lombok.Getter;

import java.util.Date;

/**
 * @author IzumiiKonata
 * Date: 2025/1/17 13:35
 */
@Getter
public class ProfileEntry {
    public final GameProfile gameProfile;
    public final Long expirationStamp;

    ProfileEntry(GameProfile gameProfileIn, Long expirationDateIn) {
        this.gameProfile = gameProfileIn;
        this.expirationStamp = expirationDateIn;
    }

}
