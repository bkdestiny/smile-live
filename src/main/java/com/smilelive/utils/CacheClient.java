package com.smilelive.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;



/*
    封装Redis工具类
*/

@Component
@Slf4j
public class CacheClient {
    @Resource
    private  StringRedisTemplate stringRedisTemplate;
    //线程池
    private static final ExecutorService CACHE_REBULD_EXECUTOR= Executors.newFixedThreadPool (10);

    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue ().set (key, JSONUtil.toJsonStr (value),time,unit);
    }
    //解决缓存穿透,逻辑过期
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        //设置逻辑过期
        RedisData redisData=new RedisData ();
        redisData.setData (value);
        redisData.setExpireTime (LocalDateTime.now ().plusSeconds (unit.toSeconds (time)));
        //写入Redis
        stringRedisTemplate.opsForValue ().set (key, JSONUtil.toJsonStr (redisData));
    }
    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long time,TimeUnit unit){
        String key=keyPrefix+id;
        //1.从redis查询缓存
        String json=stringRedisTemplate.opsForValue ().get (key);
        //2.判断是否存在
        if(StrUtil.isNotBlank (json)){
            //存在,直接返回
            return JSONUtil.toBean (json,type);
        }
        //判断命中的是否是空值,(解决缓存穿透)
        if(json!=null){
            //返回一个错误信息
            return null;
        }
        //不存在,用传入的方法从数据库查询
        R r=dbFallback.apply (id);
        if(r==null){
            //不存在,将空值写入redis
            stringRedisTemplate.opsForValue ().set (key,"",5L,TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }
        //存在,将数据和逻辑过期时间写入redis
        this.set (key,r,time,unit);
        return r;
    }
    //逻辑过期解决缓存击穿
    public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long time,TimeUnit unit) {
        String key = keyPrefix + id;
        //从redis查询缓存
        String json = stringRedisTemplate.opsForValue ().get (key);
        //判断是否存在
        if (StrUtil.isBlank (json)) {
            //不存在,直接返回空
            return null;
        }
        RedisData redisData = JSONUtil.toBean (json,RedisData.class);
        R r = BeanUtil.toBean (redisData.getData (), type);
        LocalDateTime expireTime = redisData.getExpireTime ();
        //存在,判断缓存是否过期
        if (expireTime.isAfter (LocalDateTime.now ())) {
            //未过期,直接返回数据
            return r;
        }
        //已过期,需要缓存重建
        //获取互斥锁
        boolean isLock = tryLock (key);
        //判断是否获取成功
        if (isLock) {
            // 获取锁成功,开启独立线程,实现缓存重建
            CACHE_REBULD_EXECUTOR.submit (() -> {
                try {
                    //查询数据库
                    R r1= dbFallback.apply (id);
                    //重建缓存
                    this.setWithLogicalExpire (key, r,time,unit);
                } catch (Exception e) {
                    e.printStackTrace ();
                } finally {
                    //释放锁
                    unlock (key);
                }
            });
        }
        //获取锁失败,返回旧数据
        return r;
    }
    private boolean tryLock(String key){
        Boolean flag=stringRedisTemplate.opsForValue ().setIfAbsent (key,"1",RedisContent.TRYLOCK_TTL,TimeUnit.SECONDS);
        return BooleanUtil.isTrue (flag);
    }
    private void unlock(String key){
        stringRedisTemplate.delete (key);
    }
}
