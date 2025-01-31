package com.wei.cache.cache2;

import com.wei.MockService;

public class UserService {
    private final CacheManager cacheManager;

    public UserService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Object getUserById(String userId) {
        return cacheManager.get("user:" + userId, Object.class,
                () -> MockService.getUser(Long.valueOf(userId)));
    }

    public void updateUser(Object user) {
        MockService.deleteUser(10086L);
        cacheManager.delete("user:" + 10086L);
    }
}