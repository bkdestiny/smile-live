package com.smilelive.config;

import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.smilelive.handler.ChatHandler;
import com.smilelive.handler.MediaStreamHandler;
import com.smilelive.handler.SocketIOHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @ClassNameSocketIOConfig
 * @Description TODO
 * @Author DELL
 * @Date 2022/1/2114:19
 * @Version 1.0
 **/
@Configuration
public class SocketIOConfig{

/*   @Resource
    private SocketIOHandler socketIOHandler;
    @Resource
    private MediaStreamHandler mediaStreamHandler;
    @Resource
    private ChatHandler chatHandler;*/
    @Value("${socketio.host}")
    private String host;

    @Value("${socketio.port}")
    private Integer port;

    @Value("${socketio.bossCount}")
    private int bossCount;

    @Value("${socketio.workCount}")
    private int workCount;

    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests;

    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout;

    @Value("${socketio.pingTimeout}")
    private int pingTimeout;

    @Value("${socketio.pingInterval}")
    private int pingInterval;
    @Bean("socketIOServer")
    public SocketIOServer socketIOServer() throws Exception {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        socketConfig.setTcpKeepAlive (true);
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setSocketConfig(socketConfig);
        // host在本地测试可以设置为localhost或者本机IP，在Linux服务器跑可换成服务器IP
        configuration.setHostname(host);
        configuration.setPort(port);
        // socket连接数大小（如只监听一个端口boss线程组为1即可）
        configuration.setBossThreads(bossCount);
        configuration.setWorkerThreads(workCount);
        configuration.setAllowCustomRequests(allowCustomRequests);
        // 协议升级超时时间（毫秒），默认10秒。HTTP握手升级为ws协议超时时间
        configuration.setUpgradeTimeout(upgradeTimeout);
        // Ping消息超时时间（毫秒），默认60秒，这个时间间隔内没有接收到心跳消息就会发送超时事件
        configuration.setPingTimeout(pingTimeout);
        // Ping消息间隔（毫秒），默认25秒。客户端向服务器发送一条心跳消息间隔
        configuration.setPingInterval(pingInterval);
        //configuration.setTransports (Transport.WEBSOCKET);//指定传输协议为websocket
        SocketIOServer socketIOServer = new SocketIOServer(configuration);
        //添加事件监听器
/*        socketIOServer.addListeners(socketIOHandler);
        socketIOServer.addListeners (mediaStreamHandler);
        socketIOServer.addListeners (chatHandler);*/
        //启动SocketIOServer
        socketIOServer.start();
        System.out.println("SocketIO启动完毕");
        return socketIOServer;
    }
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }
}
