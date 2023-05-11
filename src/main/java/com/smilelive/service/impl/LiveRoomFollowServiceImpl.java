package com.smilelive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smilelive.entity.LiveRoomFollow;
import com.smilelive.handler.MediaStreamHandler;
import com.smilelive.mapper.LiveRoomFollowMapper;
import com.smilelive.service.LiveRoomFollowService;
import com.smilelive.service.LiveRoomService;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.sql.Wrapper;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LiveRoomFollowServiceImpl extends ServiceImpl<LiveRoomFollowMapper, LiveRoomFollow> implements LiveRoomFollowService  {
    @Autowired
    private MediaStreamHandler mediaStreamHandler;

    @Override
    public Result queryByUserId(Long userId) {
        List<LiveRoomFollow> liveRoomFollows = getBaseMapper ().queryByUserId (userId);
        Map map=MediaStreamHandler.getLiveMap ();
        for(LiveRoomFollow follow:liveRoomFollows){
            if(map.get (follow.getLiveUserId ())!=null){
                follow.setLive (true);
            }
        }
        return Result.ok (liveRoomFollows);
    }

    @Override
    public Result follow(Long userId,Long liveUserId) {
        Long count = query ().eq ("user_id", userId).eq ("live_user_id", liveUserId).count ();
        if(count>0){
            return Result.fail ("您已经关注此直播间了");
        }
        boolean save = save (new LiveRoomFollow (userId,liveUserId));
        if(!save){
            return Result.fail ("关注失败");
        }
        return Result.ok ();
    }

    @Override
    public Result cancelFollow(Long userId,Long liveUserId) {
        boolean remove = remove (new QueryWrapper<LiveRoomFollow> ().eq ("user_id",userId).eq ("live_user_id",liveUserId));
        if(!remove){
            Result.fail ("取消关注失败");
        }
        return Result.ok ();
    }

    @Override
    public Result isFollow(Long userId,Long liveUserId) {
        Long count = query ().eq ("user_id", userId).eq("live_user_id",liveUserId).count ();
        if(count==0){
            return Result.ok (false);
        }
        return Result.ok (true);
    }

    @Override
    public Result queryCountByUserId(Long userId) {
        Long count = query ().eq ("live_user_id", userId).count ();
        return Result.ok (count);
    }

}
