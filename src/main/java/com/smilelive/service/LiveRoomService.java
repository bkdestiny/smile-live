package com.smilelive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smilelive.entity.LiveRoom;
import com.smilelive.utils.Result;
import org.springframework.web.multipart.MultipartFile;

public interface LiveRoomService extends IService<LiveRoom> {

    Result currentLiveRoom();

    Result saveLiveRoom(LiveRoom liveRoom);

    Result createLiveRoom();

    Result saveCover(MultipartFile file, Long id);

    Result getAll();

    Result queryById(Long id);

    Result queryByUserId(Long userId);
}
