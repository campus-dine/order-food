package com.weapp.order_food.model.dto;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    private Long orderId;
    private Integer targetStatus;       // 对应主表的 0~5 状态
    private Integer deliveryStatus;     // 如果是外送订单，可选传入修改后的外送表状态
}