package com.youlexuan.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 普通查询
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> searchMap(Map searchMap) {

        //1、创建查询条件
        Query query = new SimpleQuery("*:*");
        //2、拼接条件字段
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //3、查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query,TbItem.class);
        //4、封装返回数据
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("total",page.getTotalElements());
        map.put("rows",page.getContent());

        return map;
    }

    /**
     * 高亮显示方法
     * @param searchMap
     * @return
     */
    public Map<String,Object> searchList(Map searchMap){

        //多条件查询的格式处理
        String str = (String)searchMap.get("keywords");
        searchMap.put("keywords",str.replace(" ",""));
        //多条件查询的格式处理

        HighlightQuery query = new SimpleHighlightQuery();

        //一、基本高亮查询
        //1、创建高亮查询对象
        //2、创建高亮元素对象
        HighlightOptions option = new HighlightOptions().addField("item_title");
        option.setSimplePrefix("<em style='color:red'>");
        option.setSimplePostfix("</em>");
        //3、将高亮元素放入到高亮对象中
        query.setHighlightOptions(option);
        //4、将查询条件 keywords 放入到 query 中
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //一、基本高亮查询

        //二、分类过滤查询
        if(!"".equals(searchMap.get("category"))){
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //二、分类过滤查询
        //三、品牌过滤查询
        if(!"".equals(searchMap.get("brand"))){
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //三、品牌过滤查询
        //四、规格过滤查询
        if(searchMap.get("spec") != null){
            Map<String,String> map = (Map)searchMap.get("spec");
            for(Map.Entry<String,String> entry : map.entrySet()){
                Criteria criteria1 = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }
        //四、规格过滤查询
        //五、价格过滤查询
        if(!"".equals(searchMap.get("price"))){
            String price  = (String)searchMap.get("price");
            System.out.println("price:"+price);
            String[] pri = price.split("-");

            if(!"0".equals(pri[0])){
                Criteria criteria1 = new Criteria("item_price").greaterThanEqual(pri[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }

            if(!"*".equals(pri[1])){
                Criteria criteria1 = new Criteria("item_price").lessThanEqual(pri[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }
        //五、价格过滤查询
        //六、分页过滤查询
        //取当前
        Integer pageNo = (Integer)searchMap.get("pageNo");
        if(pageNo == null){
            pageNo = 1;
        }
        //取每页多少条
        Integer pageSize = (Integer)searchMap.get("pageSize");
        if(pageSize == null){
            pageSize = 20;
        }

        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        //六、分页过滤查询
        //七、排序
        String sort = (String)searchMap.get("sort");
        String sortField = (String)searchMap.get("sortField");

        if(sort != null && sort.length() > 0){
            if("ASC".equals(sort)){
                Sort sort2 = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort2);
            }else{
                Sort sort2 = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort2);
            }
        }
        //七、排序

        //5、查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);

        //6、渲染前台标题对象
        for (HighlightEntry<TbItem> entity : page.getHighlighted()){
            TbItem item = entity.getEntity();
            //判断高亮显示对象是否存在 并且 判断高亮显示对象的样式对象是否存在
            if(entity.getHighlights().size() > 0 && entity.getHighlights().get(0).getSnipplets().size() > 0){
                item.setTitle(entity.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("rows",page.getContent());
        //分页封装
        map.put("totalPages",page.getTotalPages());
        map.put("total",page.getTotalElements());
        return map;
    }


    /**
     * 分组查询
     * @param
     * @return
     */
    public List searchByGroup(Map searchMap){
        List list = new ArrayList();
        //1、创建查询对象
        Query query = new SimpleQuery();
        //2、创建查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //3、创建分组对象
        GroupOptions options = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(options);
        //4、查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query,TbItem.class);
        //5、结果处理
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

        Page<GroupEntry<TbItem>> pageEntry = groupResult.getGroupEntries();

        List<GroupEntry<TbItem>> groupList = pageEntry.getContent();

        for(GroupEntry<TbItem> entry : groupList){
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 查询品牌与规格
     * @param
     * @return
     */
    public Map<String,Object> findBrandAndSpecList(String category){
        System.out.println("findBrandAndSpecList...");
        Map<String,Object> map = new HashMap<String,Object>();

        //根据模板名称查对应的typeId 手机->35
        Long typeId = (Long)redisTemplate.boundHashOps("itemcat").get(category);
        if(typeId != null){
            //查询品牌
            List<Map> brandList = (List<Map>)redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            System.out.println("brangList size:"+brandList.size());

            //查询规格 [{"id":27,"text":"网络","options":[移动3G,联通4G]},{},{}]
            List<Map> specList = (List<Map>)redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList" , specList);
            System.out.println("specList size:"+specList.size());
        }
        return map;
    }

    //业务层逻辑框架
    public Map<String,Object> search(Map searchMap){
        Map<String,Object> map = new HashMap<String,Object>();

        //高亮显示
        map.putAll(searchList(searchMap));
        //分组查询
        List list = searchByGroup(searchMap);
        map.put("categoryList",list);
        //如果分类存在的话 查询品牌与规格 不能传关键字
        String category = (String)searchMap.get("category");
        System.out.println("category:"+category);
        if(category.length() > 0){
            map.putAll(findBrandAndSpecList(category));
        }else{
            if(list.size() > 0){
                //品牌和规格查询
                map.putAll(findBrandAndSpecList(list.get(0)+""));
            }
        }

        return map;
    }

    @Override
    public void importItems(List<TbItem> list) {

        solrTemplate.saveBeans(list);
        solrTemplate.commit();

    }

    @Override
    public void deleteItems(Long[] goodsIds) {

        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();

    }

}
