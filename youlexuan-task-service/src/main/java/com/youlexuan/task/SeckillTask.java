package com.youlexuan.task;

import com.youlexuan.mapper.TbSeckillGoodsMapper;
import com.youlexuan.pojo.TbSeckillGoods;
import com.youlexuan.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Scheduled(cron="* * * * * ?")
    public void refreshSeckillGoods(){
        System.out.println("执行了任务调度"+new Date());

        List ids = new ArrayList(redisTemplate.boundHashOps("seckillList").keys());

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStartTimeLessThan(new Date());
        criteria.andEndTimeGreaterThan(new Date());
        criteria.andIdNotIn(ids);

        List<TbSeckillGoods> list = seckillGoodsMapper.selectByExample(example);

        for (TbSeckillGoods goods : list){
            redisTemplate.boundHashOps("seckillList").put(goods.getId(),goods);
        }

    }

    @Scheduled(cron="* * * * * ?")
    public void removeGoodsFromRedis(){
        List<TbSeckillGoods> list = new ArrayList(redisTemplate.boundHashOps("seckillList").keys());
        for (TbSeckillGoods seckillGoods:list){
            if(seckillGoods.getEndTime().getTime()<=new Date().getTime()){
                redisTemplate.boundHashOps("seckillList").delete(seckillGoods.getId());
            }
        }
    }


}
