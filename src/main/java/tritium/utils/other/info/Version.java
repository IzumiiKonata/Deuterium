package tritium.utils.other.info;

import lombok.Getter;

/**
 * @author IzumiiKonata
 * @since 11/19/2023
 */
@Getter
public class Version {

    private final int major, minor, patch;
    private final String commit, branch;
    private final ReleaseType releaseType;
    private final VersionType versionType;

    public Version(ReleaseType releaseType, int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.commit = this.branch = "";

        this.releaseType = releaseType;
        this.versionType = VersionType.SEMANTIC;
    }

    public Version(String commit, String branch) {
        this.major = this.minor = this.patch = 0;
        this.commit = commit;
        this.branch = branch;

        this.releaseType = ReleaseType.Dev;
        this.versionType = VersionType.COMMIT_AND_BRANCH;
    }

    @Override
    public String toString() {
        if (this.getVersionType() == VersionType.SEMANTIC)
            return String.format("%s %d.%d.%d", releaseType, major, minor, patch);

        return String.format("(%s/%s)", branch, commit);
    }

    public enum ReleaseType {
        Release,
        Beta,
        Dev
    }

    public enum VersionType {
        SEMANTIC,
        COMMIT_AND_BRANCH
    }

}