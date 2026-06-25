package com.weapp.order_food.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weapp.order_food.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}