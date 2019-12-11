package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbBrandMapper;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.pojo.TbBrandExample;
import com.youlexuan.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        //设定分页插件条件
        PageHelper.startPage(pageNum,pageSize);
        //查询
        Page<TbBrand> page = (Page)brandMapper.selectByExample(null);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    public void update(TbBrand brand){
        brandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id:ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult search(TbBrand brand, int page, int rows) {
        //1、设定查询起始页
        PageHelper.startPage(page,rows);

        //2、模糊查询条件
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();

        if(brand != null){
            if(brand.getName() != null && brand.getName().length() > 0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            System.out.println("firstChar:"+brand.getFirstChar());
            if(brand.getFirstChar() != null && brand.getFirstChar().length() >0){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }

        //3、查询
        Page<TbBrand> p = (Page<TbBrand>) brandMapper.selectByExample(example);

        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
