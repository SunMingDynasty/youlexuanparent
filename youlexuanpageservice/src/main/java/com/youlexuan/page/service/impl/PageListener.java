package com.youlexuan.page.service.impl;

import com.youlexuan.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class PageListener implements MessageListener {

    @Autowired
    PageService pageService;

    @Override
    public void onMessage(Message message) {

        try {
            TextMessage text = (TextMessage)message;
            String goodsId = text.getText();
            System.out.println("消费者获取ids"+goodsId);

            pageService.genItemById(Long.parseLong(goodsId));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
