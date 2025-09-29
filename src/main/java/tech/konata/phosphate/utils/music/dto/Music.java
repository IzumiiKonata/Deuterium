package tech.konata.phosphate.utils.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.util.Tuple;
import tech.konata.ncm.api.CloudMusicApi;
import tech.konata.phosphate.settings.GlobalSettings;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class Music {
    private final long id;
    private final String name;
    private final String translatedName;
    private final String aliasName;
    private final String artistsName;
    public final JsonArray artists;
    public final JsonObject album;
    private final String albumName;
    public final long duration;
    public final String picUrl;
    private final long publishTime;
    private final String formattedPublishTime;

    public boolean hasDynamicCover = false;
    public String dynamicCoverURL;

    public float dynamicCoverHoverAlpha = 0.0f;

    public boolean prevHover = false;

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
        this.duration = this.publishTime = 0;
    }

    /**
     * 专辑歌曲没有 picUrl, 通过 cover 传入封面 picUrl
     */
    public Music(JsonObject data, String cover) {

//        System.out.println(data);

        this.id = data.get("id").getAsLong();

        if (data.has("tns")) {
            JsonArray tns = data.getAsJsonArray("tns");

            if (!tns.isEmpty()) {
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

        if (!alias.isEmpty()) {
            this.aliasName = alias.get(0).getAsString();
        } else {
            this.aliasName = "";
        }

        if (data.has("ar")) {
            this.artists = data.get("ar").getAsJsonArray();
        } else {
            this.artists = data.get("artists").getAsJsonArray();
        }

        this.artistsName = this.getArtists(4);

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

    /**
     * 获取歌曲
     *
     * @param id 歌曲 id
     * @return 歌曲对象
     */
    public static Music getMusicById(long id) {
        JsonArray json = CloudMusicApi.songDetail(id).toJsonObject().getAsJsonArray("songs");
        if (json.isEmpty()) {
            throw new RuntimeException("NOT FOUND");
        }
        return new Music(json.get(0).getAsJsonObject(), null);
    }

    private String getArtists(int limit) {
        StringBuilder artistsName = new StringBuilder();

        int count = 0;

        for (JsonElement artistData : artists) {

            JsonObject jObj = artistData.getAsJsonObject();

            if (count + 1 > limit)
                return artistsName.substring(0, artistsName.length() - 3) + " ...";

            if (jObj.has("name")) {

                JsonElement jName = jObj.get("name");

                if (!jName.isJsonNull()) {
                    artistsName.append(jName.getAsString());

                    if (jObj.has("tns")) {
                        JsonArray tns = jObj.getAsJsonArray("tns");

                        if (!tns.isEmpty()) {
                            artistsName.append(" (").append(tns.get(0).getAsString()).append(")");
                        }
                    }

                    artistsName.append(" / ");
                }

            }



            ++count;
        }

        if (artistsName.length() == 0)
            return "";

        return artistsName.substring(0, artistsName.length() - 3);
    }


    /**
     * 将歌曲扔进垃圾桶 (优化推荐)
     */
//    public void addTrashCan(){
//        this.api.POST_API("/api/radio/trash/add?alg=RT&songId=" + this.id + "&time=25", null);
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

    public void updPlayCount(PlayList pl, int sec) {
//        MultiThreadingUtil.runAsync(() -> {
//            Map<String, Object> data = new HashMap<>();
//            data.put("id", this.id);
//            data.put("sourceid", pl.id);
//            data.put("time", sec);
//
//            JsonObject result = CloudMusic.api.POST("/scrobble", data).toJson();
//        });
    }

    /**
     * 获得歌曲 url
     *
     * @return 歌曲文件 url
     */
    public Tuple<String, String> getPlayUrl() {
        JsonObject result = CloudMusicApi.songUrlV1(this.id, GlobalSettings.MUSIC_QUALITY.getValue().getQuality().toLowerCase()).toJsonObject();
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

    public long getDuration() {
        return this.duration * 1000;
    }

}
