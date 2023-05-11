package com.smilelive.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.smilelive.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate=stringRedisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("RefreshToken");
        //1.从请求头authorization获取token
        String token=request.getHeader ("authorization");
        //判断token是否为空
        if(StrUtil.isBlank (token)){
            //空，放行
            return true;
        }
        String key=RedisContent.TOKEN_KEY+token;
        //不为空，从redis查询用户是否存在
        String json=stringRedisTemplate.opsForValue ().get (key);
        User user= JSONUtil.toBean (json,User.class);
        if(user==null){
            //不存在，放行
            return true;
        }
        //存在，保存用户到Threadlocal
        UserHolder.saveUser (user);
        //刷新token在redis的有效期
        stringRedisTemplate.expire (key,RedisContent.TOKEN_TTL, TimeUnit.DAYS);
        //放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
