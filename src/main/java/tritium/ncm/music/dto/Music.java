package tritium.ncm.music.dto;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import net.minecraft.util.Tuple;
import tritium.ncm.api.CloudMusicApi;
import tritium.ncm.music.CloudMusic;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Music {

    static final int STEREO = 8192;
    static final int INSTRUMENTAL = 131072;
    static final int DOLBY_ATMOS = 262144;
    static final int DIRTY = 1048576;
    static final long HIRES = 17179869184L;

    @SerializedName("name")
    private final String name;

    @SerializedName("mainTitle")
    private final String mainTitle;

    @SerializedName("additionalTitle")
    private final String additionalTitle;

    @SerializedName("id")
    private final long id;

    @SerializedName("ar")
    private final List<Artist> artists;

    @SerializedName("alia")
    private final List<String> aliasName;

    @SerializedName("al")
    private final Album album;

    @SerializedName("dt")
    private final long duration;

    @SerializedName("mark")
    private final long featureFlag;

    @SerializedName("publishTime")
    private final long publishTime;

    @SerializedName("tns")
    private final List<String> translatedName;

    private transient String artistsName;

    public void init() {
        this.artistsName = this.buildArtistsNames();

        if (this.artistsName.isEmpty()) {
            this.artistsName = "Unknown";
        }
    }

    private String buildArtistsNames() {
        List<String> artistsList = this.artists.stream().map(Artist::getName).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < artistsList.size(); i++) {
            String artistName = artistsList.get(i);

            if (i != artistsList.size() - 1) {
                sb.append(artistName);

                if (i != artistsList.size() - 2) {
                    sb.append("& ");
                }

                sb.append(", ");
            } else {
                sb.append(artistName);
            }
        }

        return sb.toString();
    }

    public String getCoverUrl(int size) {
        return this.album.getPicUrl() + "?param=" + size + "y" + size;
    }

    /**
     * 更新歌曲播放次数
     * 这个方法目前会触发网易云风控, 不要使用
     */
    @Deprecated
    public void updPlayCount(PlayList pl, float sec) {
//        MultiThreadingUtil.runAsync(() -> {
//            Map<String, Object> data = new HashMap<>();
//            data.put("id", this.id);
//            data.put("sourceid", pl.id);
//            data.put("time", sec);
//
//            JsonObject result = CloudMusic.api.GET("/scrobble", data).toJson();
//        });
    }

    /**
     * 获得歌曲 url
     *
     * @return 歌曲文件 url
     */
    public Tuple<String, String> getPlayUrl() {
        JsonObject result = CloudMusicApi.songUrlV1(this.id, CloudMusic.quality.getQuality().toLowerCase()).toJsonObject();
        JsonObject music = result.get("data").getAsJsonArray().get(0).getAsJsonObject();
        if (music.get("code").getAsInt() != 200) {
            throw new RuntimeException(this.name);
        }

        String url = music.get("url").getAsString();

        String type = music.get("type").getAsString();

        if (type.isEmpty())
            type = "mp3";

        return new Tuple<>(url, type);
    }

    public void setLike(boolean like) {
        CloudMusicApi.like(this.id, like);
    }

    public boolean isInstrumental() {
        return (this.featureFlag & INSTRUMENTAL) != 0;
    }

    public boolean isDolbyAtmos() {
        return (this.featureFlag & DOLBY_ATMOS) != 0;
    }

    public boolean isDirty() {
        return (this.featureFlag & DIRTY) != 0;
    }

    public boolean isHiRes() {
        return (this.featureFlag & HIRES) != 0;
    }

}
