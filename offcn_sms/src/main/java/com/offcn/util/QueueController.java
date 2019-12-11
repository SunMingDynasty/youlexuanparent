package com.offcn.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
//JMS 提供方
@RestController
public class QueueController {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    //jms提供方
    @RequestMapping("/send")
    public void send(String text){
        jmsMessagingTemplate.convertAndSend("offcn",text);
    }

    @RequestMapping("/sendMap")
    public void sendMap(){
        Map<String,String> map = new HashMap<String,String>();
        map.put("mobile","18810533579");
        map.put("code","zy32");
        jmsMessagingTemplate.convertAndSend("mess",map);
    }

    @RequestMapping("/sendSMS")
    public void sendSMS(){

        Map<String,String> map = new HashMap<String,String>();
        map.put("mobile","18810533579");
        map.put("template_code","SMS_164666777");
        map.put("sign_name","优乐码");
        map.put("param","{\"code\":\"102931\"}");

        jmsMessagingTemplate.convertAndSend("smsMap",map);
    }

}
