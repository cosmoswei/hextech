package com.wei.cache.cache2;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setDatabase(0);
        return org.redisson.Redisson.create(config);
    }

    @Bean
    public RMapCache<String, String> redisCache(RedissonClient redissonClient) {
        return redissonClient.getMapCache("my-cache");
    }

    @Bean
    public CacheManager cacheManager(RMapCache<String, String> redisCache) {
        CacheStrategy strategy = new CacheAsideStrategy(redisCache);
        return new CacheManager(strategy);
    }
}