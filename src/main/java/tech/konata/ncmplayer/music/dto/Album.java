package tech.konata.ncmplayer.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Album {
    public long id;
    public String name;
    public String cover;
    public JsonArray artists;
    public String artistNames = null;

    public final List<Music> musics = new CopyOnWriteArrayList<>();

    public float rotateDeg = 0.0f;
    public double scale = 1.0;

    public boolean flipped = false;

    public double scrollOffset = 0.0, scrollSmooth = 0.0;

    public Album(JsonObject album, JsonArray artists) {

//        Map<String, Object> postData = new HashMap<>();
//        postData.put("id", id);
//
//        JsonObject json = CloudMusic.api.GET("/album", postData).toJson();
//
//        JsonObject album = json.get("album").getAsJsonObject();

        this.id = album.get("id").getAsLong();
        this.name = album.get("name").getAsString();
        this.cover = album.get("picUrl").getAsString();
        this.artists = artists;

        this.artistNames = this.getArtists(6);
    }

    public Album(String name, String cover, String artists) {
        this.id = -1;
        this.name = name;
        this.cover = cover;

        this.artists = null;
        this.artistNames = artists;
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

                        if (tns.size() > 0) {
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

    public String getCoverLocation() {

        if (this.id == -1) {
            return "/textures/AlbumCover" + this.name + ".png";
        }

        return "/textures/AlbumCover" + this.id + ".png";
    }

    public boolean coverLoaded = false;

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof Album))
            return false;

        return ((Album) obj).id == this.id;
    }
}
