package com.wei.mq;

import lombok.Data;

// 示例 JavaBean
@Data
class Person {
    private Long id;
    private int age;
    private String name;
    private String phone;
    private String color;

    @Override
    public String toString() {
        return "Person{id=" + id + ", age=" + age + ", name='" + name + "', phone='" + phone + "', color='" + color + "'}";
    }
}