package tech.konata.phosphate.rendering.background.nativelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import lombok.var;
import tech.konata.obfuscation.ExcludeThis;

@ExcludeThis
public interface NtDllLib extends Library {

    NtDllLib INSTANCE = Native.load("ntdll", NtDllLib.class);

    int MINIMUM_MAJOR = 10; // Windows 11 also has a major version of 10
    int MINIMUM_BUILD = 22621;

    /**
     * Check whether current OS is supported.
     *
     * @return True if compatible, otherwise false
     */
    static boolean checkCompatibility() {
        var major = new IntByReference();
        var minor = new IntByReference();
        var build = new IntByReference();
        getBuildNumber(major, minor, build);

        System.out.println("System Version: " + major.getValue() + "." + minor.getValue() + ", Build " + (build.getValue() & 0x0FFFFFFF));

        return major.getValue() >= MINIMUM_MAJOR && (build.getValue() & 0x0FFFFFFF) >= MINIMUM_BUILD;
    }

    void RtlGetNtVersionNumbers(
            IntByReference MajorVersion,
            IntByReference MinorVersion,
            IntByReference BuildNumber
    );

    static void getBuildNumber(final IntByReference majorVersion, final IntByReference minorVersion, final IntByReference buildNumber) {
        INSTANCE.RtlGetNtVersionNumbers(majorVersion, minorVersion, buildNumber);
    }
}