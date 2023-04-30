package com.smilelive;

import cn.hutool.core.io.FileUtil;
import com.smilelive.entity.LiveRoom;
import com.smilelive.mapper.LiveRoomMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@SpringBootTest
class SmileLiveApplicationTests {
    @Resource
    private LiveRoomMapper liveRoomMapper;
    @Test
    void contextLoads() {
        FileUtil.mkdir ("abc");
        FileUtil.touch ("abc/test.txt");
    }
    @Test
    void getLiveRooms(){
        List<LiveRoom> liveRooms = liveRoomMapper.getLiveRooms ();
        System.out.println ("liverooms-->"+liveRooms);
    }
}
