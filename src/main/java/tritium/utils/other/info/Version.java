package tritium.utils.other.info;

import lombok.Getter;

/**
 * @author IzumiiKonata
 * @since 11/19/2023
 */
@Getter
public class Version {

    private final int major, minor, patch;
    private final Type type;

    public Version(Type type, int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;

        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%s %d.%d.%d", type, major, minor, patch);
    }

    public enum Type {
        Release,
        Beta,
        Dev
    }

}