package com.youlexuan.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.pojo.TbSeller;
import com.youlexuan.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //security 安全机制
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //security 安全机制
        //取商家用户
        TbSeller seller = sellerService.findOne(username);

        //逻辑验证
        if(seller != null && "1".equals(seller.getStatus())){
            return new User(username,seller.getPassword(),grantedAuths);
        }else{
            return null;
        }
    }
}
