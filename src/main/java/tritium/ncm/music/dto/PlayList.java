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
    public final long id;

    @SerializedName("name")
    public final String name;

    @SerializedName(value = "coverImgUrl", alternate = { "picUrl" })
    public final String coverUrl;

    @SerializedName("trackCount")
    public final int count;

    @SerializedName(value = "playCount", alternate = { "playcount" })
    public final long playCount;

    @SerializedName("creator")
    public final User creator;

    @SerializedName("description")
    public final String description;

    @SerializedName("subscribed")
    public final boolean subscribed;

    @SerializedName("createTime")
    public final long createTime;

    // unique fields
    public transient List<Music> musics;
    public transient boolean searchMode = false;
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

        if (!musics.isEmpty()) {
            callback.onMusicsLoaded(musics);
            return;
        }

        if (!searchMode) {

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
