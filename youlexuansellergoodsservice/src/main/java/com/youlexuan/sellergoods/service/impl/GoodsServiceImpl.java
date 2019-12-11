package com.youlexuan.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.youlexuan.group.Goods;
import com.youlexuan.mapper.*;
import com.youlexuan.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.pojo.TbGoodsExample.Criteria;
import com.youlexuan.sellergoods.service.GoodsService;

import com.youlexuan.entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private  TbItemMapper itemMapper;

	@Override
	public List<TbItem> findItemsByIdsAndStatus(Long[] ids, String status) {
		//遍历审核的商品
		for (Long goodsId : ids){
			//根据商品id 查询对应item
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			List<TbItem> itemList = itemMapper.selectByExample(example);

			//修改每条item的状态值
			for (TbItem item : itemList){
				item.setStatus(status);
				itemMapper.updateByPrimaryKey(item);
			}
		}

		//查询满足id 与 status 一致数组
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		return  itemMapper.selectByExample(example);

	}

	@Override
	public void updateStatus(Long[] ids, String status) {

		for (Long id : ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}


	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//添加商品表
		goods.getGoods().setAuditStatus("0");//商品未审核
		goods.getGoods().setIsDelete("0");//0未删除 1已删除
		goodsMapper.insert(goods.getGoods());

		//添加商品描述表
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);

	}

	private void saveItemList(Goods goods){
		//1 是启用规格 0是不启用规格
		if("1".equals(goods.getGoods().getIsEnableSpec())) {

			//添加商品规格信息 item表
			try {
				for (TbItem item : goods.getItemList()) {
					//存标题
					String title = goods.getGoods().getGoodsName();
					Map<String, Object> map = JSON.parseObject(item.getSpec());
					for (String key : map.keySet()) {
						title += " " + map.get(key);
					}
					item.setTitle(title);

					//封装规格商品数据
					setItemValus(goods,item);

					//插入数据库
					itemMapper.insert(item);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//else 表示没有规格 在item表中添加一条数据
		}else{

			TbItem item = new TbItem();
			//存标题
			String title = goods.getGoods().getGoodsName();
			item.setTitle(title);

			//没有规格的默认数据
			item.setPrice(goods.getGoods().getPrice());
			item.setNum(99999);
			item.setStatus("0");
			item.setIsDefault("0");
			item.setSpec("{}");
			//封装规格商品数据
			setItemValus(goods,item);

			//插入数据库
			itemMapper.insert(item);
		}
	}


	public void setItemValus(Goods goods,TbItem item){

		//商家id
		item.setGoodsId(goods.getGoods().getId());
		//卖家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		//分类id
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		//取品牌
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//取分类
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//存图片 JSON转型 map
		List<Map> urls = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (urls.size() > 0) {
			//[{"color":"红色","url":"http://192.168.188.146/group1/M00/00/00/wKi8kl3JyeeAJqYxAATdw4ZsQyQ147.jpg"},
			// {"color":"蓝色","url":"http://192.168.188.146/group1/M00/00/00/wKi8kl3JyfCAQGBgAAT5imdysJk573.jpg"}]
			item.setImage((String) urls.get(0).get("url"));
		}

	}

	
	/**
	 * 修改
	 */

	public void update(Goods goods){
		goods.getGoods().setAuditStatus("0");
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria =example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));


		//对不同规格进行查询
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> list = itemMapper.selectByExample(example);
		goods.setItemList(list);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			System.out.println("id:"+id);
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}criteria.andIsDeleteEqualTo("0");
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
