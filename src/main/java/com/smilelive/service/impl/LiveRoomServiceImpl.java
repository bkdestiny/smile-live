package com.smilelive.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.corundumstudio.socketio.SocketIOServer;
import com.mchange.lang.LongUtils;
import com.smilelive.entity.LiveRoom;
import com.smilelive.entity.User;
import com.smilelive.handler.MediaStreamHandler;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.smilelive.handler.ChatHandler.LIVEROOM_KEY;

@Service
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {
    @Resource
    private UserService userService;
    @Resource
    private MyFileUtil myFileUtil;
    @Resource
    private MediaStreamHandler mediaStreamHandler;
    @Resource
    private SocketIOServer socketIOServer;
    /*获取当前用户的直播间*/
    @Override
    public Result currentLiveRoom() {
        LiveRoom liveroom = query ().eq ("user_id", UserHolder.getUser ().getId ()).one ();
        if(liveroom==null){
            //未开通直播间
            return createLiveRoom ();
        }
        //获取当前直播间观看人数
        pushViewer (liveroom);
        return Result.ok (liveroom);
    }
    @Transactional
    public Result createLiveRoom(){
        //查询直播间是否存在
        LiveRoom query = query ().eq ("user_id", UserHolder.getUser ().getId ()).one ();
        if(query!=null){
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
    /*保存直播间封面*/
    @Override
    public Result saveCover(MultipartFile file, Long id) {
        String path = myFileUtil.saveImage (file, MyFileUtil.COVER_PATH);
        if(path ==null|| StrUtil.isBlank (path )){
            return Result.fail ("修改封面失败");
        }
        String oldCover=query ().eq ("id",id).one ().getCover ();
        boolean isUpdate = update ().eq ("id", id).set ("cover", path).update ();
        if(!isUpdate){
            myFileUtil.delImage (path );
            return Result.fail ("修改封面失败");
        }
        myFileUtil.delImage (path);
        return Result.ok ();
    }
    //获取当前直播间观看人数
    private void pushViewer(LiveRoom liveRoom){
        int size = socketIOServer.getRoomOperations (LIVEROOM_KEY + liveRoom.getId ()).getClients ().size ();
        liveRoom.setViewer (size);
    }
    @Override
    public Result getAll() {
        Map<Long, Process> map = MediaStreamHandler.getMap ();
        List<LiveRoom> list = getBaseMapper ().getLiveRooms ();
        Iterator<LiveRoom> iterator = list.iterator ();
        while(iterator.hasNext ()){
            LiveRoom next = iterator.next ();
            //获取当前直播间是否正在直播
            if(map.get (next.getId ())!=null){
                next.setLive (true);
            }
            //获取当前直播间观看人数
            pushViewer (next);
        }
        return Result.ok (list);
    }
    /*根据Id获取直播间信息*/
    @Override
    public Result queryById(Long id) {
        LiveRoom liveroom = getBaseMapper ().queryById (id);
        if(liveroom==null){
            return Result.fail ("获取直播间信息失败");
        }
        if(MediaStreamHandler.getMap ().get (id)!=null){
            liveroom.setLive (true);
        }
        //获取当前直播间观看人数
        pushViewer (liveroom);
        return Result.ok (liveroom);
    }

    @Override
    @Transactional
    /*更新直播间信息*/
    public Result saveLiveRoom(LiveRoom liveRoom) {
        //直播间存在，更新直播间信息
        boolean isUpdate = updateById (liveRoom);
        if(!isUpdate){
            return Result.fail ("修改直播间信息失败");
        }
        return Result.ok ();
    }
}
