package com.smilelive.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        //配置类
        Config config=new Config ();
        //添加redis地址,这里添加了单点的地址,
        // 也可以使用config.useClusterServers()添加集群地址
        config.useSingleServer ().setAddress ("redis://192.168.88.130:6379").setPassword ("123456");
        //创建客户端
        return Redisson.create(config);
    }
}
