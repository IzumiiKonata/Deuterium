package tritium.utils.other.info;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author IzumiiKonata
 * Date: 2025/11/8 10:25
 */
@Data
public class BuildInfo {

    @SerializedName("version")
    private final String version;
    @SerializedName("commit")
    private final String commit;
    @SerializedName("commit_short")
    private final String commitShort;
    @SerializedName("build_number")
    private final int buildNumber;
    @SerializedName("build_time")
    private final long buildTime;
    @SerializedName("build_time_readable")
    private final String buildTimeReadable;
    @SerializedName("branch")
    private final String branch;

}
