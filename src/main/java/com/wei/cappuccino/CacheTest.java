package com.wei.cappuccino;

import com.wei.MockService;
import com.wei.MyUser;
import org.redisson.api.RedissonClient;

public class CacheTest {
    public static void main(String[] args) {
        RedissonClient redissonClient = RedissonFactory.create();

        CacheBase l1Cache = new CaffeineCache();
        CacheBase l2Cache = new RedisCache();
        CacheManager cacheManager = new CacheManager(l1Cache, l2Cache);
        UserDao userDao = new UserDao(); // 假设是数据库访问类
        UserService userService = new UserService(cacheManager, userDao);
        // 查询用户
        MyUser user = MockService.getUser(123L);

        for (int i = 0; i <= 3; i++) {
            System.out.println("User: " + user);
            // 更新用户
            user.setName("New Name");
//            userService.updateUser(user);

            // 再次查询，确保数据被更新
            MyUser updatedUser = userService.getUserById(123L);
            System.out.println("Updated User: " + updatedUser);
        }
    }
}