package com.smilelive.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* 分页配置
* */
@Configuration
public class MyBatisConfig {
    @Bean
    public MybatisPlusInterceptor paginationInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor=new MybatisPlusInterceptor ();
        mybatisPlusInterceptor.addInnerInterceptor (new PaginationInnerInterceptor ());
        return mybatisPlusInterceptor;
    }
}
