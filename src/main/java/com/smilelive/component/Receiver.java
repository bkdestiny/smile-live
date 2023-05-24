package com.smilelive.component;

import cn.hutool.json.JSONUtil;
import com.smilelive.config.RabbitMqConfig;
import com.smilelive.entity.RuneOrder;
import com.smilelive.service.RuneOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.channels.Channel;
import java.time.LocalDateTime;

@Component
@Slf4j
public class Receiver {
    @Resource
    private RuneOrderService runeOrderService;
    @RabbitListener(queues = RabbitMqConfig.RUNE_ORDER_QUEUE)
    public void runeOrderReceiver(Message message){
        log.info ("runeOrder-->{}",new String(message.getBody ()));
        //TODO 处理充值订单
        String runeOrderJson=new String (message.getBody ());
        RuneOrder runeOrder=JSONUtil.toBean (runeOrderJson,RuneOrder.class);
        String out_trade_no= runeOrder.getOut_trade_no ();
        try {
            runeOrderService.payedOrder (out_trade_no);
        }catch (Exception e){

        }
    }
    @RabbitListener(queues = RabbitMqConfig.RUNE_ORDER_DELAYED_QUEUE)
    public void runeOrderDelayedReceiver(Message message){
        log.info ("runeOrderDelayed-->{}",new String(message.getBody ()));
        //处理超时未支付订单
        String runeOrderJson=new String (message.getBody ());
        RuneOrder runeOrder=JSONUtil.toBean (runeOrderJson,RuneOrder.class);
        String out_trade_no= runeOrder.getOut_trade_no ();
        //更新订单状态
        runeOrderService.update ().
                eq ("out_trade_no", out_trade_no).
                eq ("status", 1).
                set ("status", 5).
                set ("update_time", LocalDateTime.now ()).update ();
    }
}
