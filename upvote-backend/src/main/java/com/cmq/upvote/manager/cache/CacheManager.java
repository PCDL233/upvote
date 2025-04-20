package com.cmq.upvote.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CacheManager {

    /**
     * 热点 Key 检测器
     */
    private TopK hotKeyDetector;
    /**
     * caffeine 本地缓存
     */
    private Cache<String, Object> localCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 热点 Key 检测器
     * 1. 监控 Top 100 Key
     * 2. 宽度 100000
     * 3. 深度 5
     * 4. 衰减系数 0.92
     * 5. 最小出现 10 次才记录
     *
     * @return TopK
     */
    @Bean
    public TopK getHotKeyDetector() {
        hotKeyDetector = new HeavyKeeper(
                // 监控 Top 100 Key  
                100,
                // 宽度  
                100000,
                // 深度  
                5,
                // 衰减系数  
                0.92,
                // 最小出现 10 次才记录  
                10
        );
        return hotKeyDetector;
    }

    /**
     * caffeine 本地缓存
     * 1. 最大缓存 1000
     * 2. 过期时间 5 分钟
     *
     * @return Cache<String, Object>
     */
    @Bean
    public Cache<String, Object> localCache() {
        return localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 构造缓存 Key
     */
    private String buildKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    /**
     * 从缓存中获取数据
     */
    public Object get(String hashKey, String key) {
        String compositeKey = buildKey(hashKey, key);

        //1.从应用本地缓存中获取
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("从本地缓存中获取数据: {} = {}", compositeKey, value);
            //2.更新热点 Key 检测器
            hotKeyDetector.add(key, 1);
            return value;
        }

        //2.从 Redis 中获取
        Object redisValue = redisTemplate.opsForHash().get(hashKey, key);
        if (redisValue == null) {
            log.info("Redis 中不存在数据: {}", compositeKey);
            return null;
        }
        if (redisValue instanceof Number) {
            //如果是数字类型，则转换为 Long
            redisValue = ((Number) redisValue).longValue();
        }
        //记录访问（计数+1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        //3.如果热key存在并且不在本地缓存中，则缓存
        if (addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }
        return redisValue;
    }

    /**
     * 将数据放入本地缓存
     * 但前提是该数据的键已经存在于本地缓存中
     */
    public void putIfPresent(String hashKey, String key, Object value) {
        String compositeKey = buildKey(hashKey, key);
        Object object = localCache.getIfPresent(compositeKey);
        if (object == null) {
            return;
        }
        localCache.put(compositeKey, value);
    }

    /**
     * 定期清除过期的热点 Key
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void cleanHotKey() {
        // 清除过期的热点 Key
        hotKeyDetector.fading();
        log.info("清除过期的热点 Key");
    }
}
