package com.youlexuan.search.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemMessageListener implements MessageListener {

    @Autowired
    private ItemSearchService searchService;

    @Override
    public void onMessage(Message message) {
        try {
            //监听器从mq中取 list
            TextMessage text = (TextMessage) message;
            String solrList = text.getText();
            //JSON格式转换
            List<TbItem> list = JSON.parseArray(solrList, TbItem.class);
            for (TbItem item : list){
                Map map = JSON.parseObject(item.getSpec());
                item.setSpecMap(map);
            }
            //将list数据导入到solr中
            searchService.importItems(list);
            System.out.println("导入索引库...");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}