package com.wei.cache;

import com.wei.MyUser;

public class UserService {
    private final CacheManager cacheManager;
    private final UserDao userDao;

    public UserService(CacheManager cacheManager, UserDao userDao) {
        this.cacheManager = cacheManager;
        this.userDao = userDao;
    }

    public MyUser getUserById(Long userId) {
        return cacheManager.get("user:" + userId, MyUser.class, () -> userDao.findById(userId));
    }

    public void updateUser(MyUser user) {
        userDao.update(user);
        cacheManager.delete("user:" + user.getId()); // 数据变更后，删除缓存
    }
}