package tech.konata.phosphate.event.events.rendering;

import net.minecraft.util.AxisAlignedBB;
import tech.konata.phosphate.event.eventapi.EventCancellable;

public class BlockOverlayEvent extends EventCancellable {
    AxisAlignedBB axisalignedbb;

    public BlockOverlayEvent(AxisAlignedBB axisalignedbb) {
        this.axisalignedbb = axisalignedbb;
    }

    public AxisAlignedBB getBB() {
        return this.axisalignedbb;
    }

    public void setBB(AxisAlignedBB axisalignedbb) {
        this.axisalignedbb = axisalignedbb;
    }

}
