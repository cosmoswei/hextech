package com.wei.cache;

import com.wei.MyUser;

public class UserService {

    private final UserDao userDao = new UserDao();

    public MyUser getUserById(Long userId) {
        return userDao.findById(userId);
    }

    public void updateUser(MyUser user) {
        userDao.update(user);
    }
}