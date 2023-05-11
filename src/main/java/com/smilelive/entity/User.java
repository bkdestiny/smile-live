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
    private Integer wallet;
    private String phone;
    private String avatar;
    private String sex;
    private LocalDate birthday;
    private LocalDateTime created;
    /*
    *   礼物榜价值
    * */
    @TableField(exist = false)
    private Integer giftValue;
    /*
    * 验证码
    * */
    @TableField(exist = false)
    private String captcha;
}
