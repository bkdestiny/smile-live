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
    private String title;
    private String cover;
    private String classtify;
    @TableField(exist = false)
    private long viewer;
    @TableField(exist = false)
    private long isLive;
}
