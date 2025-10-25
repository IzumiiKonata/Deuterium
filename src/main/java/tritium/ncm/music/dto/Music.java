package tritium.ncm.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.util.Tuple;
import tritium.ncm.api.CloudMusicApi;
import tritium.ncm.music.CloudMusic;

import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public class Music {

    static final int STEREO = 8192;
    static final int INSTRUMENTAL = 131072;
    static final int DOLBY_ATMOS = 262144;
    static final int DIRTY = 1048576;
    static final long HIRES = 17179869184L;

    private final long id;
    private final String name;
    private final String translatedName;
    private final String aliasName;
    private String artistsName;
    public final JsonArray artists;
    public final JsonObject album;
    private final String albumName;
    public final long duration;
    public final String picUrl;
    private final long publishTime;
    private final String formattedPublishTime;
    private final long featureFlag;

    public boolean hasDynamicCover = false;
    public String dynamicCoverURL;

    public float dynamicCoverHoverAlpha = 0.0f;

    public boolean prevHover = false;

    public float coverflowHoverAlpha = 0.0f;
    public float listHoverAlpha = 0.0f;
    public boolean textureLoaded = false;
    public boolean textureLoadedSmall = false;
    public float coverLoadAlpha = .0f;

    private Boolean liked = null;

    public boolean isLiked() {
        if (liked == null) {
            liked = CloudMusic.likeList.contains(id);
        }
        return liked;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof Music))
            return false;

        return ((Music) obj).id == this.id;
    }

    public Music(long id) {
        this.id = id;
        this.name = this.translatedName = this.aliasName = this.artistsName = this.albumName = this.picUrl = this.formattedPublishTime = "";
        this.artists = null;
        this.album = null;
        this.duration = this.featureFlag = this.publishTime = 0;
    }

    /**
     * 专辑歌曲没有 picUrl, 通过 cover 传入封面 picUrl
     */
    public Music(JsonObject data, String cover) {

//        System.out.println(data);

        this.id = data.get("id").getAsLong();

        if (data.has("tns")) {
            JsonArray tns = data.getAsJsonArray("tns");

            if (tns.size() > 0) {
                this.name = data.get("name").getAsString();
                this.translatedName = tns.get(0).getAsString();
            } else {
                this.name = data.get("name").getAsString();
                this.translatedName = null;
            }
        } else {
            this.name = data.get("name").getAsString();
            this.translatedName = null;
        }


        JsonArray alias;
        if (data.has("alia")) {
            alias = data.get("alia").getAsJsonArray();
        } else {
            alias = data.get("alias").getAsJsonArray();
        }

        if (alias.size() > 0) {
            this.aliasName = alias.get(0).getAsString();
        } else {
            this.aliasName = "";
        }

        if (data.has("ar")) {
            this.artists = data.get("ar").getAsJsonArray();
        } else {
            this.artists = data.get("artists").getAsJsonArray();
        }

        this.artistsName = this.getArtists();

        if (this.artistsName == null || this.artistsName.isEmpty()) {
            this.artistsName = "Unknown";
        }

        if (data.has("al")) {
            this.album = data.get("al").getAsJsonObject();
        } else {
            this.album = data.get("album").getAsJsonObject();
        }

        if (data.has("dt")) {
            this.duration = data.get("dt").getAsLong() / 1000;
        } else {
            this.duration = data.get("duration").getAsLong() / 1000;
        }

        if (data.has("mark")) {
            this.featureFlag = data.get("mark").getAsLong();
        } else {
            this.featureFlag = 0;
        }

        if (data.has("publishTime")) {
            this.publishTime = data.get("publishTime").getAsLong();
            this.formattedPublishTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date(publishTime));
        } else {
            this.publishTime = 0;
            this.formattedPublishTime = "";
        }

//        System.out.println(this.album);
        this.albumName = this.album.get("name").isJsonNull() ? "None" : this.album.get("name").getAsString();

        if (this.album.has("picUrl")) {
            this.picUrl = this.album.get("picUrl").getAsString();
        } else {
            this.picUrl = cover;
        }

    }

    private String getArtists() {
        List<String> artistsList = new ArrayList<>();

        for (JsonElement artistData : artists) {

            JsonObject jObj = artistData.getAsJsonObject();

            if (jObj.has("name")) {
                JsonElement jName = jObj.get("name");

                if (!jName.isJsonNull()) {
                    artistsList.add(jName.getAsString());
                }
            }
        }

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

    /**
     * 将歌曲扔进垃圾桶 (优化推荐)
     */
//    public void addTrashCan(){
//        this.api.GET_API("/api/radio/trash/add?alg=RT&songId=" + this.id + "&time=25", null);
//    }

    /**
     * 获取歌词
     *
     * @return 滚动歌词对象
     */
    public LyricDTO lyric() {
        return new LyricDTO(CloudMusicApi.lyricNew(this.id).toJsonObject());
    }

    public String getPicUrl() {
        return this.getPicUrl(420);
    }

    public String getPicUrl(int size) {
        return this.picUrl + "?param=" + size + "y" + size;
    }

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
        this.liked = like;
    }

    public long getDuration() {
        return this.duration * 1000;
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
