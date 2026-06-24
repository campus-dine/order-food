package com.weapp.order_food.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "购物车加减操作参数")
public class CartOperationDTO {
    private Long customerId;
    private Long dishId;
}