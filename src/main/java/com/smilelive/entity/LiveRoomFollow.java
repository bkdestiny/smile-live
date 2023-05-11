package com.smilelive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "liveroom_follow")
public class LiveRoomFollow {
    private Long id;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "live_user_id")
    private Long liveUserId;
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String title;
    @TableField(exist = false)
    private String cover;
    @TableField(exist = false)
    private String classtify;
    @TableField(exist = false)
    private boolean isLive=false;
    public LiveRoomFollow(){}
    public LiveRoomFollow(Long userId, Long liveUserId) {
        this.userId = userId;
        this.liveUserId = liveUserId;
    }
}
