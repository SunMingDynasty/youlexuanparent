package com.youlexuan.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.entity.Result;
import com.youlexuan.pojo.Cart;
import com.youlexuan.pojo.TbOrderItem;
import com.youlexuan.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 当用户在未登录的情况下
     * 获取用户购物车
     */
    @RequestMapping("/findCookieCartList")
    public List<Cart> findCookieCartList(){


        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("login name : " + name);

        //从coookie中取cartList为key的键值对
        String cartList =  CookieUtil.getCookieValue(request,"cartList","utf-8");

        if(cartList == null || "".equals(cartList) || "null".equals(cartList)){
            cartList = "[]";
        }
        //cookie的购物车
        List<Cart> cookieCart = JSON.parseArray(cartList,Cart.class);

        //若name有值 则查询redis 若name为anonymousUser则查询cookie
        if("anonymousUser".equals(name)){
            return cookieCart;
        }else{
            //从redis中取来的集合
            List<Cart> redisCart = cartService.findGoodsFromRedisCart(name);
            if(cookieCart.size()>0){
                //合并cookie中的购物车
                redisCart = cartService.mergeList(redisCart,cookieCart);
                //清空cookie
                CookieUtil.deleteCookie(request,response,"cartList");
                //将合并后redis更新到redis服务器中
                cartService.addGoodsToRedisCart(redisCart,name);
            }
            return redisCart;
        }

    }

    @RequestMapping("/addGoodsToCart")
    public Result addGoodsToCart(Long itemId, int num){

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");


        try {

            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("login name : " + name);
            //1、先取出cookie中的购物车
            //未登录情况下获取购物车
            List<Cart> list = findCookieCartList();
            //向cookie购物车中添加要添加的商品
            //添加购物车
            List<Cart> cartList = cartService.addGoodsToCart(list, itemId, num);

            //若登录人名字为anonymousUser 则向cookie中添加购物车 若不为anonymousUser 向redis中添加购物车
            if("anonymousUser".equals(name)){
                //将添加好的购物车存入cookie中
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24,"UTF-8");
            }else{
                cartService.addGoodsToRedisCart(cartList,name);
            }

            return new Result(true,"加入购物车成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"加入购物车失败");
        }
    }

}
