package com.smilelive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("gift")
public class Gift {
    private Long id;
    private String name;
    private Integer value;
    private String image;
}
