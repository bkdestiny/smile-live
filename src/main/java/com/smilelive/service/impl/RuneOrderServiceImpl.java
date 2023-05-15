package com.smilelive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private RedissonClient redissonClient;
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
        return runeOrder;
    }

    @Override
    public String payedOrder(String orderNo){
        String lockKey= RedisContent.LOCK_RUNE_KEY+orderNo;
        RLock lock = redissonClient.getLock (lockKey);
        try {
            //尝试获取锁
            boolean isLock = lock.tryLock (2, RedisContent.LOCK_RUNE_TTL, TimeUnit.SECONDS);
            if (!isLock) {
                //获取锁失败
                return "error";
            }
            //调用代理对象完成业务
            RuneOrderService proxy = (RuneOrderService) AopContext.currentProxy ();
            proxy.completeOrder (orderNo);
            return "success";
        }catch (Exception e){
            return "error";
        }finally {
            //释放锁
            lock.unlock ();
        }
    }
    @Transactional
    public void completeOrder(String orderNo) throws SQLException {
        //获取订单信息
        RuneOrder order = query ().eq ("out_trade_no", orderNo).one ();
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
        //设置 3 已核销
        order.setStatus (3);
        //设置核销时间
        order.setUseTime (LocalDateTime.now ());
        //更新订单信息
        boolean orderUpdate = update ().update (order);
        if(!orderUpdate){
            throw new SQLException ();
        }
    }


}
