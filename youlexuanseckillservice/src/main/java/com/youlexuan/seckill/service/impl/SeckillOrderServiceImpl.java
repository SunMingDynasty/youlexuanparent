package com.youlexuan.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.youlexuan.mapper.TbSeckillGoodsMapper;
import com.youlexuan.pojo.TbSeckillGoods;
import com.youlexuan.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbSeckillOrderMapper;
import com.youlexuan.pojo.TbSeckillOrder;
import com.youlexuan.pojo.TbSeckillOrderExample;
import com.youlexuan.pojo.TbSeckillOrderExample.Criteria;
import com.youlexuan.seckill.service.SeckillOrderService;

import com.youlexuan.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private IdWorker idWorker;

	@Override
	public void deleteOrderFromRedis(String userId, String orderId) {
		//取出当前登录人的秒杀订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder != null){
			//如果超时 删除该登录人对应的秒杀订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);

			//恢复库存
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillList").get(seckillOrder.getSeckillId());
			seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			redisTemplate.boundHashOps("seckillList").put(seckillOrder.getSeckillId(),seckillGoods);
		}

	}

	@Override
	public void saveOrderFromRedisToDb(String userId, String orderId, String transactionId) {
		//先查询当前用户是否有秒杀的商品
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

		if(seckillOrder == null){
			throw  new RuntimeException("订单不存在");
		}
		System.out.println("orderId : "+orderId);
		System.out.println("seckillOrder id : "+ seckillOrder.getId());
		System.out.println("orderId.equals(seckillOrder.getId()) : "+orderId.equals(seckillOrder.getId()));
		if(!orderId.equals(seckillOrder.getId() + "") ){
			throw new RuntimeException("订单编号不一致");
		}
		//向订单对象中装入付款信息
		seckillOrder.setStatus("2");//2表示已支付
		seckillOrder.setTransactionId(transactionId);
		seckillOrder.setPayTime(new Date());
		//存入mysql数据库 生成订单
		seckillOrderMapper.insert(seckillOrder);
		//删除redis数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public TbSeckillOrder searchOrderFromRedis(String userId) {
		return (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void submitOrder(Long seckillId, String userId) {
		//从redis中取出秒杀商品
		TbSeckillGoods goods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillList").get(seckillId);

		//
		if(goods == null) {
			throw new RuntimeException("商品不存在");
		}else if(goods.getStockCount() <= 0){
			throw new RuntimeException("商品已售罄");
		}
		//商品的库存 减1
		goods.setStockCount(goods.getStockCount() - 1);

		//如果库存已经==0
		if(goods.getStockCount() == 0){
			//将改商品状态添加库存
			seckillGoodsMapper.updateByPrimaryKey(goods);
			//将秒杀完毕的商品 从redis中移除
			redisTemplate.boundHashOps("seckillList").delete(goods.getId());
		}else{
			//redis 更新库存
			redisTemplate.boundHashOps("seckillList").put(goods.getId(),goods);
		}

		//保存订单
		//保存（redis）订单
		long orderId = idWorker.nextId();
		//定义秒杀订单对象
		TbSeckillOrder seckillOrder=new TbSeckillOrder();

		seckillOrder.setId(orderId);//存入秒杀的订单号
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(goods.getCostPrice());//秒杀价格
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setSellerId(goods.getSellerId());//商家
		seckillOrder.setUserId(userId);//设置用户ID
		seckillOrder.setStatus("1");//状态 1未付款 2已支付 3已付款
		//秒杀订单存到redis中
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
