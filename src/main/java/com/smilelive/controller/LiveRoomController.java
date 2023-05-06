package com.smilelive.controller;

import com.smilelive.entity.LiveRoom;
import com.smilelive.handler.MediaStreamHandler;
import com.smilelive.service.LiveRoomService;
import com.smilelive.utils.MyFileUtil;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
@Slf4j
@RestController
@RequestMapping("liveroom")
public class LiveRoomController {
    @Resource
    private MyFileUtil myFileUtil;
    @Resource
    private LiveRoomService liveRoomService;
    @RequestMapping("currentLiveRoom")
    public Result currentLiveRoom(){
        return liveRoomService.currentLiveRoom();
    }
    @PostMapping("saveLiveRoom")
    public Result saveLiveRoom(LiveRoom liveRoom){
        return liveRoomService.saveLiveRoom(liveRoom);
    }
    @PostMapping("createLiveRoom")
    public Result createLiveRoom(){
        return liveRoomService.createLiveRoom();
    }
    @RequestMapping("saveCover")
    public Result saveCover(@RequestParam("file") MultipartFile file,@RequestParam Long id){
        log.info("saveCover -->{}",id);
        return liveRoomService.saveCover(file,id);
    }
    @RequestMapping("getAll")
    public Result getAll(){
        return liveRoomService.getAll();
    }
    @RequestMapping("queryById")
    public Result queryById(@RequestParam Long id){
        return liveRoomService.queryById(id);
    }

}
