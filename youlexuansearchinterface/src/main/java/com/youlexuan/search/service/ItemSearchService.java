package com.youlexuan.search.service;

import com.youlexuan.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    public Map<String,Object> searchMap(Map searchMap);

    public Map<String,Object> search(Map searchMap);

    public void importItems(List<TbItem> list);

    public void deleteItems(Long[] goodsIds);

}
