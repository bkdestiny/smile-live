package com.smilelive.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mchange.lang.LongUtils;
import com.smilelive.entity.LiveRoom;
import com.smilelive.entity.User;
import com.smilelive.mapper.LiveRoomMapper;
import com.smilelive.mapper.UserMapper;
import com.smilelive.service.LiveRoomService;
import com.smilelive.service.UserService;
import com.smilelive.utils.MyFileUtil;
import com.smilelive.utils.Result;
import com.smilelive.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {
    @Resource
    private UserService userService;
    @Resource
    private MyFileUtil myFileUtil;
    @Override
    public Result currentLiveRoom() {
        Long liveroomId= UserHolder.getUser ().getLiveroomId ();
        if(liveroomId==null){
            //未开通直播间
            return createLiveRoom ();
        }
        LiveRoom liveroom = getById (liveroomId);
        if(liveroom==null){
            return Result.fail ("您还未开通直播间");
        }
        return Result.ok (liveroom);
    }
    @Transactional
    public Result createLiveRoom(){
        if(UserHolder.getUser ().getLiveroomId ()!=null){
            return Result.fail ("您已经创建过直播间");
        }
        Long userId=UserHolder.getUser ().getId ();
        LiveRoom liveRoom=new LiveRoom ();
        long liveroomId=IdUtil.getSnowflakeNextId ();
        liveRoom.setId (liveroomId);
        boolean isSave = save(liveRoom);
        if(!isSave){
            return Result.fail ("直播间创建失败");
        }
        //创建成功
        boolean isUpdate = userService.update ().eq ("id", userId).set ("liveroom_id",liveroomId).update ();
        if(!isUpdate) {
            //修改失败
            removeById (liveroomId);
            return Result.fail ("直播间创建失败");
        }
        return Result.ok (liveRoom);
    }

    @Override
    public Result saveCover(MultipartFile file, Long id) {
        String filename = myFileUtil.saveImage (file, MyFileUtil.COVER_PATH);
        if(filename==null|| StrUtil.isBlank (filename)){
            return Result.fail ("修改封面失败");
        }
        String oldCover=query ().eq ("id",id).one ().getCover ();
        boolean isUpdate = update ().eq ("id", id).set ("cover", filename).update ();
        if(!isUpdate){
            myFileUtil.delImage (MyFileUtil.COVER_PATH,filename);
            return Result.fail ("修改封面失败");
        }
        myFileUtil.delImage (MyFileUtil.COVER_PATH,oldCover);
        return Result.ok ();
    }
    @Override
    @Transactional
    public Result saveLiveRoom(LiveRoom liveRoom) {
        Long userId=UserHolder.getUser ().getId ();
        Long liveroomId=UserHolder.getUser ().getLiveroomId ();
        if(liveroomId==null||liveroomId==0){
            //直播间不存在，创建直播间
            return createLiveRoom ();
        }
        //直播间存在，更新直播间信息
        boolean isUpdate = updateById (liveRoom);
        if(!isUpdate){
            return Result.fail ("修改直播间信息失败");
        }
        return Result.ok ();
    }
}
