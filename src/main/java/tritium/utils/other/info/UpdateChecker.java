package tritium.utils.other.info;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.util.EnumChatFormatting;
import tritium.Tritium;
import tritium.screens.ConsoleScreen;
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
        ConsoleScreen.log("[UpdateChecker] Checking for updates...");
        Thread versionCheckThread = new Thread(() -> {
            Version version = Tritium.getVersion();
            if (version.getVersionType() == Version.VersionType.COMMIT_AND_BRANCH) {
                String branch = VersionUtils.getCurrentBranch();
                String currentCommit = VersionUtils.getCurrentCommit();
                String remote = VersionUtils.getRemoteCommit(branch);

                if (currentCommit.equals("UNKNOWN") || remote.equals("UNKNOWN")) {
                    updateCheckResult = UpdateCheckResult.ERROR;
                    ConsoleScreen.log(EnumChatFormatting.RED + "[UpdateChecker] Cannot check for updates because branch or commit is unknown");
                    return;
                }

                if (currentCommit.equals(remote)) {
                    // up to date
                    updateCheckResult = UpdateCheckResult.UP_TO_DATE;
                    ConsoleScreen.log(EnumChatFormatting.GREEN + "[UpdateChecker] Up to date.");
                } else {
                    updateCheckResult = UpdateCheckResult.OUTDATED_NEW_COMMIT;
                    ConsoleScreen.log(EnumChatFormatting.YELLOW + "[UpdateChecker] Outdated, new commit found.");
                }
            } else {

                String get;
                try {
                    get = HttpUtils.getString("https://gh-proxy.com/https://api.github.com/repos/IzumiiKonata/Deuterium/releases/latest", null);
                } catch (IOException e) {
                    Tritium.getLogger().error("检查更新失败!", e);
                    updateCheckResult = UpdateCheckResult.ERROR;
                    ConsoleScreen.log(EnumChatFormatting.RED + "[UpdateChecker] Cannot check for updates because failed to fetch latest release");
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
                    ConsoleScreen.log(EnumChatFormatting.GREEN + "[UpdateChecker] Up to date.");
                } else {
                    updateCheckResult = UpdateCheckResult.OUTDATED_NEW_RELEASE;
                    ConsoleScreen.log(EnumChatFormatting.YELLOW + "[UpdateChecker] Outdated, new release found.");
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
