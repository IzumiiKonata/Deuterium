/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.mojang.util.UUIDTypeAdapter
 */
package tritium.utils.res.skin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mojang.util.UUIDTypeAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UUIDFetcher {
    public static final long FEBRUARY_2015 = 1422748800000L;
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
    private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final Map<String, UUID> uuidCache = new HashMap<String, UUID>();
    private static final Map<UUID, String> nameCache = new HashMap<UUID, String>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    @SerializedName("name")
    public String name;
    @SerializedName("id")
    public UUID id;
    public long changedToAt;

    public static void getUUID(final String name, final Consumer<UUID> action) {
        pool.execute(() -> action.accept(UUIDFetcher.getUUID(name)));
    }

    public static UUID getUUID(String name) {
        return UUIDFetcher.getUUIDAt(name, System.currentTimeMillis());
    }

    public static void getUUIDAt(final String name, final long timestamp, final Consumer<UUID> action) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                action.accept(UUIDFetcher.getUUIDAt(name, timestamp));
            }
        });
    }

    public static UUID getUUIDAt(String name, long timestamp) {
        UUIDFetcher data;
        block4:
        {
            if (uuidCache.containsKey(name = name.toLowerCase())) {
                return uuidCache.get(name);
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000L)).openConnection();
                connection.setReadTimeout(5000);
                data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);
                if (data != null) break block4;
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
        uuidCache.put(name, data.id);
        nameCache.put(data.id, data.name);
        return data.id;
    }

    public static String getCorrectUsername(String name, long timestamp) {
        UUIDFetcher data;
        block4:
        {
            if (uuidCache.containsKey(name = name.toLowerCase()) && nameCache.containsKey(uuidCache.get(name))) {
                return nameCache.get(uuidCache.get(name));
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000L)).openConnection();
                connection.setReadTimeout(5000);
                data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);
                if (data != null) break block4;
                return null;
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
        uuidCache.put(name, data.id);
        nameCache.put(data.id, data.name);
        return data.name;
    }

    public static void getName(final UUID uuid, final Consumer<String> action) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                action.accept(UUIDFetcher.getName(uuid));
            }
        });
    }

    public static void getCorrectUsername(final String name, final Consumer<String> action) {
        pool.execute(new Runnable() {

            @Override
            public void run() {
                action.accept(UUIDFetcher.getCorrectUsername(name, System.currentTimeMillis()));
            }
        });
    }

    public static String getName(UUID uuid) {
        if (nameCache.containsKey(uuid)) {
            return nameCache.get(uuid);
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher[] nameHistory = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher[].class);
            UUIDFetcher currentNameData = nameHistory[nameHistory.length - 1];
            uuidCache.put(currentNameData.name.toLowerCase(), uuid);
            nameCache.put(uuid, currentNameData.name);
            return currentNameData.name;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static NameHistory getHistory(UUID uuid) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            connection.setReadTimeout(5000);
            UUIDFetcher[] nameHistory = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher[].class);
            return new NameHistory(uuid, nameHistory);
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }
}

