package com.wei;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockService {

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            putUser(getUser(12L));
        }
    }

    static Map<Long, MyUser> myUserMap = new ConcurrentHashMap<>();

    public static void putUser(MyUser myUser) {
        safeSleep(2);
        myUserMap.putIfAbsent(myUser.getId(), myUser);
    }


    public static MyUser getUser(Long id) {
        MyUser myUser = myUserMap.get(id);
        if (myUser != null) {
            return myUser;
        }
        myUser = new MyUser(id);
        myUser.setAge(12);
        myUser.setId(id);
        myUser.setName("cosmoswei");
        safeSleep(4);
        myUserMap.putIfAbsent(id, myUser);
        return myUser;
    }

    public static void deleteUser(Long id) {
        myUserMap.remove(id);
        safeSleep(5);
    }




    private static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

