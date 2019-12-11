package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.entity.Result;
import com.youlexuan.order.service.OrderService;
import com.youlexuan.pay.service.AlipayService;
import com.youlexuan.pojo.TbPayLog;
import com.youlexuan.util.IdWorker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PayController {

    @Reference(timeout = 6000)
    private AlipayService alipayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前登录人 查看当前登录人是否有未结算的定单
        //如果有则生成二维码进行结算 如果没有 不生成二维码 返回空map
        //1、获取当前登录人
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //从redis中查询未结算的订单日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if(payLog!=null){
            return alipayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //定义计数器
        int x = 0;
        while(true){

            Map<String,String> map = new HashMap<String,String>();
            try {
                //调用查询支付状态的方法
                map = alipayService.queryPayStatus(out_trade_no);
                x++;
                //睡3秒
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                System.out.println("查询服务出错...");
            }

            if(map == null){
                return new Result(false,"支付结果出错");
            }

            if(map.get("tradeStatus")!=null && "TRADE_SUCCESS".equals(map.get("tradeStatus"))){
                //当支付成功时，修改日志信息与订单信息 同时清空redis
                orderService.updateOrderTradeStatus(out_trade_no,map.get("transaction_id"));

                return new Result(true,"支付成功");
            }

            if(map.get("tradeStatus")!=null && "TRADE_CLOSED".equals(map.get("tradeStatus"))){
                return new Result(true,"未付款交易超时关闭，或支付完成后全额退款");
            }

            if(map.get("tradeStatus")!=null && "TRADE_FINISHED".equals(map.get("tradeStatus"))){
                return new Result(true,"未付款交易超时关闭，或支付完成后全额退款");
            }

            if(x >= 300){
                return new Result(true,"二维码超时");
            }
        }

    }

}
