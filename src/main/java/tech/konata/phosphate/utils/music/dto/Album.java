package tech.konata.phosphate.utils.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import tech.konata.phosphate.utils.music.IMusicList;

import java.util.ArrayList;
import java.util.List;

/**
 * 专辑对象
 */
public class Album implements IMusicList {
    public final long id;
    public final String name;
    public final String cover;
    public final JsonArray alias;
    public final JsonArray artists;
    public final int size;
    public final String[] description;
    private final JsonArray songs;
    private List<Music> musics;

    public Album(JsonObject album) {
        this.songs = album.get("songs").getAsJsonArray();
        album = album.get("album").getAsJsonObject();

        this.id = album.get("id").getAsLong();
        this.name = album.get("name").getAsString();
        this.cover = album.get("picUrl").getAsString();
        this.size = album.get("size").getAsInt();
        this.artists = album.get("artists").getAsJsonArray();
        this.alias = album.get("alias").getAsJsonArray();
        if (!album.get("description").isJsonNull()) {
            this.description = album.get("description").getAsString().split("\n");
        } else {
            this.description = null;
        }
    }

    @Override
    public List<Music> getMusics() {
        if (this.musics != null) {
            return this.musics;
        }

        this.musics = new ArrayList<>();
        this.songs.forEach(element -> {
            this.musics.add(new Music(element.getAsJsonObject(), this.cover));
        });
        return this.musics;
    }

}
