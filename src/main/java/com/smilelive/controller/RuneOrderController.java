package com.smilelive.controller;

import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.smilelive.config.AliPayConfig;
import com.smilelive.config.RabbitMqConfig;
import com.smilelive.entity.RuneOrder;
import com.smilelive.service.RuneOrderService;
import com.smilelive.utils.Alipay;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("rune")
public class RuneOrderController {
    @Resource
    private RuneOrderService runeOrderService;
    @Resource
    private AliPayConfig aliPayConfig;
    @Resource
    private AmqpTemplate amqpTemplate;
    @Resource
    private Alipay alipay;
    @RequestMapping("pay")
    public String pay(HttpSession session,@RequestParam Long userId,@RequestParam Float money) throws AlipayApiException {
        //消息队列 创建订单
        Integer rune=money.intValue ()*10;
        RuneOrder order = runeOrderService.createOrder (userId, money, rune, 2);
        if(order==null){
            log.info("error--> order=null");
            return null;
        }
        String s = alipay.payRuneOrder (order);
        log.info("s-->{}",s);
        return s;
    }

    @PostMapping("notify")
    public String handleAlipayed(HttpServletRequest req) throws AlipayApiException, InterruptedException {
        Map<String, String[]> map = req.getParameterMap();
        log.info("map1 -->{}",map);
        //阿里验签方法
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = req.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                aliPayConfig.getAlipayPublicKey (),
        aliPayConfig.getChatset (),  aliPayConfig.getSignType ());
        if (signVerified) {
            System.out.println("签名验证成功...");
            /** 验证成功 完成充值订单*/
            String out_trade_no=params.get ("out_trade_no");
           // String result = runeOrderService.payedOrder (out_trade_no);
            RuneOrder order = new RuneOrder (out_trade_no);
            amqpTemplate.convertAndSend (
                    RabbitMqConfig.RUNE_ORDER_EXCHANGE,
                    RabbitMqConfig.RUNE_ORDER_ROUTING_KEY,
                    JSONUtil.toJsonStr (order).getBytes(StandardCharsets.UTF_8));
            return "success";
        } else {
            System.out.println("签名验证失败...");
            return "error";
        }
    }

}
