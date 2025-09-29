package tech.konata.phosphate.utils.music.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import tech.konata.ncm.RequestUtil;
import tech.konata.ncm.api.CloudMusicApi;
import tech.konata.phosphate.utils.music.CloudMusic;
import tech.konata.phosphate.utils.music.IMusicList;
import tech.konata.phosphate.rendering.entities.impl.ScrollText;
import tech.konata.phosphate.utils.other.multithreading.MultiThreadingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<Music> musics;

    public double scrollSmooth = 0;
    public double scrollOffset = 0;
    public boolean searchMode = false;
    public JsonArray songs;

    public boolean coverLoadedSmall = false;
    public boolean coverLoaded = false;

    public final ScrollText st = new ScrollText();

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

        MultiThreadingUtil.runAsync(() -> {

            RequestUtil.RequestAnswer requestAnswer = CloudMusicApi.playlistTrackAll(id, 8);
//            System.out.println(requestAnswer.toString());
            this.songs = requestAnswer.toJsonObject ().getAsJsonArray("songs");

        });

    }

    //Search
    public PlayList() {
        searchMode = true;
        this.id = 0;
        this.name = "Search";
    }

    public final List<Music> emptyList = Lists.newArrayList();

    @Override
    public List<Music> getMusics() {
        if (this.musics != null && (this.songs != null || searchMode)) {
            return this.musics;
        }

        if (this.songs != null) {
            this.musics = new ArrayList<>();
            if (!this.songs.isEmpty()) {
                this.songs.forEach(element -> {
                    this.musics.add(new Music(element.getAsJsonObject(), null));
                });
            }

            return this.musics;

        } else {
            return emptyList;
        }

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
