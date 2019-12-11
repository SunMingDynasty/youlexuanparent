package com.youlexuan.solrutil;

import com.alibaba.fastjson.JSON;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importData(){

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> list = itemMapper.selectByExample(example);

        for (TbItem item : list){
            //对item表中的spec 进行json格式化
            Map<String,String> mapSpec = JSON.parseObject(item.getSpec(),Map.class);
            item.setSpecMap(mapSpec);
            System.out.println(item.getTitle());
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();

    }

    public static void main(String[] args) {
       ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
       SolrUtil solrUtil = (SolrUtil) ac.getBean("solrUtil");
       solrUtil.importData();
    }

}
