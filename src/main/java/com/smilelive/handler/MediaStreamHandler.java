package com.smilelive.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.smilelive.dto.StreamData;
import com.smilelive.entity.LiveRoom;
import com.smilelive.utils.DealProcessStream;
import com.smilelive.utils.RedisContent;
import com.smilelive.utils.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MediaStreamHandler {
    @Autowired
    private SocketIOServer socketIOServer;
    private static Map<Long,Process> map=new ConcurrentHashMap<> ();
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @OnEvent ("publishStream")
    public void publishStream(SocketIOClient client, AckRequest ack,long id){
        try {
            log.info(id+"开启直播");
            Process p = map.get (id);
            if(p!=null){
                p.destroy ();
                map.remove (p);
            }
            System.out.println ("id -->"+id);
            //UUID sessionId=client.getSessionId ();
            //["-fflags","nobuffer","-i","-","-vcodec","libx264","-f","flv","rtmp://192.168.88.130/myapp/s"]
            //String command="ffmpeg -fflags nobuffer -i - -vcodec libx264 -f flv rtmp://192.168.88.130/myapp/s";
            //Process process = Runtime.getRuntime ().exec (command);
            List<String> list=new ArrayList<> ();
            list.add ("ffmpeg");
            list.add ("-fflags");
            list.add ("nobuffer");
            list.add ("-i");
            list.add ("-");
            list.add ("-vcodec");
            //list.add ("libx264");
            list.add ("copy");//在客户端/浏览器 直接转码h264 ，这里只需要拷贝（只支持chrome浏览器）
            list.add ("-f");
            list.add ("flv");
            String url="rtmp://192.168.88.130/myapp/"+id;
            System.out.println (url);
            list.add (url);
            ProcessBuilder processBuilder = new ProcessBuilder ();
            processBuilder.command (list);
            processBuilder.redirectErrorStream (true);
            Process process = processBuilder.start ();
/*           ProcessWrapper ffmpeg = new DefaultFFMPEGLocator ().createExecutor();
            ffmpeg.addArgument ("-fflags");
            ffmpeg.addArgument ("nobuffer");
            ffmpeg.addArgument ("-i");
            ffmpeg.addArgument ("-");
            ffmpeg.addArgument ("-vcodec");
            ffmpeg.addArgument ("libx264");
            ffmpeg.addArgument ("-f");
            ffmpeg.addArgument ("flv");
            ffmpeg.addArgument ("rtmp://192.168.88.130/myapp/q");
            ffmpeg.execute ();*/

            /*
            问题描述
            Java程序中使用Runtime.getRuntime().exec()执行ffmpeg推流命令，大约五分钟后，推流停止，ffplay无法接收到流，但推流进程仍存在。

            解决方法
            process的阻塞：
            在runtime执行大点的命令中，输入流和错误流会不断有流进入存储在JVM的缓冲区中，如果缓冲区的流不被读取被填满时，就会造成runtime的阻塞。所以在进行比如：大文件复制等的操作时，我们还需要不断的去读取JVM中的缓冲区的流，来防止Runtime的死锁阻塞。
            利用单独两个线程，分别处理process的getInputStream()和getErrorSteam()，防止缓冲区被撑满，导致阻塞；
            */
            new Thread (new DealProcessStream (process.getInputStream ())).start ();
            new Thread (new DealProcessStream (process.getErrorStream ())).start ();
            map.put (id,process);
            ack.sendAckData (Result.ok ());
        }catch (Exception e){
            ack.sendAckData (Result.fail ("服务器异常"));
            e.printStackTrace ();
        }
    }
    @OnEvent ("downLive")
    public void downLive(SocketIOClient client,AckRequest ack,long id){
        try {
            //删除进程
            map.get (id).destroy ();
            map.remove (id);
            ack.sendAckData (Result.ok ());
        }catch (Exception e){
            e.printStackTrace ();
        }
    }
    @OnEvent ("streamData")
    public void streamData(SocketIOClient client, AckRequest ack, StreamData data){
        boolean channelOpen = client.isChannelOpen ();
        if(!channelOpen){
            System.out.println ();
        }
        //System.out.println ("b -->"+data.getData ().length+"id-->"+data.getId ());
        //System.out.println ("b -->"+b.length);
        UUID sessionId=client.getSessionId ();
        try {
            //1.获取Process
            Process process=map.get (data.getId ());
            if(process==null){
                ack.sendAckData (Result.fail ("进程关闭"));
            }
            //推流
            OutputStream outputStream = process.getOutputStream ();
            outputStream.write (data.getData ());
            ack.sendAckData (Result.ok ());
        }catch (IOException e){
            ack.sendAckData (Result.fail ("服务器异常"));
            e.printStackTrace ();
        }
    }

    public static Map<Long, Process> getMap() {
        return map;
    }
}
