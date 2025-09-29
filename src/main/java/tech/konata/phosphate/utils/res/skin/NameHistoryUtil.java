/*
 * Decompiled with CFR 0.151.
 */
package tech.konata.phosphate.utils.res.skin;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NameHistoryUtil {
    private static final HashMap<String, NameHistory> cacheHistory = new HashMap();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public static NameHistory getNameHistory(final String name) {
        if (cacheHistory.containsKey(name)) {
            return cacheHistory.get(name);
        }
        NameHistory nameHistory = new NameHistory(UUID.randomUUID(), new UUIDFetcher[0]);
        cacheHistory.put(name, nameHistory);
        EXECUTOR_SERVICE.execute(new Runnable() {

            @Override
            public void run() {
                NameHistory nameHistory = NameHistoryUtil.requestHistory(name);
                cacheHistory.put(name, nameHistory);
            }
        });
        return nameHistory;
    }

    public static void getNameHistory(final String name, final Consumer<NameHistory> callback) {
        if (cacheHistory.containsKey(name)) {
            callback.accept(cacheHistory.get(name));
            return;
        }
        NameHistory nameHistory = new NameHistory(UUID.randomUUID(), new UUIDFetcher[0]);
        cacheHistory.put(name, nameHistory);
        EXECUTOR_SERVICE.execute(new Runnable() {

            @Override
            public void run() {
                NameHistory history = NameHistoryUtil.requestHistory(name);
                cacheHistory.put(name, history);
                callback.accept(history);
            }
        });
    }

    public static boolean isInCache(String name) {
        return cacheHistory.containsKey(name);
    }

    private static NameHistory requestHistory(String name) {
        UUID uuid = UUIDFetcher.getUUID(name);
        if (uuid == null) {
            return null;
        }
        return UUIDFetcher.getHistory(uuid);
    }
}

