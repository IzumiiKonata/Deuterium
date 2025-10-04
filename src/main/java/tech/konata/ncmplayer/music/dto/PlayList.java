package tech.konata.ncmplayer.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import tech.konata.commons.ncm.RequestUtil;
import tech.konata.commons.ncm.api.CloudMusicApi;
import tech.konata.ncmplayer.music.IMusicList;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 歌单对象
 */
public class PlayList implements IMusicList {
    public final long id;
    public String name;
    @Getter
    private final RenderValues renderValues = new RenderValues();
    public String coverUrl;
    public int count;
    public long playCount;
    public User creator;
    public String[] description;
    public boolean subscribed;
    public long createTime;
    //    public JsonArray tags;
    public List<Music> musics = new CopyOnWriteArrayList<>();

    public double scrollSmooth = 0;
    public double scrollOffset = 0;
    public boolean searchMode = false;
    public JsonArray songs;

    public boolean coverLoadedSmall = false;
    public boolean coverLoaded = false;
    public float coverLoadAlpha = .0f;
    public float musicLoadAlpha = 1.0f;

    public PlayList(JsonObject data) {

        JsonObject playlist;
        if (data.has("playlist")) {
            playlist = data.get("playlist").getAsJsonObject();
        } else {
            playlist = data;
        }

        if (playlist.has("subscribed") && !playlist.get("subscribed").isJsonNull()) {
            this.subscribed = playlist.get("subscribed").getAsBoolean();
        }
        this.id = playlist.get("id").getAsLong();
        this.name = playlist.get("name").getAsString();
        this.coverUrl = playlist.get("coverImgUrl").getAsString();
        this.count = playlist.get("trackCount").getAsInt();
        this.playCount = playlist.get("playCount").getAsLong();
        this.creator = new User(playlist.get("creator").getAsJsonObject());
        this.createTime = playlist.get("createTime").getAsLong();
        if (!playlist.get("description").isJsonNull()) {
            this.description = playlist.get("description").getAsString().split("\n");
        } else {
            this.description = null;
        }
//        this.tags = playlist.get("tags").getAsJsonArray();

    }

    //Search
    public PlayList() {
        searchMode = true;
        this.id = 0;
        this.name = "Search";
    }

    @Override
    public List<Music> getMusics() {
        if (this.musics != null && !musics.isEmpty() && (this.songs != null || searchMode)) {
            return this.musics;
        }

        if (this.songs == null && !searchMode) {

            this.songs = new JsonArray();

            MultiThreadingUtil.runAsync(() -> {
                RequestUtil.RequestAnswer requestAnswer = null;
                try {
                    requestAnswer = CloudMusicApi.playlistTrackAll(id, 8);
                } catch (Exception e) {
                    this.songs = null;
                    throw new RuntimeException(e);
                }
                this.songs = requestAnswer.toJsonObject().getAsJsonArray("songs");
                this.songs.forEach(element -> {
                    this.musics.add(new Music(element.getAsJsonObject(), null));
                });
            });

        }

        return this.musics;
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

    public static class RenderValues {

        public float hoveredAlpha = 0;

    }

}
