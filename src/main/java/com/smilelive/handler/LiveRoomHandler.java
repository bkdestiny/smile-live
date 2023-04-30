package com.smilelive.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.smilelive.dto.LiveChatMessage;
import com.smilelive.entity.LiveRoom;
import com.smilelive.utils.RedisContent;
import com.smilelive.utils.Result;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveRoomHandler {
    private Map<Long,Map<UUID,SocketIOClient>> onlineMap=new ConcurrentHashMap<> ();
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @OnEvent ("joinLiveRoom")
    public void joinLiveRoom(SocketIOClient client, AckRequest ack,long roomId){
        UUID sessionId=client.getSessionId ();
        Map<UUID, SocketIOClient> map = onlineMap.get (roomId);
        if(map==null){
            onlineMap.put (roomId,new HashMap<UUID,SocketIOClient> ());
        }
        map.put (sessionId,client);
        System.out.println (sessionId+"加入了房间！");
        onlineMap.put (roomId,map);
    }
    @OnEvent ("sendChatMessage")
    public void sendChatMessage(SocketIOClient client, AckRequest ack, LiveChatMessage message){
        Map<UUID,SocketIOClient> map=onlineMap.get (message.getRoomId ());
        Iterator<Map.Entry<UUID,SocketIOClient>> iterator = map.entrySet ().iterator ();
        while(iterator.hasNext ()) {
            iterator.next ().getValue ().sendEvent ("liveChatMessage",Result.ok (message));
        }
    }
    @OnEvent ("leaveLiveRoom")
    public void leaveLiveRoom(SocketIOClient client,AckRequest ack,long roomId){
        Map<UUID, SocketIOClient> map = onlineMap.get (roomId);
        if(map==null){
            return;
        }
        UUID sessionId=client.getSessionId ();
        map.remove (sessionId);
        onlineMap.put (roomId,map);
        System.out.println (sessionId+"离开了房间"+roomId);
    }
}
