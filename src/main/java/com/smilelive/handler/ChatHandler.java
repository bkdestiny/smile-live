package com.smilelive.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.smilelive.dto.GroupMsg;
import com.smilelive.entity.GiftRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class ChatHandler {
    private static final String SINGLE_KEY="single:";
    private static final String GROUP_KEY="group:";
    public static final String LIVEROOM_KEY="liveroom:";
    @Autowired
    private SocketIOServer socketIOServer;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @OnEvent ("joinLiveRoom")
    public void joinLiveRoom(SocketIOClient client, AckRequest ack,Long id){
        client.joinRoom (LIVEROOM_KEY+id);
    }
    @OnEvent ("sendChatMessage")
    public void sendChatMessage(SocketIOClient client, AckRequest ack, GroupMsg msg){
        msg.setType ("USER");
        /*根据msg的房间id获取*/
        Collection<SocketIOClient> clients =socketIOServer.getRoomOperations (LIVEROOM_KEY+msg.getRoomId ()).getClients ();
        Iterator<SocketIOClient> iterator = clients.iterator ();
        while(iterator.hasNext ()){
            iterator.next ().sendEvent ("liveroomChat",msg);
        }
    }
    @OnEvent ("sendGiftMessage")
    public void sendGiftMessage(SocketIOClient client, AckRequest ack, GiftRecord record){
        String content=" 送出了 "+record.getCount ()+" x ";
        //创建直播间礼物消息
        GroupMsg msg = GroupMsg.createGiftMsg (record.getReceiver (), record.getGiver (),record.getGiverName (),content, record.getGiftImage ());
        Collection<SocketIOClient> clients = socketIOServer.getRoomOperations (LIVEROOM_KEY + record.getReceiver ()).getClients ();
        Iterator<SocketIOClient> iterator = clients.iterator ();
        while(iterator.hasNext ()){
            iterator.next ().sendEvent ("liveroomChat",msg);
        }
    }
    @OnEvent ("leaveLiveRoom")
    public void leaveLiveRoom(SocketIOClient client,AckRequest ack,Long id){
        client.leaveRoom (LIVEROOM_KEY+id);
    }
}
