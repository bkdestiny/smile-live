package com.smilelive.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.smilelive.entity.User;
import com.smilelive.utils.RedisContent;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SocketIOHandler {
    @Autowired
    private SocketIOServer socketIOServer;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final Map<String,SocketIOClient> clientMap=new ConcurrentHashMap<String,SocketIOClient> ();

    @OnConnect
    public void onConnect(SocketIOClient client){
        String sessionId = client.getSessionId ().toString ();
        clientMap.put (sessionId,client);
        System.out.println ("sessionId"+client.getSessionId ()+"连接成功");
    }
    @OnDisconnect
    public void onDisconnect(SocketIOClient client){
        String sessionId = client.getSessionId ().toString ();
        clientMap.remove (sessionId);
        System.out.println ("sessionId"+client.getSessionId ()+"断开连接");
    }
    @OnEvent ("currentUser")
    public void currentUser(SocketIOClient client, AckRequest ack,String token) {
        log.info ("token -->" + token);
        String tokenKey = RedisContent.TOKEN_KEY + token;
        String tokenJson = stringRedisTemplate.opsForValue ().get (tokenKey);
        if (StrUtil.isBlank (tokenJson)) {
            client.sendEvent ("currentUser", Result.fail ("自动登录失败"));
            return;
        }
        User user = JSONUtil.toBean (tokenJson, User.class);
        client.sendEvent ("currentUser", Result.ok (user));
    }
    public static Map<String, SocketIOClient> getClientMap() {
        return clientMap;
    }
}
