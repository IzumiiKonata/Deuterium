package tritium.rendering.shader;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class Shader {
    private boolean active;

    public abstract void run(List<Runnable> runnable);

    public abstract void runNoCaching(List<Runnable> runnable);

    public abstract void update();
}
