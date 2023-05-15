package com.smilelive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smilelive.dto.Order;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName(value = "rune_order")
public class RuneOrder extends Order {
    /**
    * 数据库id
    * */
    private Long id;
    /**
     * 订单号
     * */
    private String out_trade_no;
    /**
     * 付款金额
     * */
    public Float total_amount;
    /**
     * 超时时间参数
    * */
    @TableField(exist = false)
    private String timeout_express="10m";
    /**
    * 产品编号
    * */
    @TableField(exist = false)
    private String product_code="smile-live";
    /**
     * 充值卢恩
    * */
    public Integer rune;
    /**
    *   订单名称
    */
    private String subject;
    /**
    * 下单用户Id
    * */
    @TableField(value = "user_id")
    private Long userId;
    /**
     * 支付方式 1：余额支付；2：支付宝；3：微信
     */
    private Integer payType;

    /**
     * 订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款
     */
    private Integer status;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 核销时间
     */
    private LocalDateTime useTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
