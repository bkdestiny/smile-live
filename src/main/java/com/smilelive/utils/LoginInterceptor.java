package com.smilelive.utils;

import com.smilelive.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info ("request-->{}",request.getSession ().getId ());
        //查看ThreadLocal中是否有用户
        User user =UserHolder.getUser ();
        if(user==null){
            //没有用户,拦截,响应失败 返回301状态码
            response.setStatus (301);
            return false;
        }
        //放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser ();
    }
}
