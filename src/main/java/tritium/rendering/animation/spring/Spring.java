package tritium.rendering.animation.spring;

import lombok.Getter;
import java.util.function.DoubleUnaryOperator;

/**
 * @author IzumiiKonata
 * Date: 2025/12/11 23:28
 */
public class Spring {

    @Getter
    private double currentPosition;
    private double targetPosition;
    private double currentTime = 0.0;

    private SpringParams params = new SpringParams();

    private QueuedParams queueParams = null;
    private QueuedPosition queuePosition = null;

    private DoubleUnaryOperator currentSolver;
    private DoubleUnaryOperator getV;
    private DoubleUnaryOperator getV2;

    public Spring(double current) {
        targetPosition = current;
        currentPosition = current;
        currentSolver = t -> targetPosition;
        getV = t -> 0.0;
        getV2 = t -> 0.0;
    }

    public Spring() {
        this(0.0);
    }

    private static DoubleUnaryOperator makeVelocityFunc(DoubleUnaryOperator f) {
        return t -> {
            final double h = 1e-4;
            return (f.applyAsDouble(t + h) - f.applyAsDouble(t - h)) / (2 * h);
        };
    }

    private static DoubleUnaryOperator solveSpring(double from, double velocity, double to, SpringParams p) {
        double delta = to - from;

        double mass = p.mass;
        double damping = p.damping;
        double stiffness = p.stiffness;
        boolean soft = p.soft;

        if (soft || damping >= 2.0 * Math.sqrt(stiffness * mass)) {
            double angular_freq = -Math.sqrt(stiffness / mass);
            double leftover = -angular_freq * delta - velocity;

            return t -> to - (delta + t * leftover) * Math.exp(t * angular_freq);
        }

        double damp_freq = Math.sqrt(4.0 * mass * stiffness - damping * damping);
        double leftover = (damping * delta - 2.0 * mass * velocity) / damp_freq;
        double dfm = (0.5 * damp_freq) / mass;
        double dm = -(0.5 * damping) / mass;

        return t -> to - (Math.cos(t * dfm) * delta + Math.sin(t * dfm) * leftover) * Math.exp(t * dm);
    }

    public void update(double delta) {
        currentTime += delta;
        currentPosition = currentSolver.applyAsDouble(currentTime);

        if (queueParams != null) {
            queueParams.time -= delta;
            if (queueParams.time <= 0) {
                updateParams(queueParams.params);
                queueParams = null;
            }
        }

        if (queuePosition != null) {
            queuePosition.time -= delta;
            if (queuePosition.time <= 0) {
                setTargetPosition(queuePosition.position);
                queuePosition = null;
            }
        }

        if (arrived()) {
            setPosition(targetPosition);
        }
    }

    public void setPosition(double position) {
        targetPosition = position;
        currentPosition = position;
        currentSolver = t -> targetPosition;
        getV = t -> 0.0;
        getV2 = t -> 0.0;
    }

    public void setTargetPosition(double position, double delay) {
        if (delay > 0) {
            queuePosition = new QueuedPosition(position, delay);
        } else {
            queuePosition = null;
            targetPosition = position;
            resetSolver();
        }
    }

    public void setTargetPosition(double position) {
        setTargetPosition(position, 0.0);
    }

    public void updateParams(SpringParams newParams, double delay) {
        if (delay > 0) {
            queueParams = new QueuedParams(newParams, delay);
        } else {
            params = newParams;
            resetSolver();
        }
    }

    public void updateParams(SpringParams newParams) {
        updateParams(newParams, 0.0);
    }

    public boolean arrived() {
        return Math.abs(targetPosition - currentPosition) < 0.01 && Math.abs(getV.applyAsDouble(currentTime)) < 0.01 && Math.abs(getV2.applyAsDouble(currentTime)) < 0.01 && queueParams == null && queuePosition == null;
    }

    private void resetSolver() {
        double v = getV.applyAsDouble(currentTime);
        currentTime = 0.0;

        currentSolver = solveSpring(currentPosition, v, targetPosition, params);

        getV = makeVelocityFunc(currentSolver);
        getV2 = makeVelocityFunc(getV);
    }
}