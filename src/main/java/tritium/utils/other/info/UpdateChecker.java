package tritium.utils.other.info;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import tritium.Tritium;
import tritium.utils.i18n.Localizable;
import tritium.utils.json.JsonUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.info.github.LatestReleaseDAO;

import java.io.IOException;

/**
 * @author IzumiiKonata
 * Date: 2025/11/8 11:53
 */
@UtilityClass
public class UpdateChecker {

    @Getter
    private UpdateCheckResult updateCheckResult = UpdateCheckResult.CHECKING;

    public void check() {
        Thread versionCheckThread = new Thread(() -> {
            Version version = Tritium.getVersion();
            if (version.getVersionType() == Version.VersionType.COMMIT_AND_BRANCH) {
                String branch = VersionUtils.getCurrentBranch();
                String currentCommit = VersionUtils.getCurrentCommit();
                String remote = VersionUtils.getRemoteCommit(branch);

                if (currentCommit.equals("UNKNOWN") || remote.equals("UNKNOWN")) {
                    updateCheckResult = UpdateCheckResult.ERROR;
                    return;
                }

                if (currentCommit.equals(remote)) {
                    // up to date
                    updateCheckResult = UpdateCheckResult.UP_TO_DATE;
                } else {
                    updateCheckResult = UpdateCheckResult.OUTDATED_NEW_COMMIT;
                }
            } else {

                String get;
                try {
                    get = HttpUtils.getString("https://api.github.com/repos/IzumiiKonata/Deuterium/releases/latest", null);
                } catch (IOException e) {
                    Tritium.getLogger().error("检查更新失败!", e);
                    updateCheckResult = UpdateCheckResult.ERROR;
                    return;
                }

                LatestReleaseDAO latestRelease = JsonUtils.parse(get, LatestReleaseDAO.class);
                String name = latestRelease.getName();

                String[] splitVer = name.split("\\.");
                int major = Integer.parseInt(splitVer[0]);
                int minor = Integer.parseInt(splitVer[1]);
                int patch = Integer.parseInt(splitVer[2]);

                if (version.getMajor() == major && version.getMinor() == minor && version.getPatch() == patch) {
                    updateCheckResult = UpdateCheckResult.UP_TO_DATE;
                } else {
                    updateCheckResult = UpdateCheckResult.OUTDATED_NEW_RELEASE;
                }
            }
        }, "Version Check Thread");
        versionCheckThread.setDaemon(true);
        versionCheckThread.start();
    }

    public enum UpdateCheckResult {
        CHECKING("version.checking"),
        UP_TO_DATE("version.up_to_date"),
        OUTDATED_NEW_RELEASE("version.outdated_new_release"),
        OUTDATED_NEW_COMMIT("version.outdated_new_commit"),
        ERROR("version.error");

        @Getter
        final Localizable localizable;

        UpdateCheckResult(String localizeKey) {
            this.localizable = Localizable.of(localizeKey);
        }
    }

}
