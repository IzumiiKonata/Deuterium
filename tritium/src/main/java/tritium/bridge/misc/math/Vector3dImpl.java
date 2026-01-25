package tritium.bridge.misc.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import today.opai.api.interfaces.dataset.Vector3d;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:33
 */
@AllArgsConstructor
public class Vector3dImpl implements Vector3d {

    @Getter
    private double x, y, z;

}
