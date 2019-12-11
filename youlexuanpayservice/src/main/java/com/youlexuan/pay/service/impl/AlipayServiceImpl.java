package com.youlexuan.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.youlexuan.pay.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayClient alipayClient;




    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        //返回结果对象
        Map<String,String> map = new HashMap<String,String>();

        //1、创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //2、封装请求参数
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"total_amount\":\""+total_fee+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数

        //3、发送请求
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            //4、获取响应状态吗与响应结果
            String code = response.getCode();
            System.out.println("响应状态码 code:"+code);
            //5、获取响应结果
            String body = response.getBody();
            System.out.println("响应结果 body:"+body);

            //6、如果响应状态码为10000 表示成功
            if("10000".equals(code)){
                //获取二维码信息
                map.put("qrcode", response.getQrCode());
                //订单编号
                map.put("out_trade_no", response.getOutTradeNo());
                //消费总金额
                map.put("total_fee",total_fee);

                System.out.println("qrcode:"+response.getQrCode());
                System.out.println("out_trade_no:"+response.getOutTradeNo());
                System.out.println("total_fee:"+total_fee);
            }else{
                System.out.println("调用接口失败");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //结果集对象
        Map<String,String> map = new HashMap<String,String>();
        //1、阿里查询状态对象
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        //2、封装参数 out_trade_no 订单编号 trade_no 交易流水号
        queryRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}");
        //3、执行查询
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(queryRequest);

            //4、判断结果
            if("10000".equals(response.getCode())){
                //获取结果
                map.put("tradeStatus",response.getTradeStatus());
                //交易流水号码
                map.put("transaction_id",response.getTradeNo());

                System.out.println("tradeStatus:"+response.getTradeStatus());
                System.out.println("transaction_id:"+response.getTradeNo());
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //封装结果
        return map;
    }

    @Override
    public Map close(String out_trade_no) {
        //结果集对象
        Map<String,String> map = new HashMap<String,String>();
        //1、阿里查询状态对象
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        //2、封装参数 out_trade_no 订单编号 trade_no 交易流水号
        queryRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}");
        //3、执行查询
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(queryRequest);

            //4、判断结果
            if("10000".equals(response.getCode())){
                //返回值
                map.put("code",response.getCode());
                //交易流水号码
                map.put("out_trade_no",out_trade_no);

                System.out.println("tradeStatus:"+response.getTradeStatus());
                System.out.println("transaction_id:"+response.getTradeNo());

            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //封装结果
        return map;
    }
}
