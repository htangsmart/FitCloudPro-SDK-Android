package com.github.kilnn.wristband2.sample.mock;

import java.util.Random;

/**
 * Mock login user system
 */
public class UserMock {

    public static User getLoginUser() {
        Random random = new Random();
        if (random.nextInt(10) > 5) {
            return mockUser1();
        } else {
            return mockUser2();
        }
    }

    private static User mockUser1() {
        User user = new User();
        user.setId(1);
        user.setAge(22);
        user.setSex(true);
        user.setHeight(180);
        user.setWeight(65);
        return user;
    }

    private static User mockUser2() {
        User user = new User();
        user.setId(2);
        user.setAge(30);
        user.setSex(false);
        user.setHeight(160);
        user.setWeight(50);
        return user;
    }

}
