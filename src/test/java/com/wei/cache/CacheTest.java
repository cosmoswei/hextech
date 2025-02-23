package com.wei.cache;

import com.wei.MockService;
import com.wei.MyUser;
import com.wei.cappuccino.CacheManager;
import com.wei.cappuccino.facade.CappuccinoConfig;
import com.wei.cappuccino.facade.CappuccinoFactory;

public class CacheTest {
    public static void main(String[] args) {
        CappuccinoConfig cappuccinoConfig = CappuccinoConfig.builder()
                .caffeineTtl(200L)
                .caffeineMacSize(200)
                .redisUri("redis://120.76.41.234:8866")
                .redisPassword("huangxuwei")
                .build();
        CacheManager cacheManager = CappuccinoFactory.newInstance(cappuccinoConfig);
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