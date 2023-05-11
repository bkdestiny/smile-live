package com.smilelive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smilelive.entity.LiveRoomFollow;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LiveRoomFollowMapper extends BaseMapper<LiveRoomFollow> {
    @Select ("select f.*,u.nickname,u.avatar,l.title,l.cover,l.classtify from liveroom_follow f inner join user u on f.live_user_id=u.id inner join liveroom l on f.live_user_id=l.user_id where f.user_id=#{id}")
    List<LiveRoomFollow> queryByUserId(Long userId);
}
