package com.offcn.util;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtil smsUtil;

    @JmsListener(destination = "smsMap")
    public void sendSMS(Map<String,String> map) throws Exception{

        SendSmsResponse response = smsUtil.sendSms(
                map.get("mobile"),
                map.get("template_code"),
                map.get("sign_name"),
                map.get("param")
        );

        System.out.println("Code=" + response.getCode());
        System.out.println("Message=" + response.getMessage());
        System.out.println("RequestId=" + response.getRequestId());
        System.out.println("BizId=" + response.getBizId());

    }

}
