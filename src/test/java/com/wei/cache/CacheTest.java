package com.wei.cache;

import com.wei.MockService;
import com.wei.MyUser;
import com.wei.cappuccino.Cappuccino;
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
        Cappuccino cappuccino = CappuccinoFactory.newInstance(cappuccinoConfig);
        UserService userService = new UserService();
        // 查询用户
        long userId = 122222L;
        MyUser user = MockService.getUser(userId);
        Long finalUserId1 = userId;
        MyUser myUser = cappuccino.get("user:" + userId,
                () -> userService.getUserById(finalUserId1));
        System.out.println("Updated User 1: " + myUser);
        // 更新用户
        cappuccino.fail("user:" + userId, (z) -> userService.updateUser(user));
        for (int i = 0; i < 4; i++) {
            myUser = cappuccino.get("user:" + userId,
                    () -> userService.getUserById(finalUserId1));
            System.out.println("Updated User 2: " + myUser);
            user.setName("New Name");
            if (i / 2 == 0) {
                cappuccino.fail("user:" + userId, (x) -> userService.updateUser(user));
            }
            MyUser updatedUser = cappuccino.get("user:" + userId,
                    () -> userService.getUserById(finalUserId1));
            System.out.println("Updated User 3: " + updatedUser);
        }

        cappuccino.shutdown();
    }
}