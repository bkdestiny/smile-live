package com.smilelive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.tomcat.jni.Proc;

@Data
@TableName("liveroom")
public class LiveRoom {
    private long id;
    @TableField("user_id")
    private long userId;
    private String title;
    private String cover;
    private String classtify;
    @TableField(exist = false)
    private long viewer;
    @TableField(exist = false)
    private boolean isLive=false;
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private Integer followCount;
    @TableField(exist = false)
    private Boolean follow=false;
}
