package com.smilelive.utils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.smilelive.config.AliPayConfig;
import com.smilelive.dto.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Alipay {
    @Autowired
    private AliPayConfig aliPayConfig;

    public String payRuneOrder(Order order) throws AlipayApiException {
        //设置初始化的AlipayClient
        AlipayClient alipayClient=new DefaultAlipayClient (
                aliPayConfig.getGatewayUrl (),
                aliPayConfig.getAppId (),
                aliPayConfig.getAppPrivateKey (),
                aliPayConfig.getFormat (),
                aliPayConfig.getChatset (),
                aliPayConfig.getAlipayPublicKey (),
                aliPayConfig.getSignType ());
        //设置请求参数
        AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest ();
        alipayRequest.setReturnUrl (aliPayConfig.getReturnRuneOrderUrl ());
        alipayRequest.setNotifyUrl (aliPayConfig.getNotifyUrl ());
        //商品描述(可空)
        String body="";
        alipayRequest.setBizContent ("{\"out_trade_no\":\"" + order.getOut_trade_no () + "\","
                + "\"total_amount\":\"" + order.getTotal_amount () + "\","
                + "\"subject\":\"" + order.getSubject () + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求
        String result=alipayClient.pageExecute (alipayRequest).getBody ();
        return result;
    }
}
