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
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GiftServiceImpl extends ServiceImpl<GiftMapper, Gift> implements GiftService {
    @Resource
    private GiftRecordMapper giftRecordMapper;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryByValueAsc() {
        List<Gift> gifts = query ().orderByAsc ("value").list ();
        return Result.ok (gifts);
    }

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
            Integer total=gift.getValue ()*giftRecord.getCount ();
            Integer newWallet=currentUser.getWallet ()-total.intValue ();
            if(newWallet<0){
                return Result.fail ("余额不足");
            }
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
                throw new Exception ();
            }
            //TODO 问题 netty-socketIO json解析不了LocalDataTime
            giftRecord.setTime (null);
            //更新礼物榜信息(redis)
            String giftRankingKey= RedisContent.GIFT_RANKING_KEY+giftRecord.getReceiver ();
            Double score = stringRedisTemplate.opsForZSet ().score (giftRankingKey, String.valueOf (currentUser.getId ()));
            if(score==null){
                score=0d;
            }
            stringRedisTemplate.opsForZSet ().add (giftRankingKey,String.valueOf (currentUser.getId ()),score.intValue ()+total);
            return Result.ok (giftRecord);
        }catch (Exception e){
            e.printStackTrace ();
            return Result.fail ("礼物赠送失败");
        }
    }

    @Override
    public Result queryGiftRankingByDesc(Long userId) {
        String giftRankingKey=RedisContent.GIFT_RANKING_KEY+userId;
        Set<ZSetOperations.TypedTuple<String>> giftRankings = stringRedisTemplate.opsForZSet ().reverseRangeWithScores (giftRankingKey, 0, -1);
        if(giftRankings==null||giftRankings.isEmpty ()){
            return Result.ok ();
        }
        List<String> ids=new ArrayList<> ();
        for(ZSetOperations.TypedTuple<String> tuple:giftRankings){
             ids.add (tuple.getValue ());
        }
        String idsStr= StrUtil.join (",",ids);
        List<User> list = userService.query ().in("id",ids).last ("ORDER BY FIELD(id," + idsStr + ")").list ();
        int i=0;
        for(ZSetOperations.TypedTuple<String> tuple:giftRankings){
            list.get (i++).setGiftValue (tuple.getScore ().intValue ());
        }
        return Result.ok (list);
    }
}
