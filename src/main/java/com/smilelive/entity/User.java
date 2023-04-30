package com.smilelive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {
    private long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String avatar;
    private String sex;
    private LocalDate birthday;
    private LocalDateTime created;
}
