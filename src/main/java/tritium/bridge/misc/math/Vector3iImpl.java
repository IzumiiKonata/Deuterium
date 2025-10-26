package tritium.bridge.misc.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import today.opai.api.interfaces.dataset.Vector3i;

/**
 * @author IzumiiKonata
 * Date: 2025/10/21 22:36
 */
@AllArgsConstructor
public class Vector3iImpl implements Vector3i {

    @Getter
    private int x, y, z;

}
