package com.wei;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyUser {
    private String name;
    private Long id;
    private Integer age;

    public MyUser(Long userId) {
        this.id = userId;
    }
}
