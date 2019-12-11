package com.youlexuan.page.service.impl;

import com.youlexuan.mapper.TbGoodsDescMapper;
import com.youlexuan.mapper.TbGoodsMapper;
import com.youlexuan.mapper.TbItemCatMapper;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.page.service.PageService;
import com.youlexuan.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class PageServiceImpl implements PageService {

    public String pathDir = "f:/html/";

    @Autowired
    public FreeMarkerConfig freemarkerConfig;

    @Autowired
    public TbGoodsMapper goodsMapper;

    @Autowired
    public TbGoodsDescMapper goodsDescMapper;

    @Autowired
    public TbItemCatMapper itemCatMapper;

    @Autowired
    public TbItemMapper itemMapper;


    public boolean deleteItemById(Long[] ids){
        for (Long goodsId:ids) {
            File file = new File(pathDir+goodsId+".html");
            file.delete();
        }
        return true;
    }

    @Override
    public boolean genItemById(Long id) {
        try {
            //1、取配置信息对象
            Configuration configuration = freemarkerConfig.getConfiguration();
            //2、取模板
            Template template = configuration.getTemplate("item.ftl");
            //3、取数据
            //创建存储数据的对象
            Map<String,Object> map = new HashMap<String,Object>();
            //3.1 查询商品表
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            map.put("goods",goods);
            //3.2 查询商品描述表
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
            map.put("goodsDesc",goodsDesc);
            //3.3 查询商品的规格表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            criteria.andStatusEqualTo("1");
            criteria.andIsDefaultEqualTo("1");

            List<TbItem> list = itemMapper.selectByExample(example);

            map.put("itemList",list);

            //查询面包屑
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
            TbItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
            TbItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
            map.put("itemCat1",itemCat1.getName());
            map.put("itemCat2",itemCat2.getName());
            map.put("itemCat3",itemCat3.getName());


            //4、输出到模板页面中
            Writer writer = new FileWriter(pathDir+id+".html");
            //5、生成
            template.process(map,writer);
            //6、关流
            writer.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
