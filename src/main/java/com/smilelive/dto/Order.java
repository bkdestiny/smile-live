package com.smilelive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    //订单号
    private String out_trade_no;
    //付款金额
    public Float total_amount;
    //订单名称
    private String subject;
    //超时时间参数
    private String timeout_express="10m";
    //产品编号
    private String product_code="smile-live";

    public Order(String out_trade_no, Float total_amount, String subject) {
        this.out_trade_no = out_trade_no;
        this.total_amount = total_amount;
        this.subject = subject;
    }
}
