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
        System.out.println("User: " + myUser);
        {
            myUser = cappuccino.get("user:" + userId,
                    () -> userService.getUserById(finalUserId1));
            System.out.println("User: " + myUser);
            // 更新用户
            user.setName("New Name");
            userService.updateUser(user);
            cappuccino.fail("user:" + userId, (userId2) -> userService.updateUser(user));

            userId = userId + 0;
            Long finalUserId = userId;
            MyUser updatedUser = cappuccino.get("user:" + userId,
                    () -> userService.getUserById(finalUserId));
            System.out.println("Updated User: " + updatedUser);
        }
    }
}