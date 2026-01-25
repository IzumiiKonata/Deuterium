package tritium.bridge.management;

import lombok.Getter;
import today.opai.api.enums.EnumGame;
import today.opai.api.interfaces.dataset.PartyMember;
import today.opai.api.interfaces.managers.GameStateManager;

import java.util.Collections;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/10/25 16:52
 */
public class GameStateManagerImpl implements GameStateManager {

    @Getter
    private static final GameStateManagerImpl instance = new GameStateManagerImpl();

    @Override
    public EnumGame getGame() {
        return EnumGame.UNKNOWN;
    }

    @Override
    public String getMap() {
        return "N/A";
    }

    @Override
    public String getGameMode() {
        return "N/A";
    }

    @Override
    public String getGameName() {
        return "N/A";
    }

    @Override
    public boolean isLobby() {
        return false;
    }

    @Override
    public boolean isHypixel() {
        return false;
    }

    @Override
    public boolean isParty() {
        return false;
    }

    @Override
    public List<PartyMember> getPartyMembers() {
        return Collections.emptyList();
    }

    @Override
    public void requeue() {
        // No-op
    }
}
