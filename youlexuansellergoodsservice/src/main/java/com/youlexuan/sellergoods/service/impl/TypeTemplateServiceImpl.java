package com.youlexuan.sellergoods.service.impl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.youlexuan.mapper.TbBrandMapper;
import com.youlexuan.mapper.TbSpecificationOptionMapper;
import com.youlexuan.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbTypeTemplateMapper;
import com.youlexuan.pojo.TbTypeTemplateExample.Criteria;
import com.youlexuan.sellergoods.service.TypeTemplateService;

import com.youlexuan.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	public void saveToRedis(){

		//查询所有的模板
		List<TbTypeTemplate> list = findAll();
		//遍历
		for(TbTypeTemplate typeTemplate : list){

			//1、存品牌到redis
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(),Map.class);
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);



			//2、存规格到redis
			//[{"id":27,"text":"网络"，"options":[{},{},{}]},{"id":32,"text":"机身内存"}]
			List<Map> specList = findSpecByTypeId(typeTemplate.getId());
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);

			System.out.println("品牌列表和规格列表 更新...");

		}
	}

	public List<Map> findSpecByTypeId(Long id) {

		//根据模板id查询模板对象
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//取模板中的规格 [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(),Map.class);

//		遍历规格查找规格对应到选项
		for (Map map : list) {

//			取[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}] 中的 id值
			Long specId = new Long((Integer)map.get("id"));

			//查询
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(specId);

			List<TbSpecificationOption> specOptionList = specificationOptionMapper.selectByExample(example);
			map.put("options",specOptionList);
		}

		return list;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);

		//存入数据到缓存
		saveToRedis();

		return new PageResult(page.getTotal(), page.getResult());
	}



}
