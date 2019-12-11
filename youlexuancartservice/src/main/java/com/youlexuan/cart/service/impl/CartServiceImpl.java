package com.youlexuan.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.pojo.Cart;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements com.youlexuan.cart.service.CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 合并list
     * @param list1
     * @param list2
     * @return
     */
    public List<Cart> mergeList(List<Cart> list1,List<Cart> list2){
        List<Cart> mergeList = null;
        for (Cart cart : list2){
            for (TbOrderItem orderItem : cart.getOrderItemList()){
                mergeList = addGoodsToCart(list1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return mergeList;
    }

    @Override
    public List<Cart> addGoodsToCart(List<Cart> cartList, Long itemId, int num) {

        //1、查询要购买的商品是否存在
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("商品不存在");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("商品无效");
        }

        //2、获取商家id
        String sellerId = item.getSellerId();

        //3、查询用户购物车中是否包含该商家的购物车
        Cart cart = searchCartBySellerId(cartList,sellerId);

        //4、判断商家购物车是否存在
        //4.1 如果购物车中商家不存在
        if(cart == null){
            Cart cart1 = new Cart();
            cart1.setSellerId(sellerId);
            cart1.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item,num);

            List<TbOrderItem> list = new ArrayList<TbOrderItem>();
            list.add(orderItem);
            cart1.setOrderItemList(list);
            //将商家购物车装入到用户购物车中
            cartList.add(cart1);
        }else{
            //4.2 如果商家购物车存在于用户的购物车中
            List<TbOrderItem> list = cart.getOrderItemList();
            //4.3 遍历商品 判断商品是否存在商家购物车中
            TbOrderItem orderItem = searchGoodsByItemId(list,itemId);

            //4.4 如果该商品存在于该商家的购物车中 进行数量的叠加
            if(orderItem != null){
                //商品数量
                orderItem.setNum(orderItem.getNum().intValue() + num);
                //商品总金额
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum().intValue()));

                //4.4.1 如果商品数量 小于等于 0 则将商品从商家购物车移除
                if(orderItem.getNum() <= 0){
                    list.remove(orderItem);
                }

                //4.4.2 如果商家购物车中商品一件都没有了 移除该商家购物车
                if(cart.getOrderItemList().size() <= 0){
                    cartList.remove(cart);
                }
            //4.5 如果该商品不存在于该商家的购物车中
            }else{
                //4.5.1 新建商品订单
                TbOrderItem orderItem1 = createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem1);
            }

        }

        return cartList;
    }

    @Override
    public void addGoodsToRedisCart(List<Cart> cartList,String username) {
        System.out.println("向redis中存入"+username+"购物车");
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> findGoodsFromRedisCart(String username) {

        System.out.println("从redis购物车取值...");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        if(cartList == null){
            cartList = new ArrayList();
        }

        return cartList;
    }

    //查询商家购物车中是否存在该商品
    public TbOrderItem searchGoodsByItemId(List<TbOrderItem> list,Long itemId){
        for (TbOrderItem orderItem : list){
            if(orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }


    //创建购物车订单
    public TbOrderItem createOrderItem(TbItem item,int num){
        if(num <= 0){
            return  null;
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }


    //查询购物车中是否含有该商家
    public Cart searchCartBySellerId(List<Cart> cartList ,String sellerId){
        if(cartList != null) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }

}
