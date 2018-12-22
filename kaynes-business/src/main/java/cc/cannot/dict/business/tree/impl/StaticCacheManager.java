package cc.cannot.dict.business.tree.impl;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StaticCacheManager {

    private StaticCacheManager() {

    }

    private static final Map<String, CacheData> CACHE = new ConcurrentHashMap<>();

    //启动定时任务清理过期缓存，避免内存溢出
    static {
        Timer t = new Timer();
        // 两分钟清理一次
        t.schedule(new CacheData.ClearTimerTask(CACHE), 0, 2 * 60 * 1000L);
    }

    public static <T> void set(String key, T t, long expire, TimeUnit unit) {
        CACHE.put(key, new CacheData<>(t, expire, unit));
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        CacheData<T> data = CACHE.get(key);
        if (data == null) {
            return null;
        }
        if (data.isExpire()) {
            remove(key);
            return null;
        }
        return data.getData();
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }

    public static void removeAll(String... keys) {
        for (String key : keys) {
            CACHE.remove(key);
        }
    }

    public static void removeAll() {
        CACHE.clear();
    }

    private static class CacheData<T> {
        // 缓存数据
        private T data;
        // 过期时间(单位，毫秒)
        private long expireTime;

        public CacheData(T t, long expire, TimeUnit unit) {
            this.data = t;
            if (expire <= 0) {
                this.expireTime = 0L;
            } else {
                this.expireTime = System.currentTimeMillis() + unit.toMillis(expire);
            }
        }

        /**
         * 判断缓存数据是否过期
         */
        public boolean isExpire() {
            return expireTime > 0 && expireTime > System.currentTimeMillis();
        }

        public T getData() {
            return data;
        }

        /**
         * 清理过期数据定时任务
         */
        private static class ClearTimerTask extends TimerTask {
            Map<String, CacheData> cache;

            ClearTimerTask(Map<String, CacheData> cache) {
                this.cache = cache;
            }

            @Override
            public void run() {
                Set<String> keys = cache.keySet();
                long now = System.currentTimeMillis();
                for (String key : keys) {
                    CacheData data = cache.get(key);
                    if (data.expireTime <= 0 || data.expireTime > now) {
                        continue;
                    }
                    cache.remove(key);
                }
            }
        }
    }
}