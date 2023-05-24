package com.smilelive.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smilelive.entity.User;
import com.smilelive.mapper.UserMapper;
import com.smilelive.service.UserService;
import com.smilelive.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MyFileUtil myFileUtil;
    @Override
    public Result loginByPassword(HttpServletRequest req,User loginForm) {
        String sessionId=req.getSession ().getId ();
        String captchaKey=RedisContent.CAPTCHA_KEY+sessionId;
        String captcha = stringRedisTemplate.opsForValue ().get (captchaKey);
        if(!captcha.equals (loginForm.getCaptcha ())){
            return Result.fail ("验证码错误");
        }
        User user=null;
        /*
        * 判断是用户名还是手机号登录
        * */
        try {
            if (loginForm.getUsername () != null) {
                user = query ()
                        .eq ("username", loginForm.getUsername ())
                        .eq ("password", loginForm.getPassword ())
                        .one ();
            } else if (loginForm.getPhone () != null) {
                user = query ()
                        .eq ("phone", loginForm.getPhone ())
                        .eq ("password", loginForm.getPassword ())
                        .one ();
            }
            //用户是否存在
            if (user == null) {
                //不存在，返回错误结果
                return Result.fail ("用户名或密码错误");
            }
            //用户存在，UUID生产token
            String token = UUID.fastUUID ().toString (true);
            //保存到redis
            user.setPassword (null);
            String userJson = JSONUtil.toJsonStr (user);
            String tokenKey = RedisContent.TOKEN_KEY + token;
            //设置token有效期为7天
            stringRedisTemplate.opsForValue ().set (tokenKey, userJson,RedisContent.TOKEN_TTL,TimeUnit.DAYS);
            return Result.ok (token);
        }catch (Exception e){
            return Result.fail ("用户名或密码错误");
        }
    }

    @Override
    public Result currentUser(HttpServletRequest req) {
        /*String token=req.getHeader ("authorization");
        String tokenKey=RedisContent.TOKEN_KEY+token;
        String userJson = stringRedisTemplate.opsForValue ().get (tokenKey);
        if(userJson==null||StrUtil.isBlank (userJson)){
            return Result.fail ("用户信息失效");
        }
        User user=JSONUtil.toBean (userJson,User.class);*/
        Long userId=UserHolder.getUser ().getId ();
        User currentUser = getById (userId);
        if(currentUser==null){
            return Result.fail ("用户不存在");
        }
        return Result.ok (currentUser);
    }
    @Override
    public Result updateUserInfo(User userInfo) {
        log.info ("userInfo-->{}",userInfo);
        User user =UserHolder.getUser ();
        user.setId (user.getId ());
        user.setNickname (userInfo.getNickname ());
        user.setSex (userInfo.getSex ());
        user.setBirthday (userInfo.getBirthday ());
        log.info("user-->",user);
        boolean b = updateById (user);
        if(!b){
            return Result.fail ("保存失败");
        }
        return Result.ok ();
    }

    @Override
    public Result saveAvatar(MultipartFile file, Long id) {
        //保存头像文件
            String path = myFileUtil.saveImage (file, MyFileUtil.AVATAR_PATH);
        try {
            //返回保存的文件名
            if (path == null || StrUtil.isBlank (path)) {
                //保存失败
                throw new Exception ();
            }
            //保存成功,保存文件名到数据库
            Long userId = UserHolder.getUser ().getId ();
            String oldAvatar = query ().eq ("id", userId).one ().getAvatar ();
            boolean update = update ().eq ("id", userId).set ("avatar", path).update ();
            if (!update) {
                    throw new Exception ();
            }
            //删除旧头像文件
            myFileUtil.delImage (oldAvatar);
            //删除直播间redis缓存
            String liveRoomKey=RedisContent.LIVEROOM_KEY+userId;
            stringRedisTemplate.delete (liveRoomKey);
            return Result.ok ();
        }catch (Exception e){
            //异常，删除保存的新头像文件
            myFileUtil.delImage (path);
            return Result.fail ("保存头像失败");
        }
    }

    @Override
    public Result logout(HttpServletRequest req) {
        String token = req.getHeader ("authorization");
        String tokenKey=RedisContent.TOKEN_KEY+token;
        Boolean delete = stringRedisTemplate.delete (tokenKey);
        return Result.ok ();
    }
    /*登录验证码*/
    @Override
    public void loginCaptcha(HttpServletRequest req,HttpServletResponse resp) throws Exception{
            /*1.hutool生产验证码*/
            GifCaptcha gifCaptcha = CaptchaUtil.createGifCaptcha (300, 100, 6);
            //2.sessionId作为key保存到redis 5分钟
            String captchaKey = RedisContent.CAPTCHA_KEY + req.getSession ().getId ();
            stringRedisTemplate.opsForValue ().set (captchaKey,
                    gifCaptcha.getCode (),
                    RedisContent.CAPTCHA_TTL,TimeUnit.MINUTES);
            //3
            gifCaptcha.write (resp.getOutputStream ());
    }
}
