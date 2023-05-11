package com.smilelive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smilelive.entity.User;
import com.smilelive.utils.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface UserService extends IService<User> {
    Result loginByPassword(HttpServletRequest req,User user);

    Result currentUser(HttpServletRequest req);

    Result updateUserInfo(User userInfo);

    Result saveAvatar(MultipartFile file, Long id);

    Result logout(HttpServletRequest req);

    void loginCaptcha(HttpServletRequest req,HttpServletResponse resp) throws Exception;
}
