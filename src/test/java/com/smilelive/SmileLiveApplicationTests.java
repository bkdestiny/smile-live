package com.smilelive;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class SmileLiveApplicationTests {

    @Test
    void contextLoads() {
        FileUtil.mkdir ("abc");
        FileUtil.touch ("abc/test.txt");
    }

}
