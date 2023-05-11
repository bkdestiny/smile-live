package com.smilelive.controller;

import com.smilelive.entity.User;
import com.smilelive.service.UserService;
import com.smilelive.utils.MyFileUtil;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.File;
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private MyFileUtil myFileUtil;
    @PostMapping("loginByPassword")
    public Result loginByPassword(HttpServletRequest req,@RequestBody User loginForm){
        return userService.loginByPassword (req,loginForm);
    }
    @GetMapping("currentUser")
    public Result currentUser(HttpServletRequest req){
        return userService.currentUser(req);
    }
    @PostMapping("updateUserInfo")
    public Result updateUserInfo(@RequestBody User userInfo){
        return userService.updateUserInfo(userInfo);
    }
    @PostMapping("saveAvatar")
    public Result saveAvatar(@RequestParam("file") MultipartFile file,@RequestParam Long id){
        return userService.saveAvatar(file,id);
    }
    @PostMapping("logout")
    public Result logout(HttpServletRequest req){
        return userService.logout(req);
    }
    @GetMapping("loginCaptcha")
    public void loginCaptcha(HttpServletRequest req,HttpServletResponse resp) throws Exception {
        log.info("sessionId-->{}",req.getSession ().getId ());
        userService.loginCaptcha(req,resp);
    }
}
