package com.youlexuan.search.service.impl;

import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class ItemDeleteMessageListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage obj = (ObjectMessage)message;
            Long[] ids = (Long[])obj.getObject();
            itemSearchService.deleteItems(ids);
            System.out.println("solr delete ... ");
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
