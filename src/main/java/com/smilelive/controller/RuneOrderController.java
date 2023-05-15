package com.smilelive.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.smilelive.config.AliPayConfig;
import com.smilelive.entity.RuneOrder;
import com.smilelive.service.RuneOrderService;
import com.smilelive.utils.Alipay;
import com.smilelive.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("rune")
public class RuneOrderController {
    @Resource
    private RuneOrderService runeOrderService;
    @Resource
    private AliPayConfig aliPayConfig;
    @Resource
    private Alipay alipay;
    @RequestMapping("pay")
    public String pay(HttpSession session,@RequestParam Long userId,@RequestParam Float money) throws AlipayApiException {
        Integer rune=money.intValue ()*10;
        RuneOrder order = runeOrderService.createOrder (userId, money, rune, 2);
        if(order==null){
            log.info("error--> order=null");
            return null;
        }
        String s = alipay.payRuneOrder (order);
        log.info("s-->{}",s);
        return s;
    }

    /*回调*/
    @RequestMapping("returnRuneOrderUrl")
    public Result returnRuneOrderUrl(HttpServletRequest req, HttpSession session) throws UnsupportedEncodingException, AlipayApiException {
        log.info("runeOrder -->{}",session);
        //获取支付宝GET过来反馈信息
        Map<String,String> params=new HashMap<String,String> ();
        Map<String,String[]> requestParams=req.getParameterMap ();
        for(Iterator<String> iter = requestParams.keySet ().iterator (); iter.hasNext ();){
            String name=(String)iter.next ();
            String[] values=(String[])requestParams.get (name);
            String valueStr="";
            for(int i=0;i<values.length;i++){
                valueStr=(i==values.length-1)?valueStr+values[i]:valueStr+values[i]+",";
            }
            //乱码解决,这段代码在出现乱码时使用
            valueStr=new String(valueStr.getBytes ("ISO-8859-1"),"utf-8");
            params.put(name,valueStr);
        }
        System.out.println (params);
        //验证签名(支付宝公钥)
        boolean signVerified= AlipaySignature.rsaCheckV1 (params,aliPayConfig.getAlipayPublicKey (),aliPayConfig.getChatset (),aliPayConfig.getSignType ());
        if(signVerified){
            //商户订单号
            String orderNo=new String (req.getParameter ("order_no").
                    getBytes ("ISO-8859-1"),"UTF-8");
            //支付宝交易流水号
            String tradeNo=new String (req.getParameter ("out_trade_no").
                    getBytes ("ISO-8859-1"),"UTF-8");
            //付款金额
            float totalAmount=Float.parseFloat (new String (req.getParameter ("total_amount").
                    getBytes ("ISO-8859-1"),"UTF-8"));
            System.out.println ("商品订单号="+orderNo);
            System.out.println ("支付宝交易号="+tradeNo);
            System.out.println ("付款金额="+totalAmount);
            /*
             * TODO 这里编写自己的业务代码(对数据库的操作)
             * */
            log.info("支付成功");

            //跳转到提示页面(成功或者失败的提示页面)
            //成功
            return Result.ok ();
        }else{
            //失败
            log.info("支付失败");
            return Result.fail ("支付失败");
        }
    }

    @PostMapping("notify")
    public String handleAlipayed(HttpServletRequest req) throws AlipayApiException, InterruptedException {
        Map<String, String[]> map = req.getParameterMap();
        log.info("map1 -->{}",map);
        //阿里验签方法
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = req.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                aliPayConfig.getAlipayPublicKey (),
        aliPayConfig.getChatset (),  aliPayConfig.getSignType ());
        if (signVerified) {
            System.out.println("签名验证成功...");
            String out_trade_no=params.get ("out_trade_no");
            String result = runeOrderService.payedOrder (out_trade_no);
            return result;
        } else {
            System.out.println("签名验证失败...");
            return "error";
        }
    }

}
