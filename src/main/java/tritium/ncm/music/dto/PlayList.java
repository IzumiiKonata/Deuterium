package tritium.ncm.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import tritium.ncm.RequestUtil;
import tritium.ncm.api.CloudMusicApi;
import tritium.utils.json.JsonUtils;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 歌单对象
 */
@Data
public class PlayList {

    @SerializedName("id")
    private final long id;

    @SerializedName("name")
    private final String name;

    @SerializedName(value = "coverImgUrl", alternate = { "picUrl" })
    private final String coverUrl;

    @SerializedName("trackCount")
    private final int count;

    @SerializedName(value = "playCount", alternate = { "playcount" })
    private final long playCount;

    @SerializedName("creator")
    private final User creator;

    @SerializedName("description")
    private final String description;

    @SerializedName("subscribed")
    private final boolean subscribed;

    @SerializedName("createTime")
    private final long createTime;

    // unique fields
    public transient List<Music> musics;
    private transient boolean searchMode = false;
    public transient boolean musicsQueried = false, musicsLoaded = false;

    public List<Music> getMusics() {

        if (this.musics == null)
            this.musics = new CopyOnWriteArrayList<>();

        if (!musics.isEmpty() && (this.musicsQueried || searchMode)) {
            return this.musics;
        }

        if (!this.musicsQueried && !searchMode) {
            this.musicsQueried = true;

            MultiThreadingUtil.runAsync(this::queryMusics);
        }

        return this.musics;
    }

    public void loadMusicsWithCallback(MusicsLoadedCallback callback) {

        if (this.musics == null)
            this.musics = new CopyOnWriteArrayList<>();

        if (!musics.isEmpty() && (this.musicsQueried || searchMode)) {
            callback.onMusicsLoaded(musics);
            return;
        }

        if (!this.musicsQueried && !searchMode) {
            this.musicsQueried = true;

            MultiThreadingUtil.runAsync(() -> {
                queryMusics();
                callback.onMusicsLoaded(musics);
            });
        }
    }

    private void queryMusics() {
        RequestUtil.RequestAnswer requestAnswer;
        try {
            requestAnswer = CloudMusicApi.playlistTrackAll(id, 8);
        } catch (Exception e) {
            this.musicsQueried = false;
            e.printStackTrace();
            return;
        }

        JsonArray songs = requestAnswer.toJsonObject().getAsJsonArray("songs");
        songs.forEach(element -> {
            Music music = JsonUtils.parse(element.getAsJsonObject(), Music.class);
            this.musics.add(music);
            music.init();
        });

        musicsLoaded = true;
    }

    public interface MusicsLoadedCallback {
        void onMusicsLoaded(List<Music> musics);
    }

    public void updPlayCount() {
        CloudMusicApi.playlistUpdatePlaycount(this.id);
    }

    public void addToList(long musicId) {
        CloudMusicApi.playlistTracks("add", this.id, String.valueOf(musicId));
    }

    public void removeFromList(long musicId) {
        CloudMusicApi.playlistTracks("del", this.id, String.valueOf(musicId));
    }

}
