package com.smilelive.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.smilelive.dto.GroupMsg;
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
    @Autowired
    private SocketIOServer socketIOServer;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @OnEvent ("joinLiveRoom")
    public void joinLiveRoom(SocketIOClient client, AckRequest ack,long roomId){
        client.joinRoom (roomId+"");
        Collection<SocketIOClient> clients = socketIOServer.getRoomOperations (roomId + "").getClients ();
        log.info("clients-->{}",clients.toString ());
        log.info(client.getSessionId ()+"join room-->{}",roomId);
    }
    @OnEvent ("sendChatMessage")
    public void sendChatMessage(SocketIOClient client, AckRequest ack, GroupMsg message){
        log.info ("message-->{}",message.getRoomId ());
        Collection<SocketIOClient> clients =socketIOServer.getRoomOperations (message.getRoomId ()+"").getClients ();
        log.info("clients->{},size:{}",clients.toString (),clients.size ());
        Iterator<SocketIOClient> iterator = clients.iterator ();
        while(iterator.hasNext ()){
            iterator.next ().sendEvent ("liveroomChat",message);
        }
    }
    @OnEvent ("leaveLiveRoom")
    public void leaveLiveRoom(SocketIOClient client,AckRequest ack,long roomId){
        client.leaveRoom (GROUP_KEY+roomId);
        System.out.println (client.getSessionId ()+"离开了房间"+roomId);
    }
}
