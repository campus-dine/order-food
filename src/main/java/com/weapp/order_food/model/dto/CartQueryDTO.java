package com.weapp.order_food.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "查询购物车参数")
public class CartQueryDTO {
    private Long customerId;
}