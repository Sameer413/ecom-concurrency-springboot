package org.example.flashsale.common.infrastructure.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(RedisTemplate<String, Object> redisTemplate,  ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // ============================================
    // BASIC OPERATIONS
    // ============================================

    /*
     *  Get a value from redis
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /*
     *  Sets value in redis with Optional expiration in seconds
     */
    public void set(String key, Object value, Long ttlSeconds) {
        if(ttlSeconds != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(
                    key,
                    value,
                    Duration.ofSeconds(ttlSeconds)
            );
        }else {
            redisTemplate.opsForValue().set(
                    key,
                    value
            );
        }
    }

    /**
     * Deletes one or more values from Redis.
     *
     * @param keys Redis keys to delete
     * @return number of keys successfully deleted
     */
    public long delete(String... keys) {

        if(keys.length == 0) return 0L;

        return redisTemplate.delete(
                Arrays.asList(keys)
        );
    }

    // ============================================
    // DISTRIBUTED LOCK
    // ============================================

    /**
     * Acquires a lock for a specific key.
     *
     * NX = only set if the key does not already exist.
     * EX = automatically expire the lock after the TTL.
     *
     * @return true if the lock was acquired, false otherwise
     */
    public boolean acquireLock(String lockKey, String lockValue, long ttlSeconds) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(
                    lockKey,
                    lockValue,
                    Duration.ofSeconds(ttlSeconds)
                );

        return Boolean.TRUE.equals(result);
    }

    /**
     * Safely releases a lock only if the value matches the
     * value that was used when acquiring the lock.
     *
     * @return true if the lock was released, false otherwise
     */
    public boolean releaseLock(String key, String value) {
        String script = """
                    if redis.call("get", KEYS[1]) == ARGV[1] then
                        return redis.call("del", KEYS[1])
                    else
                        return 0
                    end
                """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);

        return Long.valueOf(1L).equals(result);
    }


    // ============================================
    // CACHING UTILITIES (Performance Optimization)
    // ============================================

    public <T> T getCache(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);

        if(value == null) {
            return null;
        }

        return objectMapper.convertValue(
                value,
                type
        );
    }

    public void setCache(
            String key,
            Object value,
            long ttlSeconds
    ) {
        redisTemplate.opsForValue()
                .set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    public <T> T cacheAside(
            String key,
            Class<T> type,
            Supplier<T> fetchFunction,
            long ttlSeconds
    ) {
        T cached = getCache(key, type);

        if(cached != null) {
            return cached;
        }

        T data = fetchFunction.get();

        try {
            setCache(key, data, ttlSeconds);
        } catch (Exception ignored) {
            // Cache failure should not break the request
        }

        return data;
    }

    public long invalidatePattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);

        if(keys == null || keys.isEmpty()) {
            return 0L;
        }

        return redisTemplate.delete(keys);
    }

    public List<Object> mget(String... keys) {
        if(keys.length == 0) {
            return Collections.emptyList();
        }

        return redisTemplate.opsForValue().multiGet(Arrays.asList(keys));
    }


    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public long ttl(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire != null ? expire : -2L; // -2 means key does not exist
    }


//    public record CacheEntry(
//            String key,
//            Object value,
//            Long ttlSeconds
//    ) {}
//
//    public void mset(List<CacheEntry> entries) {
//
//        if (entries.isEmpty()) {
//            return;
//        }
//
//        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//
//            for (CacheEntry entry : entries) {
//
//                byte[] key = redisTemplate.getStringSerializer()
//                        .serialize(entry.key());
//
//                byte[] value = redisTemplate.getValueSerializer()
//                        .serialize(entry.value());
//
//                connection.stringCommands().set(key, value);
//
//                if (entry.ttlSeconds() != null
//                        && entry.ttlSeconds() > 0) {
//
//                    connection.keyCommands().expire(
//                            key,
//                            entry.ttlSeconds()
//                    );
//                }
//            }
//
//            return null;
//        });
//    }


}
