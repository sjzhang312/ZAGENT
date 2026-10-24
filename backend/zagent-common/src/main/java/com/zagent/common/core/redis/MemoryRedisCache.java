package com.zagent.common.core.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.stereotype.Component;

/**
 * 内存缓存实现（zagent.cache.type=memory，默认）
 * 与 RedisCache 同构，使用 ConcurrentHashMap + 定时过期清扫，进程内有效、重启即失。
 * 后续需要分布式/持久缓存时，将配置切回 redis 即可启用原 RedisCache。
 *
 * @author zagent
 **/
@Component
@ConditionalOnProperty(name = "zagent.cache.type", havingValue = "memory", matchIfMissing = true)
public class MemoryRedisCache extends RedisCache
{
    private static final long serialVersionUID = 1L;

    /** 缓存条目：value + 过期时间戳（0 表示永不过期） */
    private static final class Entry
    {
        final Object value;
        volatile long expireAt;

        Entry(Object value, long expireAt)
        {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean expired()
        {
            return expireAt > 0 && System.currentTimeMillis() >= expireAt;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "zagent-memory-cache-cleaner");
        t.setDaemon(true);
        return t;
    });

    public MemoryRedisCache()
    {
        super();
        this.redisTemplate = null;
        cleaner.scheduleWithFixedDelay(this::sweep, 30, 30, TimeUnit.SECONDS);
    }

    private void sweep()
    {
        try
        {
            store.values().removeIf(Entry::expired);
        }
        catch (Exception ignored)
        {
        }
    }

    private Entry getEntry(String key)
    {
        Entry e = store.get(key);
        return (e == null || e.expired()) ? null : e;
    }

    private long toMillis(long timeout, TimeUnit unit)
    {
        return System.currentTimeMillis() + unit.toMillis(timeout);
    }

    private static Pattern globToPattern(String pattern)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : pattern.toCharArray())
        {
            switch (c)
            {
                case '*': sb.append(".*"); break;
                case '?': sb.append('.'); break;
                default:
                    if ("\\[](){}+^$.|".indexOf(c) >= 0)
                    {
                        sb.append('\\');
                    }
                    sb.append(c);
            }
        }
        return Pattern.compile(sb.toString());
    }

    // -------------------- 对象 --------------------

    @Override
    public <T> void setCacheObject(final String key, final T value)
    {
        store.put(key, new Entry(value, 0L));
    }

    @Override
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        if (timeout == null || timeout <= 0)
        {
            setCacheObject(key, value);
        }
        else
        {
            store.put(key, new Entry(value, toMillis(timeout, timeUnit)));
        }
    }

    @Override
    public <T> T getCacheObject(final String key)
    {
        Entry e = getEntry(key);
        return e == null ? null : (T) e.value;
    }

    // -------------------- 有效期 --------------------

    @Override
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    @Override
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        Entry e = getEntry(key);
        if (e == null)
        {
            return false;
        }
        e.expireAt = timeout <= 0 ? 0L : toMillis(timeout, unit);
        return true;
    }

    @Override
    public long getExpire(final String key)
    {
        Entry e = getEntry(key);
        if (e == null)
        {
            return -2;
        }
        if (e.expireAt <= 0)
        {
            return -1;
        }
        return Math.max(0, TimeUnit.MILLISECONDS.toSeconds(e.expireAt - System.currentTimeMillis()));
    }

    @Override
    public Boolean hasKey(String key)
    {
        return getEntry(key) != null;
    }

    // -------------------- 删除 --------------------

    @Override
    public boolean deleteObject(final String key)
    {
        return store.remove(key) != null;
    }

    @Override
    public boolean deleteObject(final Collection collection)
    {
        boolean removed = false;
        for (Object key : collection)
        {
            removed |= store.remove(String.valueOf(key)) != null;
        }
        return removed;
    }

    // -------------------- List --------------------

    @Override
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        List<T> copy = dataList == null ? new ArrayList<>() : new ArrayList<>(dataList);
        store.put(key, new Entry(copy, 0L));
        return copy.size();
    }

    @Override
    public <T> List<T> getCacheList(final String key)
    {
        Entry e = getEntry(key);
        return e == null ? new ArrayList<>() : new ArrayList<>((List<T>) e.value);
    }

    // -------------------- Set --------------------

    @Override
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        Set<T> copy = dataSet == null ? new LinkedHashSet<>() : new LinkedHashSet<>(dataSet);
        store.put(key, new Entry(copy, 0L));
        return null;
    }

    @Override
    public <T> Set<T> getCacheSet(final String key)
    {
        Entry e = getEntry(key);
        return e == null ? new HashSet<>() : new HashSet<>((Set<T>) e.value);
    }

    // -------------------- Map --------------------

    @Override
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null)
        {
            Map<String, T> exists = getRawMap(key, true);
            exists.putAll(dataMap);
        }
    }

    @Override
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return new HashMap<>(getRawMap(key, false));
    }

    @Override
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        getRawMap(key, true).put(hKey, value);
    }

    @Override
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        return (T) getRawMap(key, false).get(hKey);
    }

    @Override
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        Map<String, T> map = getRawMap(key, false);
        List<T> result = new ArrayList<>();
        for (Object hKey : hKeys)
        {
            result.add(map.get(String.valueOf(hKey)));
        }
        return result;
    }

    @Override
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        return getRawMap(key, false).remove(hKey) != null;
    }

    @SuppressWarnings("rawtypes")
    private <T> Map<String, T> getRawMap(final String key, boolean createIfAbsent)
    {
        Entry e = getEntry(key);
        if (e != null && e.value instanceof Map)
        {
            return (Map<String, T>) e.value;
        }
        if (!createIfAbsent)
        {
            return new HashMap<>();
        }
        Map<String, T> created = new ConcurrentHashMap<>();
        store.put(key, new Entry(created, 0L));
        return created;
    }

    // -------------------- keys --------------------

    @Override
    public Collection<String> keys(final String pattern)
    {
        if (pattern == null)
        {
            return new ArrayList<>();
        }
        if (pattern.indexOf('*') < 0 && pattern.indexOf('?') < 0)
        {
            List<String> exact = new ArrayList<>();
            if (getEntry(pattern) != null)
            {
                exact.add(pattern);
            }
            return exact;
        }
        Pattern p = globToPattern(pattern);
        List<String> matched = new ArrayList<>();
        for (String key : store.keySet())
        {
            Entry e = store.get(key);
            if (e != null && !e.expired() && p.matcher(key).matches())
            {
                matched.add(key);
            }
        }
        return matched;
    }
}
