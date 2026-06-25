package com.weapp.order_food.model.dto;

import lombok.Data;

@Data
public class OrderDetailQueryDTO {
    private Long orderId;     // 订单ID
    private Integer status;   // 状态校验
}