package com.wei.completableCache;

import com.wei.MyUser;

public class UserService {
    private final CompletableCacheManager completableCacheManager;
    private final UserDao userDao;

    public UserService(CompletableCacheManager completableCacheManager, UserDao userDao) {
        this.completableCacheManager = completableCacheManager;
        this.userDao = userDao;
    }

    public MyUser getUserById(Long userId) {
        return completableCacheManager.get("user:" + userId, MyUser.class, () -> userDao.findById(userId));
    }

    public void updateUser(MyUser user) {
        userDao.update(user);
        completableCacheManager.delete("user:" + user.getId()); // 数据变更后，删除缓存
    }
}