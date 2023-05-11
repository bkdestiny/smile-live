package com.smilelive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smilelive.entity.LiveRoomFollow;
import com.smilelive.utils.Result;

public interface LiveRoomFollowService extends IService<LiveRoomFollow> {
    Result queryByUserId(Long userId);

    Result follow(Long userId,Long liveUserId);

    Result cancelFollow(Long userId,Long liveUserId);

    Result isFollow(Long userId,Long liveUserId);

    Result queryCountByUserId(Long userId);
}
