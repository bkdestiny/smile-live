package com.smilelive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smilelive.entity.RuneOrder;
import com.smilelive.mapper.RuneOrderMapper;
import com.smilelive.utils.Result;

import java.sql.SQLException;

public interface RuneOrderService extends IService<RuneOrder> {
    RuneOrder createOrder(Long userId,Float total_amount,Integer rune,Integer payType);
    void payedOrder(String orderNo) throws InterruptedException;
    void completeOrder(String orderNo) throws SQLException;
}
