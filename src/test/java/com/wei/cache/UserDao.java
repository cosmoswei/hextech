package com.wei.cache;

import com.wei.MyUser;

public class UserDao {
    public void update(MyUser user) {

    }

    public MyUser findById(Long userId) {
        MyUser myUser = new MyUser(userId);
        myUser.setAge(24);
        myUser.setName("name after dao");
        return myUser;
    }
}
