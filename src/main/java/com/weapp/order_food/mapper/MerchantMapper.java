package com.weapp.order_food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weapp.order_food.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}