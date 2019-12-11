package com.youlexuan.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbContentMapper;
import com.youlexuan.pojo.TbContent;
import com.youlexuan.pojo.TbContentExample;
import com.youlexuan.pojo.TbContentExample.Criteria;
import com.youlexuan.content.service.ContentService;

import com.youlexuan.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbContent> findByCategoryId(Long id) {

		//从redis中取广告数据
		List<TbContent> list = (List<TbContent>)redisTemplate.boundHashOps("content").get(id);

		if(list == null){
			//向redis中插入轮播
			System.out.println("从mysql数据库中取值");
			//从mysql中取广告数据
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");//开启状态
			criteria.andCategoryIdEqualTo(id);
			example.setOrderByClause("sort_order");//排序
			list = contentMapper.selectByExample(example);

			//向redis中存入广告数据
			redisTemplate.boundHashOps("content").put(id,list);


//			//向redis中插入今日推荐
//			criteria.andStatusEqualTo("1");//开启状态
//			criteria.andCategoryIdEqualTo(2l);
//			example.setOrderByClause("sort_order");//排序
//			List list2 = contentMapper.selectByExample(example);
//			System.out.println("list2 size:"+list2);
//			//向redis中存入广告数据
//			redisTemplate.boundHashOps("content").put(2l,list2);


		}else{
			System.out.println("从redis数据库中取值");
		}
		return list;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清空redis对应的categoryId
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){

		TbContent content1 = contentMapper.selectByPrimaryKey(content.getId());
		Long categoryId = content1.getCategoryId();
		redisTemplate.boundHashOps("content").delete(content1.getCategoryId());

		System.out.println("redis 修改 第一次删除");

		contentMapper.updateByPrimaryKey(content);
		System.out.println("修改前："+content1.getCategoryId());
		System.out.println("修改后："+content.getCategoryId());

		//清空要改变的那个categoryId下的所有节点
		if(categoryId.longValue() != content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
			System.out.println("redis 修改 第二次删除");
		}

	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbContent content = contentMapper.selectByPrimaryKey(id);
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
