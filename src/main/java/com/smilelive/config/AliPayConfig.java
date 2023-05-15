package com.smilelive.config;

import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
@PropertySource (value = "classpath:application-alipay.properties")
@ConfigurationProperties(prefix = "alipay")
@Configuration
@Data
public class AliPayConfig {
    private String appId;
    private String appPrivateKey;
    private String chatset;
    private String alipayPublicKey;
    private String gatewayUrl;
    private String format;
    private String signType;
    private String notifyUrl;
    private String returnRuneOrderUrl;

}
