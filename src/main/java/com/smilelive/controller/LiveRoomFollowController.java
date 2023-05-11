package com.smilelive.controller;

import com.smilelive.entity.LiveRoomFollow;
import com.smilelive.service.LiveRoomFollowService;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
@Slf4j
@RestController
@RequestMapping("liveRoomFollow")
public class LiveRoomFollowController {
    @Resource
    private LiveRoomFollowService liveRoomFollowService;

    @GetMapping("queryByUserId")
    public Result queryByUserId(@RequestParam Long userId){
        return liveRoomFollowService.queryByUserId(userId);
    }
    @PostMapping("follow")
    public Result follow(@RequestParam("userId") Long userId,@RequestParam("liveUserId") Long liveUserId){
        log.info("f-->{},{}",userId,liveUserId);
        return liveRoomFollowService.follow(userId,liveUserId);
    }
    @PostMapping("cancelFollow")
    public Result cancelFollow(@RequestParam("userId") Long userId,@RequestParam("liveUserId") Long liveUserId){
        log.info("f-->{},{}",userId,liveUserId);
        return liveRoomFollowService.cancelFollow(userId,liveUserId);
    }
    @RequestMapping("isFollow")
    public Result isFollow(@RequestParam("userId") Long userId,@RequestParam("liveUserId") Long liveUserId){
        log.info("f-->{},{}",userId,liveUserId);
        return liveRoomFollowService.isFollow(userId,liveUserId);
    }
    @RequestMapping("queryCountByUserId")
    public Result queryCountByUserId(@RequestParam Long userId){
        return liveRoomFollowService.queryCountByUserId(userId);
    }
}
