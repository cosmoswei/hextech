package com.wei.cache.cache1;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.Redisson;

public class RedissonFactory {
    public static RedissonClient create() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://redis-10132.c10.us-east-1-4.ec2.redns.redis-cloud.com:10132").setPassword("DxMoBE9Ym1PtT298zpYMjNVDLxCLuGep").setDatabase(0);
        return Redisson.create(config);
    }
}