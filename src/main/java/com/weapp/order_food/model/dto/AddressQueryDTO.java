package com.weapp.order_food.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "查询用户地址列表参数")
public class AddressQueryDTO {
    private Long customerId; // 客户ID
}