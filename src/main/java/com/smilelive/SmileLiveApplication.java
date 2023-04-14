package com.smilelive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smilelive.mapper")
public class SmileLiveApplication {
    public static void main(String[] args) {
        SpringApplication.run (SmileLiveApplication.class);
    }
}