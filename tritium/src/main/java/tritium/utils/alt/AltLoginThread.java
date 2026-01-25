/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  com.mojang.authlib.Agent
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
 */
package tritium.utils.alt;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import tritium.utils.oauth.OAuth;

public class AltLoginThread
        extends Thread {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Alt alt;
    @Setter
    @Getter
    private String status;

    @Getter
    private boolean finished = false;

    public AltLoginThread(Alt alt) {
        super("Alt Login Thread");
        this.alt = alt;
        this.status = "Waiting...";
    }

    @Override
    public void run() {
        this.status = "Logging in...";
        if (alt.isMicrosoft() && !alt.isExpired()) {
            var session = new Session(alt.getUsername(), alt.getUserUUID(), alt.getAccessToken(), "mojang");
            mc.setSession(session);
        } else {
            OAuth oAuth = new OAuth();
            oAuth.refresh(
                    alt.getRefreshToken(),
                    new OAuth.LoginCallback() {
                        @Override
                        public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                            Alt at = new Alt(userName, refreshToken, token, uuid);
                            at.setLastRefreshedTime(System.currentTimeMillis() / 1000L);
                            synchronized (AltManager.getAlts()) {
                                AltManager.getAlts().set(AltManager.getAlts().indexOf(alt), at);

                            }
                            var session = new Session(userName, uuid, token, "mojang");
                            mc.setSession(session);
                            finished = true;
                            AltLoginThread.this.status = "Logged in. (" + userName + ")";
                        }

                        @Override
                        public void onFailed(Exception e) {

                        }

                        @Override
                        public void setStatus(String status) {
                            AltLoginThread.this.status = "Refreshing (" + status + ")...";
                        }
                    }
            );
        }

        this.status = "Logged in. (" + alt.getUsername() + ")";
    }
}

