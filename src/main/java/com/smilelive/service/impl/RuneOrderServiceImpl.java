package com.smilelive.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smilelive.config.RabbitMqConfig;
import com.smilelive.entity.RuneOrder;
import com.smilelive.entity.User;
import com.smilelive.mapper.RuneOrderMapper;
import com.smilelive.service.RuneOrderService;
import com.smilelive.service.UserService;
import com.smilelive.utils.RedisContent;
import com.smilelive.utils.RedisIdWorker;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class RuneOrderServiceImpl extends ServiceImpl<RuneOrderMapper, RuneOrder> implements RuneOrderService {
    @Resource
    private UserServiceImpl userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AmqpTemplate amqpTemplate;
    @Resource
    private RedissonClient redissonClient;
    //创建订单,status:未支付
    @Override
    public RuneOrder createOrder(Long userId, Float total_amount, Integer rune,Integer payType) {
        RuneOrder runeOrder = new RuneOrder ();
        //订单创建时间
        runeOrder.setCreateTime (LocalDateTime.now ());
        //生成订单号
        String time= new SimpleDateFormat ("yyyyMMddHHmmss").format (new Date ());
        String uuid= UUID.randomUUID ().toString ().replace ("-","").toUpperCase ();
        String orderNo=time+uuid;
        //1.订单号
        runeOrder.setOut_trade_no (orderNo);
        //2.支付方式
        runeOrder.setPayType (payType);
        //3.金额
        runeOrder.setTotal_amount (total_amount);
        //4.支付状态  1 未支付
        runeOrder.setStatus (1);
        //5.充值卢恩数量
        runeOrder.setRune (rune);
        //6.订单名称
        runeOrder.setSubject ("V我"+rune+"卢恩 ");
        //7.用户Id
        runeOrder.setUserId (userId);
        boolean save = save (runeOrder);
        if(!save){
            return null;
        }
        //发送消息给延迟队列 实现超时未支付 过期时间:15分钟
        amqpTemplate.convertAndSend (RabbitMqConfig.RUNE_ORDER_DELAYED_EXCHANGE,
                RabbitMqConfig.RUNE_ORDER_DELAYED_ROUTING_KEY,
                JSONUtil.toJsonStr (runeOrder).getBytes(StandardCharsets.UTF_8),
                msg->{
            msg.getMessageProperties ().setDelay (15*60*1000);
            return msg;
        });
        return runeOrder;
    }
    //支付订单后完成的业务
    @Override
    public void payedOrder(String orderNo){
        String lockKey= RedisContent.LOCK_RUNE_KEY+orderNo;
        RLock lock = redissonClient.getLock (lockKey);
        try {
            /**
            * 尝试获取锁
             * 1.尝试获取锁的时间
             * 2.锁的过期时间，防止死锁
             * 3.时间单位 秒
            * */
            boolean isLock = lock.tryLock (2, RedisContent.LOCK_RUNE_TTL, TimeUnit.SECONDS);
            if (!isLock) {
                //获取锁失败，更新订单信息为已支付 2
                throw new Exception ();
            }
            //调用代理对象完成业务
            RuneOrderService proxy = (RuneOrderService) AopContext.currentProxy ();
            proxy.completeOrder (orderNo);
        }catch (Exception e){
            //报错，更新订单信息为已支付
            update ().eq ("out_trade_no",orderNo).
                    set ("status",2)
                    .set ("pay_time",LocalDateTime.now ()).update ();
        }finally {
            //释放锁
            lock.unlock ();
        }
    }
    //事务 更新用户钱包和更新订单业务
    @Transactional
    public void completeOrder(String orderNo) throws SQLException {
        //获取订单信息
        RuneOrder order = query ().eq ("out_trade_no", orderNo).one ();
        if(order.getStatus ()>=3){
            //订单状态为已核销 或 已过期
            return;
        }
        //获取充值卢恩额度
        int rune=order.getRune ();
        //获取用户ID
        long userId=order.getUserId ();
        //获取用户 卢恩余额
        Integer oldWallet = userService.getBaseMapper ().getWalletById (userId);
        //准备用户 新卢恩余额
        Integer newWallet = oldWallet+rune;
        //更新用户 卢恩余额
        boolean walletUpdate = userService.update ().eq ("id", userId).eq ("wallet", oldWallet).set ("wallet", newWallet).update ();
        if(!walletUpdate){
            //更新失败，抛出异常
            throw new SQLException ();
        }
        LocalDateTime time=LocalDateTime.now ();
        //更新订单信息
        boolean orderUpdate =
                update ().
                eq ("out_trade_no",orderNo).
                //3 已核销
                set ("status",3).
                set("pay_time",time).
                set ("use_time",time).update ();
        if(!orderUpdate){
            //订单信息更新失败，抛出异常
            throw new SQLException ();
        }
    }


}
