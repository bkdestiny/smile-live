package com.smilelive.controller;

import com.smilelive.utils.MyFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
@Slf4j
@RestController
@RequestMapping("file")
public class FileController {
    @Resource
    private MyFileUtil myFileUtil;
    @GetMapping("image")
    public void getAvatar(HttpServletResponse resp, @RequestParam String path) {
        log.info("path-->{}",path);
        try{
            byte[] b= myFileUtil.getImage (path);
            if(b==null){
                return;
            }
            ServletOutputStream out = resp.getOutputStream ();
            out.write (b);
            out.flush ();
            out.close ();
        }catch (Exception e){
            e.printStackTrace ();
        }
    }
}
