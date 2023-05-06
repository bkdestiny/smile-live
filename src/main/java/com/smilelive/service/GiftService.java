package com.smilelive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smilelive.entity.Gift;
import com.smilelive.entity.GiftRecord;
import com.smilelive.mapper.GiftMapper;
import com.smilelive.utils.Result;

public interface GiftService extends IService<Gift> {
    Result queryByValueAsc();

    Result sendGift(GiftRecord giftRecord);

    Result queryGiftRankingByDesc(Long userId);
}
