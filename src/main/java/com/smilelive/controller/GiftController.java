package com.smilelive.controller;

import com.smilelive.entity.GiftRecord;
import com.smilelive.service.GiftService;
import com.smilelive.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("gift")
public class GiftController {
    @Resource
    private GiftService giftService;
    @GetMapping("queryByValueAsc")
    public Result queryByValueAsc(){
        return giftService.queryByValueAsc ();
    }
    @PostMapping("sendGift")
    public Result sendGift(@RequestBody GiftRecord giftRecord){
        return giftService.sendGift(giftRecord);
    }
}
