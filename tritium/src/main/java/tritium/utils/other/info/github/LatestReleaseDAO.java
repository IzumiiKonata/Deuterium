package tritium.utils.other.info.github;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author IzumiiKonata
 * Date: 2025/11/8 11:46
 */
@Data
public class LatestReleaseDAO {

    @SerializedName("url")
    private final String url;
    @SerializedName("html_url")
    private final String htmlUrl;
    @SerializedName("assets_url")
    private final String assetsUrl;
    @SerializedName("upload_url")
    private final String uploadUrl;
    @SerializedName("tarball_url")
    private final String tarballUrl;
    @SerializedName("zipball_url")
    private final String zipballUrl;
    @SerializedName("discussion_url")
    private final String discussionUrl;
    @SerializedName("id")
    private final int id;
    @SerializedName("node_id")
    private final String nodeId;
    @SerializedName("tag_name")
    private final String tagName;
    @SerializedName("target_commitish")
    private final String targetCommitish;
    @SerializedName("name")
    private final String name;
    @SerializedName("body")
    private final String body;
    @SerializedName("draft")
    private final boolean draft;
    @SerializedName("prerelease")
    private final boolean prerelease;
    @SerializedName("created_at")
    private final String createdAt;
    @SerializedName("published_at")
    private final String publishedAt;

}
