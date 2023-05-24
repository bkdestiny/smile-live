package com.smilelive.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smilelive.entity.Gift;
import com.smilelive.entity.GiftRecord;
import com.smilelive.entity.User;
import com.smilelive.mapper.GiftMapper;
import com.smilelive.mapper.GiftRecordMapper;
import com.smilelive.service.GiftService;
import com.smilelive.service.UserService;
import com.smilelive.utils.RedisContent;
import com.smilelive.utils.Result;
import com.smilelive.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GiftServiceImpl extends ServiceImpl<GiftMapper, Gift> implements GiftService {
    @Resource
    private GiftRecordMapper giftRecordMapper;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryByValueAsc() {
        List<Gift> gifts = query ().orderByAsc ("value").list ();
        return Result.ok (gifts);
    }
    //赠送礼物
    @Override
    @Transactional
    public Result sendGift(GiftRecord giftRecord) {
        //校验传值是否合法
        //礼物数量
        if(!(giftRecord.getCount ()>=1&&giftRecord.getCount ()<=99)){
            return Result.fail ("请输入合法的礼物数量");
        }
        Long currentUserId= UserHolder.getUser ().getId ();
        try {
            //校验用户
            Long giverId = giftRecord.getGiver ();
            if(currentUserId!=giverId){
                throw new Exception ();
            }
            //获取当前用户数据
            User currentUser = userService.getById (currentUserId);
            //获取礼物
            Gift gift=getById (giftRecord.getGiftId ());
            if(gift==null){
                throw new Exception ();
            }
            /*判断当前余额是否足够*/
            //计算礼物的总价值
            Integer total=gift.getValue ()*giftRecord.getCount ();
            //钱包余额减去礼物总价值
            Integer newWallet=currentUser.getWallet ()-total.intValue ();
            if(newWallet<0){
                //newWallet小于0,返回余额不足信息
                return Result.fail ("余额不足");
            }
            //余额充足,尝试更新数据库
            boolean isUpdate=userService.update ().eq ("id",giverId).
                    eq ("wallet",currentUser.getWallet ()).set ("wallet",newWallet).update ();
            if(!isUpdate){
                //更新失败
                throw new Exception ();
            }
            //更新成功,保存赠送礼物记录
            giftRecord.setGiverName (currentUser.getNickname ());
            giftRecord.setGiftName (gift.getName ());
            giftRecord.setGiftValue (gift.getValue ());
            giftRecord.setGiftImage (gift.getImage ());
            giftRecord.setTime (LocalDateTime.now ());
            int insert = giftRecordMapper.insert (giftRecord);
            if(insert==0){
                throw new SQLException ();
            }
            //TODO 问题 netty-socketIO json解析不了LocalDataTime
            giftRecord.setTime (null);
            //更新礼物榜信息(redis)
            String giftRankingKey= RedisContent.GIFT_RANKING_KEY+giftRecord.getReceiver ();
            Double score = stringRedisTemplate.
                    opsForZSet ().
                    score (giftRankingKey,
                            String.valueOf (currentUser.getId ()));
            //redis zset是否有当前用户的赠送礼物数据
            if(score==null){
                //没有数据,初始化为0
                score=0d;
            }
            //保存新的score到redis
            stringRedisTemplate.
                    opsForZSet ().
                    add (giftRankingKey,
                            String.valueOf (currentUser.getId ()),
                            score.intValue ()+total);
            //返回
            return Result.ok (giftRecord);
        }catch (Exception e){
            e.printStackTrace ();
            return Result.fail ("礼物赠送失败");
        }
    }
    //获取赠送礼物排行榜
    @Override
    public Result queryGiftRankingByDesc(Long userId) {
        //从redis获取排行榜 zset key
        String giftRankingKey=RedisContent.GIFT_RANKING_KEY+userId;
        Set<ZSetOperations.TypedTuple<String>> giftRankings = stringRedisTemplate.
                opsForZSet ().
                reverseRangeWithScores (giftRankingKey, 0, -1);
        //是否为空
        if(giftRankings==null||giftRankings.isEmpty ()){
            //返回空
            return Result.ok ();
        }
        //遍历set获取用户id数组
        List<String> ids=new ArrayList<> ();
        for(ZSetOperations.TypedTuple<String> tuple:giftRankings){
             ids.add (tuple.getValue ());
        }
        String idsStr= StrUtil.join (",",ids);
        //准备要返回前端的用户List,数据库获取用户List,按idStr数组的顺序排序
        List<User> list = userService.query ().in("id",ids).
                last ("ORDER BY FIELD(id," + idsStr + ")").list ();
        int i=0;
        //遍历将赠礼价值放入用户List
        for(ZSetOperations.TypedTuple<String> tuple:giftRankings){
            list.get (i++).setGiftValue (tuple.getScore ().intValue ());
        }
        //返回
        return Result.ok (list);
    }
}
