package com.youlexuan.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.youlexuan.mapper.TbOrderItemMapper;
import com.youlexuan.mapper.TbPayLogMapper;
import com.youlexuan.pojo.*;
import com.youlexuan.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbOrderMapper;
import com.youlexuan.pojo.TbOrderExample.Criteria;
import com.youlexuan.order.service.OrderService;

import com.youlexuan.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	@Override
	public void updateOrderTradeStatus(String out_trade_no, String transaction_id) {

		//1、修改日志
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("2");//2、表示已付款
		payLog.setTransactionId(transaction_id);//交易付款的流水号
		payLogMapper.updateByPrimaryKey(payLog);
		//2、修改订单
		//获取订单编号列表
		String orderList = payLog.getOrderList();
		String[] ids = orderList.split(",");
		for(String orderId : ids){
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if(order!=null){
				order.setStatus("2");//表示已付款
				order.setPaymentTime(new Date());//支付时间
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//3、清空redis
		redisTemplate.boundHashOps("paylog").delete(payLog.getUserId());

	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("paylog").get(userId);
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		//用于日志表装订单编号
		List orderList = new ArrayList();
		//用于日志装订单总金额
		double total_money = 0;

		//取出当前登录人对应的购物车
		List<Cart> list = (List)redisTemplate.boundHashOps("cartList").get(order.getUserId());
		//遍历每一个店铺生成一个订单
		for (Cart cart : list){

			//雪花片法生成店铺的订单编号
			long orderNum = idWorker.nextId();
			System.out.println("雪花片法生成的订单编号："+orderNum);

			//定义TbOrder对象进行封装
			TbOrder order1 = new TbOrder();
			order1.setSellerId(order.getSellerId());
			order1.setSourceType(order.getSourceType());
			order1.setUserId(order.getUserId());
			order1.setOrderId(orderNum);//订单编号
			order1.setPaymentType(order.getPaymentType());//支付类型
			order1.setStatus("1");//支付状态 1表示未付款
			order1.setCreateTime(new Date());
			order1.setUpdateTime(new Date());
			order1.setReceiverAreaName(order.getReceiverAreaName());//收货人地址信息
			order1.setReceiverMobile(order.getReceiverMobile());//收货人联系方式
			order1.setReceiver(order.getReceiver());//收货人名字
			order1.setSellerId(order.getSellerId());//商家名称

			//每个商家的商品的总价格money对象
			double money = 0;

			//循环遍历商品
			for(TbOrderItem orderItem : cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(order1.getOrderId());//生成商品id编号
				orderItem.setSellerId(order.getSellerId());
				money += orderItem.getTotalFee().doubleValue();
				//统一店铺下不同的商品
				orderItemMapper.insert(orderItem);
			}
			order1.setPayment(new BigDecimal(money));//支付金额
			//统一订单下不同店铺
			orderMapper.insert(order1);

			//---日志---
			//存储每个商家购物车 对应的订单编号
			orderList.add(orderNum);
			//将所有店铺的总金额累加 是当前登录人该订单一共消费的金额
			total_money += money;

		}

		//1表示支付宝沙箱支付 2货到付款
		if("1".equals(order.getPaymentType())){
			//定义日志对象
			TbPayLog payLog = new TbPayLog();
			//创建雪花片法对象 生成对应id
			IdWorker idWorker = new IdWorker();
			payLog.setOutTradeNo(idWorker.nextId()+"");
			payLog.setCreateTime(new Date());
			payLog.setTotalFee((long)total_money);
			payLog.setUserId(order.getUserId());
			payLog.setTradeState("1");//1表示未支付
			//123,456,789
			payLog.setOrderList(orderList.toString().
					replace("[","").replace("]","").replace(" ",""));
			payLog.setPayType(order.getPaymentType());
			//将订单日志提交到mysql数据库中
			payLogMapper.insert(payLog);
			//将该订单存放到redis中
			redisTemplate.boundHashOps("paylog").put(order.getUserId(),payLog);
		}
		//---日志---

		//删除购物车中已经购买过的商品 (对于本项目而言 购买的商品是购物车中所有信息 所以清空购物车即可)
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
