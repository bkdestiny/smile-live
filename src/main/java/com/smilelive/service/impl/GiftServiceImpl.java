package com.smilelive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smilelive.entity.Gift;
import com.smilelive.entity.GiftRecord;
import com.smilelive.entity.User;
import com.smilelive.mapper.GiftMapper;
import com.smilelive.mapper.GiftRecordMapper;
import com.smilelive.service.GiftService;
import com.smilelive.service.UserService;
import com.smilelive.utils.Result;
import com.smilelive.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class GiftServiceImpl extends ServiceImpl<GiftMapper, Gift> implements GiftService {
    @Resource
    private GiftRecordMapper giftRecordMapper;
    @Resource
    private UserService userService;
    @Override
    public Result queryByValueAsc() {
        List<Gift> gifts = query ().orderByAsc ("value").list ();
        return Result.ok (gifts);
    }

    @Override
    @Transactional
    public Result sendGift(GiftRecord giftRecord) {
        User currentUser= UserHolder.getUser ();
        try {
            //校验
            Long giverId = giftRecord.getGiver ();
            User giver = userService.getById (giverId);
            if(giver==null){
                throw new Exception ();
            }
            //获取礼物
            Gift gift=getById (giftRecord.getGiftId ());
            if(gift==null){
                throw new Exception ();
            }
            /*判断当前余额是否足够*/
            Integer total=gift.getValue ()*giftRecord.getCount ();
            Integer newWallet=giver.getWallet ()-total.intValue ();
            if(newWallet<0){
                return Result.fail ("余额不足");
            }
            boolean isUpdate=userService.update ().eq ("id",giverId).
                    eq ("wallet",giver.getWallet ()).set ("wallet",newWallet).update ();
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
            return Result.ok (giftRecord);
        }catch (Exception e){
            return Result.fail ("礼物赠送失败");
        }
    }
}
