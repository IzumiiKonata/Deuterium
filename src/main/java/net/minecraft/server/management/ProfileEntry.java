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
    public final Date expirationDate;

    ProfileEntry(GameProfile gameProfileIn, Date expirationDateIn) {
        this.gameProfile = gameProfileIn;
        this.expirationDate = expirationDateIn;
    }

}
