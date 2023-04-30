package com.smilelive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smilelive.entity.LiveRoom;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LiveRoomMapper extends BaseMapper<LiveRoom> {
    @Select("select l.*,u.username,u.nickname,u.avatar from liveroom l inner join user u on l.user_id=u.id")
    List<LiveRoom> getLiveRooms();
    @Select("select l.*,u.username,u.nickname,u.avatar from liveroom l inner join user u on l.user_id=u.id where l.id=#{id}")
    LiveRoom queryById(Long id);
}
