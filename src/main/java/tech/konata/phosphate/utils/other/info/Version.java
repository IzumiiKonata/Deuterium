package tech.konata.phosphate.utils.other.info;

import lombok.Getter;
import lombok.SneakyThrows;
import tech.konata.phosphate.Phosphate;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Calendar;

/**
 * @author IzumiiKonata
 * @since 11/19/2023
 */
@Getter
public class Version {

    private final int major, minor, patch;
    private final String suffix;

    public Version(int major, int minor, int patch, String suffix) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;

        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + suffix;
    }

}