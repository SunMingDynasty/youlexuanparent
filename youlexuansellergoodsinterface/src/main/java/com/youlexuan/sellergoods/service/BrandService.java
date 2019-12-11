package com.youlexuan.sellergoods.service;

import com.youlexuan.entity.PageResult;
import com.youlexuan.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<TbBrand> findAll();
    public PageResult findPage(int pageNum, int pageSize);
    public void add(TbBrand brand);
    public TbBrand findOne(Long id);
    public void update(TbBrand brand);
    public void delete(Long[] ids);

    public PageResult search(TbBrand brand,int page,int rows);

    public List<Map> selectOptionList();
}
