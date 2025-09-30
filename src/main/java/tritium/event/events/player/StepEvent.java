package tritium.event.events.player;


import lombok.Getter;
import tritium.event.eventapi.EventState;

@Getter
public class StepEvent extends EventState {
    private final boolean pre;
    private double stepHeight;
    private double realHeight;

    public StepEvent(boolean state, double stepHeight, double realHeight) {
        this.pre = state;
        this.stepHeight = stepHeight;
        this.realHeight = realHeight;
    }

    public StepEvent(boolean state, double stepHeight) {
        this.pre = state;
        this.stepHeight = stepHeight;
        this.realHeight = this.realHeight;
    }

    public void setStepHeight(double stepHeight) {
        this.stepHeight = stepHeight;
    }

    public void setRealHeight(double realHeight) {
        this.realHeight = realHeight;
    }
}
