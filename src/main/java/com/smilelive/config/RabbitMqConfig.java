package com.smilelive.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    //卢恩订单 直接交换机
    public static final String RUNE_ORDER_EXCHANGE="rune_order_exchange";
    //卢恩订单 队列
    public static final String RUNE_ORDER_QUEUE="rune_order_queue";
    //卢恩订单 routingkey
    public static final String RUNE_ORDER_ROUTING_KEY="rune_order_routingkey";
    //卢恩订单 延迟交换机
    public static final String RUNE_ORDER_DELAYED_EXCHANGE="rune_order_delayed_exchange";
    //卢恩订单 延迟队列
    public static final String RUNE_ORDER_DELAYED_QUEUE="rune_order_delayed_queue";
    //卢恩订单 延迟队列 routingKey
    public static final String RUNE_ORDER_DELAYED_ROUTING_KEY="rune_order_delayed_routingkey";

    //声明交换机
    @Bean
    public DirectExchange runeOrderExchange(){
        return new DirectExchange (RUNE_ORDER_EXCHANGE);
    }
    @Bean
    public CustomExchange runeOrderDelayedExchange(){
        Map<String,Object> arguments=new HashMap<> ();
        arguments.put ("x-delayed-type","direct");
        /*
         * 自定义交换机
         * 1.交换机的名称
         * 2.交换机的类型
         * 3.是否需要持久化
         * 4.是否需要自动删除
         * 5.其他参数
         * */
        return new CustomExchange (RUNE_ORDER_DELAYED_EXCHANGE,"x-delayed-message",true,false,arguments);
    }

    //声明队列
    @Bean
    public Queue runeOrderQueue(){
        return new Queue (RUNE_ORDER_QUEUE);
    }
    @Bean Queue runeOrderDelayedQueue(){
        return new Queue (RUNE_ORDER_DELAYED_QUEUE);
    }

    //声明绑定关系
    @Bean
    public Binding runeOrderBinding(@Qualifier("runeOrderQueue") Queue queue,
                                    @Qualifier("runeOrderExchange") DirectExchange exchange){
        return BindingBuilder.bind (queue).to (exchange).with(RUNE_ORDER_ROUTING_KEY);
    }
    @Bean
    public Binding runeOrderDelayedBinding(@Qualifier("runeOrderDelayedQueue") Queue queue,
                                           @Qualifier("runeOrderDelayedExchange") CustomExchange customExchange){
        return BindingBuilder.bind (queue).to (customExchange).with (RUNE_ORDER_DELAYED_ROUTING_KEY).noargs ();
    }

}
