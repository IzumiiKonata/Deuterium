package tritium.utils.other.info;

import lombok.experimental.UtilityClass;
import net.minecraft.util.LazyLoadBase;
import tritium.Tritium;
import tritium.utils.json.JsonUtils;
import tritium.utils.other.ProcessUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * @author IzumiiKonata
 * Date: 2025/11/8 10:54
 */
@UtilityClass
public class VersionUtils {

    public Optional<BuildInfo> getBuildInfo() {
        InputStream ver = Tritium.class.getResourceAsStream("/version.json");

        if (ver == null)
            return Optional.empty();

        BuildInfo info = JsonUtils.parse(new InputStreamReader(ver), BuildInfo.class);
        return Optional.of(info);
    }

    private final LazyLoadBase<String> currentBranch = LazyLoadBase.of(() -> {
        try {
            return ProcessUtils.runProcess("git", "rev-parse", "--abbrev-ref", "HEAD");
        } catch (IOException e) {
            return "UNKNOWN";
        }
    });

    public String getCurrentBranch() {
        return currentBranch.getValue();
    }

    private final LazyLoadBase<String> currentCommit = LazyLoadBase.of(() -> {
        try {
            return ProcessUtils.runProcess("git", "rev-parse", "HEAD");
        } catch (IOException e) {
            return "UNKNOWN";
        }
    });

    public String getCurrentCommit() {
        return currentCommit.getValue();
    }

    public String getCurrentCommitShort() {
        return getCurrentCommit().substring(0, 7);
    }

    public String getRemoteCommit(String branch) {
        try {
            ProcessUtils.runProcess("git", "fetch", "origin");
            return ProcessUtils.runProcess("git", "rev-parse", "origin/" + branch);
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }

    public String getRemoteCommitShort(String branch) {
        return getRemoteCommit(branch).substring(0, 7);
    }

}
