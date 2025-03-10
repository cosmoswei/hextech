package com.wei.cappuccino.facade;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wei.cappuccino.*;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CappuccinoFactory {

    private static final Logger log = LoggerFactory.getLogger(CappuccinoFactory.class);

    public static Cappuccino newInstance(CappuccinoConfig cappuccinoConfig) {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .expireAfterWrite(cappuccinoConfig.getCaffeineTtl(),
                        TimeUnit.MILLISECONDS)
                .maximumSize(cappuccinoConfig.getCaffeineMacSize())
                .build();
        MessageNotify messageNotify = new RedisNotify();
        CaffeineCache l1Cache = new CaffeineCache(cache, messageNotify);
        Config config = new Config();
        config.setCodec(StringCodec.INSTANCE)
                .useSingleServer()
                .setAddress(cappuccinoConfig.getRedisUri())
                .setPassword(cappuccinoConfig.getRedisPassword());
        RedissonClient redissonClient = RedissonHelper.init(config);
        CacheBase l2Cache = new RedisCache(redissonClient, cappuccinoConfig.getTopic());
        Cappuccino cappuccino = new Cappuccino(l1Cache, l2Cache);
        log.debug("new a Cappuccino cache instance, config is {}", cappuccinoConfig);
        return cappuccino;
    }
}
