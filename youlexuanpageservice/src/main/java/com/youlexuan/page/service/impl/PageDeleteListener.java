package com.youlexuan.page.service.impl;

import com.youlexuan.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class PageDeleteListener implements MessageListener {

    @Autowired
    private PageService pageService;

    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage obj = (ObjectMessage)message;
            Long[] ids = (Long[])obj.getObject();

            pageService.deleteItemById(ids);

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
