package tritium.bridge.management;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import today.opai.api.dataset.RotationData;
import today.opai.api.interfaces.managers.RotationManager;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:38
 */
public class RotationManagerImpl implements RotationManager {

    @Getter
    private static final RotationManagerImpl instance = new RotationManagerImpl();

    @Override
    public void applyRotation(RotationData rotationData, int speed, boolean correction) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RotationData getCurrentRotation() {
        return Minecraft.getMinecraft().thePlayer.getWrapper().getRotation();
    }
}
