package com.youlexuan.cart.service;

import com.youlexuan.pojo.Cart;
import com.youlexuan.pojo.TbOrderItem;

import java.util.List;

public interface CartService {

    public List<Cart> addGoodsToCart(List<Cart> cartList, Long ItemId,int num);

    public void addGoodsToRedisCart(List<Cart> cartList,String username);

    public List<Cart> findGoodsFromRedisCart(String username);

    public List<Cart> mergeList(List<Cart> list1,List<Cart> list2);

}
